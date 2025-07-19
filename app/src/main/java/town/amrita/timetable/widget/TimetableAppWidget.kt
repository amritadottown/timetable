package town.amrita.timetable.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import town.amrita.timetable.R
import town.amrita.timetable.activity.DateSwitcherActivity
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.models.TimetableDisplayEntry
import town.amrita.timetable.models.buildTimetableDisplay
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.utils.TODAY
import town.amrita.timetable.widget.Sizes.BEEG
import town.amrita.timetable.widget.Sizes.SMOL
import java.time.Instant
import java.util.concurrent.TimeUnit

class TimetableWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = TimetableAppWidget()
}

class TimetableAppWidget : GlanceAppWidget() {
  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    coroutineScope {
      val store = context.widgetConfig
      val initial = store.data.first()
      val timetable = store.data.map {
        val file = context.openFileInput("${it.file?.removeSuffix(".json")}.json")
        val timetable = Json.decodeFromStream<Timetable>(file)
        buildTimetableDisplay(it.day ?: TODAY, timetable)
      }.stateIn(this)

      val work = PeriodicWorkRequestBuilder<UpdateWorker>(30, TimeUnit.MINUTES)
        .build()

      WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork("TIMETABLE_UPDATE_WORKER", ExistingPeriodicWorkPolicy.KEEP, work)

      provideContent {
        val data by store.data.collectAsState(initial)
        val day = data.day ?: TODAY
        val times by timetable.collectAsState()

        val isLockedNow =
          with(data.lockedUntil) {
            if (this != null)
              Instant.ofEpochSecond(this).isAfter(Instant.now())
            else false
          }

        TimetableWidget(day, isLockedNow, times)
      }
    }
  }

  override val sizeMode = SizeMode.Responsive(setOf(SMOL, BEEG))
}

object Sizes {
  val SMOL = DpSize(100.dp, 100.dp)
  val BEEG = DpSize(250.dp, 100.dp)
}

@Composable
fun TimetableWidget(day: String, isLockedNow: Boolean, times: List<TimetableDisplayEntry>) {
  val textStyle = TextStyle(color = GlanceTheme.colors.onSurface)

  GlanceTheme {
    Scaffold(
      titleBar = @Composable {
        TitleBar(day, isLockedNow)
      },
      backgroundColor = GlanceTheme.colors.background,
      modifier = GlanceModifier.padding(bottom = 12.dp),
    ) {
      Box(GlanceModifier.appWidgetInnerCornerRadius(12.dp)) {
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

@Composable
fun TitleBar(day: String, locked: Boolean) {
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
      if (locked) {
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

  val isBeeg = LocalSize.current.width >= BEEG.width

  with(item) {
    Box(
      GlanceModifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp)
        .appWidgetInnerCornerRadius(12.dp)
        .background(GlanceTheme.colors.widgetBackground)
    ) {
      Column {
        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
          Text(if (isBeeg) name else shortName, style = strongTextStyle)
          Spacer(GlanceModifier.padding(start = 4.dp))
          if (lab) {
            Image(
              provider = ImageProvider(R.drawable.science_24px),
              contentDescription = "Lab",
              colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
            )
          }
        }
        Row {
          Text(
            "Period ${if (start == end) start + 1 else "${start + 1} → ${end + 1}"}",
            style = textStyle
          )
          if (isBeeg) {
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
}

@SuppressLint("LocalContextResourcesRead")
@Composable
fun GlanceModifier.appWidgetInnerCornerRadius(widgetPadding: Dp): GlanceModifier {
  if (Build.VERSION.SDK_INT < 31) {
    return this
  }

  val resources = LocalContext.current.resources
  // get dimension in float (without rounding).
  val px = resources.getDimension(android.R.dimen.system_app_widget_background_radius)
  val widgetBackgroundRadiusDpValue = px / resources.displayMetrics.density
  if (widgetBackgroundRadiusDpValue < widgetPadding.value) {
    return this
  }
  return this.cornerRadius(Dp(widgetBackgroundRadiusDpValue - widgetPadding.value))
}
