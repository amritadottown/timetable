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
import androidx.compose.ui.util.fastFirstOrNull
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
import androidx.glance.appwidget.action.actionSendBroadcast
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
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
import town.amrita.timetable.utils.DAYS
import town.amrita.timetable.utils.TODAY
import town.amrita.timetable.widget.Sizes.BEEG
import town.amrita.timetable.widget.Sizes.SMOL
import java.time.Instant
import java.time.LocalTime

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
        val dayToShow =
          when (it.day) {
            null -> {
              if (LocalTime.now().isBefore(initial.showNextDayAt))
                TODAY
              else
                DAYS[(DAYS.indexOf(TODAY) + 1) % DAYS.size]
            }

            else -> it.day
          }
        Pair(dayToShow, buildTimetableDisplay(dayToShow, timetable, it.showFreePeriods))
      }.stateIn(this)

      context.ensureWorkAndAlarms()

      provideContent {
        val data by store.data.collectAsState(initial)
        val timetableState by timetable.collectAsState()
        val (day, times) = timetableState

        val isLockedNow =
          with(data.lockedUntil) {
            if (this != null)
              Instant.ofEpochSecond(this).isAfter(Instant.now())
            else false
          }

        val currentPeriod = times.fastFirstOrNull { it.slot.containsTime(LocalTime.now()) }
        val nextPeriod = when {
          times.isEmpty() -> null
          LocalTime.now() < times.first().slot.start -> null
          LocalTime.now() >= times.last().slot.start -> null
          else -> times.fastFirstOrNull { LocalTime.now() < it.slot.start }
        }

        TimetableWidget(day, isLockedNow, times, currentPeriod?.shortName, nextPeriod?.shortName)
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
fun TimetableWidget(
  day: String,
  locked: Boolean,
  entries: List<TimetableDisplayEntry>,
  current: String? = null,
  next: String? = null
) {
  val textStyle = TextStyle(color = GlanceTheme.colors.onSurface)

  GlanceTheme {
    Scaffold(
      titleBar = { TitleBar(day, locked, current, next) },
      backgroundColor = GlanceTheme.colors.background,
      modifier = GlanceModifier.padding(bottom = 12.dp),
    ) {
      Box(GlanceModifier.appWidgetInnerCornerRadius(12.dp)) {
        if (entries.isEmpty()) {
          Box(
            GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Text("Nothing today :)", style = textStyle)
          }
        } else {
          LazyColumn {
            items(entries) { time ->
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
fun TitleBar(day: String, locked: Boolean, current: String? = null, next: String? = null) {
  val textStyle =
    TextStyle(color = GlanceTheme.colors.onSurface)
  val strongTextStyle =
    TextStyle(color = GlanceTheme.colors.onSurface, fontWeight = FontWeight.Medium)

  Column {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = GlanceModifier
        .fillMaxWidth()
        .height(48.dp)
        .padding(start = 16.dp)
        .clickable(actionStartActivity<DateSwitcherActivity>())
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier
          .background(GlanceTheme.colors.primary)
          .size(24.dp)
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

      if (LocalSize.current.width >= BEEG.width && (current != null || next != null)) {
        val currentNextString = when {
          current != null && next != null -> "Now $current, next $next"
          current != null -> "Now $current"
          next != null -> "Next $next"
          else -> ""
        }
        Text(
          "•",
          style = textStyle,
          modifier = GlanceModifier.padding(horizontal = 6.dp)
        )
        Text(currentNextString, style = textStyle)
      }


      Spacer(GlanceModifier.defaultWeight())
      Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier
          .size(48.dp)
          .clickable(actionSendBroadcast<AlarmReceiver>())
      ) {
        Image(
          modifier = GlanceModifier.size(24.dp),
          provider = ImageProvider(R.drawable.refresh_24px),
          contentDescription = "Refresh",
          colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
        )
      }
    }
  }
}

@Composable
fun TimetableItem(item: TimetableDisplayEntry) {

  val isBeeg = LocalSize.current.width >= BEEG.width

  with(item) {
    val isActive = item.slot.containsTime(LocalTime.now())

    val backgroundColor =
      if (isActive) GlanceTheme.colors.primary else GlanceTheme.colors.widgetBackground
    val textColor = if (isActive) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurface
    val textStyle = TextStyle(color = textColor)
    val strongTextStyle =
      TextStyle(color = textColor, fontWeight = FontWeight.Medium)

    val iconColor = if (isActive) GlanceTheme.colors.onPrimary else GlanceTheme.colors.primary

    Box(
      GlanceModifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp)
        .appWidgetInnerCornerRadius(12.dp)
        .background(backgroundColor)
    ) {
      Column {
        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
          Text(if (isBeeg) name else shortName, style = strongTextStyle)
          Spacer(GlanceModifier.padding(start = 4.dp))
          if (lab) {
            Image(
              provider = ImageProvider(R.drawable.science_24px),
              contentDescription = "Lab",
              colorFilter = ColorFilter.tint(iconColor)
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
            Text(slot.toString(), style = textStyle)
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
