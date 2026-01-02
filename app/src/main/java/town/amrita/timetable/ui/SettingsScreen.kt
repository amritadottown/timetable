package town.amrita.timetable.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import town.amrita.timetable.BuildConfig
import town.amrita.timetable.models.DEFAULT_CONFIG
import town.amrita.timetable.models.updateShowCompletedPeriods
import town.amrita.timetable.models.updateShowFreePeriods
import town.amrita.timetable.models.updateShowNextDayAt
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.ui.components.TimetableScaffold
import town.amrita.timetable.widget.AlarmReceiver
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val settings by context.widgetConfig.data.collectAsState(DEFAULT_CONFIG)

  TimetableScaffold("Settings") {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Show Free Periods")
        Switch(
          settings.showFreePeriods,
          onCheckedChange = {
            scope.launch {
              context.updateShowFreePeriods(it)
            }
          }
        )
      }

      Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Show Completed Periods")
        Switch(
          settings.showCompletedPeriods,
          onCheckedChange = {
            scope.launch {
              context.updateShowCompletedPeriods(it)
            }
          }
        )
      }

      var showTimePicker by remember { mutableStateOf(false) }
      Box(
        Modifier
          .fillMaxWidth()
          .clickable(onClick = { showTimePicker = true })
      ) {
        Row(
          Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          val showNextDayAt = settings.showNextDayAt

          Text("Show next day after")
          Text(DateTimeFormatter.ofPattern("hh:mm a").format(showNextDayAt))

          if (showTimePicker) {
            val timePickerState = rememberTimePickerState(showNextDayAt.hour, showNextDayAt.minute)
            TimePickerDialog(
              onDismissRequest = { showTimePicker = false },
              dismissButton = {
                TextButton(onClick = {
                  showTimePicker = false
                }) { Text("Cancel") }
              },
              confirmButton = {
                TextButton(onClick = {
                  scope.launch {
                    context.updateShowNextDayAt(
                      LocalTime.of(
                        timePickerState.hour,
                        timePickerState.minute
                      )
                    )
                  }
                  showTimePicker = false
                }) { Text("OK") }
              },
              title = {}) {
              TimePicker(timePickerState)
            }
          }
        }
      }

      HorizontalDivider()
      Column {
        Text("ASEB Timetable")
        Text("Version ${BuildConfig.VERSION_NAME} ${BuildConfig.FLAVOR}")
        Text(
          buildAnnotatedString {
            append("Built by ")
            withLink(LinkAnnotation.Url("https://amrita.town")) { append("amrita.town") }
          }
        )
        Text(
          buildAnnotatedString {
            withLink(LinkAnnotation.Url("https://play.google.com/store/apps/details?id=town.amrita.timetable")) {
              append(
                "ASEB Timetable on Google Play"
              )
            }
          }
        )
        Text(
          buildAnnotatedString {
            withLink(LinkAnnotation.Url("https://github.com/amritadottown/timetable")) {
              append(
                "ASEB Timetable on GitHub"
              )
            }
          }
        )
        Text(
          buildAnnotatedString {
            withLink(LinkAnnotation.Url("https://github.com/amritadottown/timetable-registry")) {
              append(
                "Timetable Registry on GitHub"
              )
            }
          }
        )
//        Text("More stuff here soon I guess")
        if (BuildConfig.DEBUG) {
          val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

          Column {
            Button(onClick = {
              val intent = Intent(context, AlarmReceiver::class.java)
              val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
              )
              alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 5000,
                pendingIntent
              )
            }) {
              Text("Test widget update alarm")
            }
          }

        }
      }
    }
  }
}

@Preview
@Composable
fun SettingsScreenPreview() {
  TimetableTheme {
    SettingsScreen()
  }
}