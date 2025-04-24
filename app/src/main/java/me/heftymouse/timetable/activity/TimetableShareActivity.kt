package me.heftymouse.timetable.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import me.heftymouse.timetable.utils.updateTimetableFromUri

class TimetableShareActivity : ComponentActivity() {
    @SuppressLint("NewApi")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showSecondaryText by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            TimetableTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    topBar = @Composable {
                        LargeTopAppBar(
                            title = @Composable {
                                Text(
                                    "Timetable",
                                    style = MaterialTheme.typography.displaySmall
                                )
                            },
                            colors = TopAppBarDefaults.mediumTopAppBarColors()
                                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            windowInsets = TopAppBarDefaults.windowInsets.add(
                                WindowInsets(
                                    left = 8.dp,
                                    right = 8.dp
                                )
                            )
                        )
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(intent.getStringExtra(Intent.EXTRA_TEXT) ?: "⚠️ Unknown name")
                        Button(onClick = {
                            scope.launch {
                                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)?.run {
                                    updateTimetableFromUri(this)
                                    showSecondaryText = true
                                }
                            }
                        }) {
                            Text("Use Timetable")
                        }
                        if(showSecondaryText) {
                            Text("If it worked the widget should be updated now. no guarantees tho")
                            FilledTonalButton(onClick = { finish() }) {
                                Text("Done")
                            }
                        }
                    }
                }
            }
        }
    }
}