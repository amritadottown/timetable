package me.heftymouse.timetable.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.heftymouse.timetable.R
import me.heftymouse.timetable.models.Timetable
import me.heftymouse.timetable.models.lockedKey
import me.heftymouse.timetable.models.widgetConfig
import me.heftymouse.timetable.widget.TimetableWidget
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DateSwitcherActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"))

        setContent {
            val scope = rememberCoroutineScope()

            val data = widgetConfig.data
            val isLockedFlow = data.map { prefs ->
                val e = prefs[lockedKey]
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
                        onDismissRequest = {
                            finish()
                        },
                        windowInsets = WindowInsets(0.dp),
                        sheetState = SheetState(
                            skipPartiallyExpanded = true,
                            density = LocalDensity.current
                        )
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
                                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(style = MaterialTheme.typography.titleLarge, text = "Day")
                                    if(isLocked) {
                                        Button(onClick = {
                                            scope.launch {
                                                updateLock(false)
                                            }

                                        }) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(painter = painterResource(R.drawable.lock_open_24px), contentDescription = "Unlock")
                                                Spacer(Modifier.width(4.dp))
                                                Text("Unlock")
                                            }
                                        }
                                    } else {
                                        TextButton(
                                            onClick = {
                                                scope.launch {
                                                    updateLock(true)
                                                }
                                            }
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(painter = painterResource(R.drawable.lock_24px), contentDescription = "Lock")
                                                Spacer(Modifier.width(4.dp))
                                                Text("Lock")
                                            }
                                        }
                                    }
                                }
                            }
                            items(items = days) { item ->
                                TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                                    scope.launch {
                                        updateWidget(item)
                                    }
                                }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            item,
                                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                                        )
                                        if (item == today) {
                                            Spacer(Modifier.width(4.dp))
                                            Icon(painter = painterResource(R.drawable.today_24px), contentDescription = "Today")
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

    private suspend fun updateLock(isLocked: Boolean) {
        widgetConfig.updateData {
            it.toMutablePreferences().apply {
                this[lockedKey] =
                    if(isLocked)
                        LocalDateTime.now()
                            .plusDays(1)
                            .truncatedTo(ChronoUnit.DAYS)
                            .toEpochSecond(ZonedDateTime.now().offset)
                    else Instant.MIN.epochSecond
            }
        }
        TimetableWidget().updateAll(this)
    }

    private suspend fun updateWidget(day: String) {
        me.heftymouse.timetable.models.updateWidget(this, day)
        finish()
    }
}