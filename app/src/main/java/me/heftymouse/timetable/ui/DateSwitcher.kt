package me.heftymouse.timetable.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.heftymouse.timetable.R
import me.heftymouse.timetable.models.lockedUntilKey
import me.heftymouse.timetable.models.updateDay
import me.heftymouse.timetable.models.updateLock
import me.heftymouse.timetable.models.widgetConfig
import me.heftymouse.timetable.utils.DAYS
import me.heftymouse.timetable.utils.TODAY
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSwitcher() {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  val data = context.widgetConfig.data
  val isLockedFlow = data.map { prefs ->
    val e = prefs[lockedUntilKey]
    if (e != null)
      Instant.ofEpochSecond(e).isAfter(Instant.now())
    else false
  }
  val isLocked by isLockedFlow.collectAsState(false)

  TimetableTheme {
    Surface(
      Modifier
        .fillMaxSize()
        .padding(0.dp),
      color = Color.Transparent,
      contentColor = contentColorFor(MaterialTheme.colorScheme.background)
    ) {
      ModalBottomSheet(
        onDismissRequest = { (context as? Activity)?.finish() },
        sheetState = SheetState(
          skipPartiallyExpanded = true,
          density = LocalDensity.current
        ),
        contentWindowInsets = { WindowInsets(0.dp) }
      ) {
        LazyColumn(
          Modifier.padding(
            top = 0.dp,
            bottom = 36.dp,
            start = 12.dp,
            end = 12.dp
          )
        ) {
          item {
            Row(
              Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(style = MaterialTheme.typography.titleLarge, text = "Day")
              if (isLocked) {
                Button(onClick = {
                  scope.launch {
                    context.updateLock(false)
                  }

                }) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      painter = painterResource(R.drawable.lock_open_24px),
                      contentDescription = "Unlock"
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Unlock")
                  }
                }
              } else {
                TextButton(
                  onClick = {
                    scope.launch {
                      context.updateLock(true)
                    }
                  }
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      painter = painterResource(R.drawable.lock_24px),
                      contentDescription = "Lock"
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Lock")
                  }
                }
              }
            }
          }
          items(items = DAYS) { day ->
            TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
              scope.launch {
                context.updateDay(day)
              }
            }) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  day,
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
}