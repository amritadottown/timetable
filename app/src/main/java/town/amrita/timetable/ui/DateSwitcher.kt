package town.amrita.timetable.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import town.amrita.timetable.R
import town.amrita.timetable.activity.MainActivity
import town.amrita.timetable.models.updateDay
import town.amrita.timetable.models.updateLock
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.utils.DAYS
import town.amrita.timetable.utils.TODAY
import town.amrita.timetable.utils.longName
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSwitcher() {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  val data = context.widgetConfig.data
  val isLockedFlow = data.map { prefs ->
    val e = prefs.lockedUntil
    if (e != null)
      Instant.ofEpochSecond(e).isAfter(Instant.now())
    else false
  }
  val isLocked by isLockedFlow.collectAsState(false)

  TimetableTheme {
    ModalBottomSheet(
      onDismissRequest = { (context as? Activity)?.finish() },
      sheetState = rememberModalBottomSheetState(true),
      contentWindowInsets = { WindowInsets.safeDrawing }
    ) {
      Column(Modifier.padding(start = 24.dp, end = 12.dp)) {
        Row(
          Modifier
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(style = MaterialTheme.typography.titleLarge, text = "Day")
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Lock")
            Switch(
              checked = isLocked,
              onCheckedChange = {
                scope.launch {
                  context.updateLock(it)
                }
              }
            )

            IconButton(onClick = {
              val intent = Intent(context, MainActivity::class.java)
              context.startActivity(intent)
            }) {
              Icon(
                painter = painterResource(R.drawable.settings_24px),
                contentDescription = "Settings"
              )
            }
          }
        }
        DAYS.map { day ->
          TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
            scope.launch {
              context.updateDay(day)
            }
          }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                day.longName(),
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
              )
              if (day == TODAY) {
                Spacer(Modifier.width(4.dp))
                Icon(
                  painter = painterResource(R.drawable.today_24px),
                  contentDescription = "Today"
                )
              }
            }
          }
        }
      }
    }
  }
}

@Preview
@Composable
fun DateSwitcherPreview() {
  DateSwitcher()
}