package me.heftymouse.timetable.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.heftymouse.timetable.R
import me.heftymouse.timetable.activity.DateSwitcherActivity
import me.heftymouse.timetable.models.Subject
import me.heftymouse.timetable.models.Timetable
import me.heftymouse.timetable.models.dayKey
import me.heftymouse.timetable.models.fileKey
import me.heftymouse.timetable.models.lockedUntilKey
import me.heftymouse.timetable.models.widgetConfig
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

data class TimetableDisplayEntry(val name: String, val slot: String, val start: Int, val end: Int, val lab: Boolean)

class TimetableWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
            = TimetableWidget()
}

class TimetableWidget : GlanceAppWidget() {
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        coroutineScope {
            val store = context.widgetConfig
            val initial = store.data.first()

            val work = PeriodicWorkRequestBuilder<UpdateWorker>(30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork("TIMETABLE_UPDATE_WORKER", ExistingPeriodicWorkPolicy.KEEP, work)

            provideContent {
                val textStyle = TextStyle(color = GlanceTheme.colors.onSurface)

                val data by store.data.collectAsState(initial)
                val day = data[dayKey] ?: LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"))
                val file = context.openFileInput(data[fileKey])
                val timetable = Json.decodeFromStream<Timetable>(file)

                val times: MutableList<TimetableDisplayEntry> = mutableListOf()
                if (timetable.schedule.containsKey(day)) {
                    var i = 0
                    timetable.schedule[day]?.forEach { x ->

                        val subject = when(x) {
                            "FREE" -> Subject("Free", "", "")
                            else -> timetable.subjects[x.removeSuffix("_LAB")]!!
                        }

                        val offset = if (x.endsWith("_LAB"))
                            when (i) {
                                0 -> 3
                                else -> 2
                            }
                        else 1

                        val slot = if (x.endsWith("_LAB"))
                            when (i) {
                                0 -> timetable.labSlots[0]
                                3 -> timetable.labSlots[1]
                                5 -> timetable.labSlots[2]
                                else -> "⚠️ UNKNOWN"
                            }
                        else timetable.slots[i]

                        times.add(TimetableDisplayEntry(subject.name, slot, i, i + offset - 1, x.endsWith("_LAB")))

                        i += offset
                    }
                }

                val isLockedNow =
                    with(data[lockedUntilKey]) {
                        if(this != null)
                            Instant.ofEpochSecond(this).isAfter(Instant.now())
                        else false
                    }

                GlanceTheme {
                    Scaffold(
                        titleBar = @Composable {
                            TitleBar(day, isLockedNow)
                        },
                        backgroundColor = GlanceTheme.colors.background,
                        modifier = GlanceModifier.padding(bottom = 12.dp)
                    ) {
                        Box(GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_inner_radius)) {
                            if (times.isEmpty()) {
                                Box(
                                    GlanceModifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Nothing today :)", style = textStyle)
                                }
                            } else {
                                LazyColumn {
                                    items(times) { time ->
                                        val padding = when (time.end) {
                                            6 -> 0.dp
                                            else -> 4.dp
                                        }

                                        Box(GlanceModifier.padding(bottom = padding)) {
                                            TimetableItem(time)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TitleBar(day: String, locked: Boolean) {
    val textStyle = TextStyle(color = GlanceTheme.colors.onSurface)
    val strongTextStyle =
        TextStyle(color = GlanceTheme.colors.onSurface, fontWeight = FontWeight.Medium)

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .clickable(actionStartActivity<DateSwitcherActivity>())
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = GlanceModifier
                    .background(GlanceTheme.colors.primary)
                    .width(24.dp).height(24.dp)
                    .cornerRadius(12.dp)
            ) {
                Image(
                    provider = ImageProvider(R.drawable.today_24px),
                    contentDescription = "Today",
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimary)
                )
            }
            Spacer(GlanceModifier.width(8.dp))
            Text(
                day,
                style = strongTextStyle
            )
            if(locked)
            {
                Spacer(GlanceModifier.width(4.dp))
                Image(
                    provider = ImageProvider(R.drawable.lock_24px),
                    contentDescription = "Locked",
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                )
            }
        }
    }
}

@Composable
fun TimetableItem(item: TimetableDisplayEntry) {
    val textStyle = TextStyle(color = GlanceTheme.colors.onSurface)
    val strongTextStyle =
        TextStyle(color = GlanceTheme.colors.onSurface, fontWeight = FontWeight.Medium)

    with(item) {
        Box(
            GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .cornerRadius(android.R.dimen.system_app_widget_inner_radius)
                .background(GlanceTheme.colors.widgetBackground)
        ) {
            Column {
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Text(name, style = strongTextStyle)
                    Spacer(GlanceModifier.padding(start = 4.dp))
                    if (lab) {
                        Image(provider = ImageProvider(R.drawable.science_24px), contentDescription = "Lab", colorFilter = ColorFilter.tint(GlanceTheme.colors.primary))
                    }
                }
                Row {
                    Text(
                        "Period ${if (start == end) start + 1 else "${start + 1} → ${end + 1}"}",
                        style = textStyle
                    )
                    Text(
                        "•",
                        style = textStyle,
                        modifier = GlanceModifier.padding(horizontal = 6.dp)
                    )
                    Text(slot, style = textStyle)
                }
            }
        }
    }
}
