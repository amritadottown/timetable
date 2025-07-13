package town.amrita.timetable.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import town.amrita.timetable.R
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.models.TimetableDisplayEntry
import town.amrita.timetable.models.buildTimetableDisplay
import town.amrita.timetable.utils.DAYS

@Composable
fun TimetablePreview(modifier: Modifier = Modifier, timetable: Timetable?) {
  Box(modifier) {
    if (timetable != null) {
      val pagerState = rememberPagerState { timetable.schedule.keys.size }
      HorizontalPager(state = pagerState, pageSpacing = 16.dp) { page ->
        val day = DAYS[page]
        val timetableDisplay = remember(timetable, day) { buildTimetableDisplay(day, timetable) }
        Column(
          modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          timetableDisplay.map {
            TimetableItem(it)
          }
        }
      }
    } else {
      Box(Modifier.matchParentSize()) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
      }
    }
  }
}

@Composable
private fun TimetableItem(item: TimetableDisplayEntry) {
  with(item) {
    Card(
      Modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.medium,
      colors = CardDefaults.cardColors()
        .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
      Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(name, fontWeight = FontWeight.Medium)
          Spacer(Modifier.padding(start = 4.dp))
          if (lab) {
            Icon(
              painter = painterResource(R.drawable.science_24px),
              contentDescription = "Lab",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
        Row {
          Text(
            "Period ${if (start == end) start + 1 else "${start + 1} → ${end + 1}"}",
          )
          Text(
            "•",
            modifier = Modifier.padding(horizontal = 6.dp)
          )
          Text(slot)
        }
      }
    }
  }
}
