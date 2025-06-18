package me.heftymouse.timetable.activity

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.heftymouse.timetable.ui.TimetableTheme
import me.heftymouse.timetable.utils.updateTimetableFromUri

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            var showSecondaryText by remember { mutableStateOf(false) }
            val launcher =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    Log.d("Timetable", uri?.toString() ?: "no uri")
                    if (uri != null) {
                        scope.launch {
                            updateTimetableFromUri(uri)
                        }
                    }
                    showSecondaryText = true
                }

            TimetableTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    topBar = @Composable {
                        LargeTopAppBar(
                            title = @Composable { Text("Timetable", style = MaterialTheme.typography.displaySmall) },
                            colors = TopAppBarDefaults.mediumTopAppBarColors()
                                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 8.dp, right = 8.dp))
                        )
                    }
                ) { innerPadding ->
                    Column(Modifier.padding(innerPadding).padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = {
                            launcher.launch(arrayOf("application/json"))
                        }) {
                            Text("Pick JSON")
                        }
                        if(showSecondaryText) {
                            Text("If it worked the widget should be updated now. no guarantees tho")
                            Button(onClick = {
                                val appWidgetId = intent?.extras?.getInt(
                                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                                    AppWidgetManager.INVALID_APPWIDGET_ID
                                ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
                                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                setResult(RESULT_OK, resultValue)
                                finish()
                            }) {
                                Text("Done")
                            }
                        }
                    }
                }
            }
        }
    }
}