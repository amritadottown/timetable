package town.amrita.timetable.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import town.amrita.timetable.ui.DateSwitcher
import town.amrita.timetable.ui.TimetableTheme

class DateSwitcherActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      TimetableTheme {
        DateSwitcher()
      }
    }
  }
}