package town.amrita.timetable.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import town.amrita.timetable.ui.RootScreen
import town.amrita.timetable.ui.Route
import town.amrita.timetable.ui.TimetableTheme

class TimetableShareActivity : ComponentActivity() {
  @SuppressLint("NewApi")
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)

    setContent {
      TimetableTheme {
        RootScreen(startRoute = Route.ShareScreenRoute(uri))
      }
    }
  }
}