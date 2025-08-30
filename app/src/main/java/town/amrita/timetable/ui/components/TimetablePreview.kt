package town.amrita.timetable.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import town.amrita.timetable.R
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.models.TimetableDisplayEntry
import town.amrita.timetable.models.WidgetConfig
import town.amrita.timetable.models.buildTimetableDisplay
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.ui.LocalTimetableColors
import town.amrita.timetable.utils.DAYS
import town.amrita.timetable.utils.TODAY
import town.amrita.timetable.utils.longName

@Composable
fun TimetablePreview(modifier: Modifier = Modifier, timetable: Timetable?) {
  val context = LocalContext.current
  val config = context.widgetConfig.data.collectAsState(WidgetConfig())

  Box(modifier) {
    if (timetable != null) {
      val initialPage = if (timetable.schedule.keys.contains(TODAY)) DAYS.indexOf(TODAY) else 0
      val pagerState =
        rememberPagerState(initialPage = initialPage) { timetable.schedule.keys.size }
      HorizontalPager(state = pagerState, pageSpacing = 8.dp) { page ->
        val day = DAYS[page]
        val timetableDisplay = buildTimetableDisplay(day, timetable, config.value.showFreePeriods)
        Column {
          Text(day.longName(), Modifier.padding(start = 4.dp, bottom = 12.dp), fontWeight = FontWeight.Medium)
          if (!timetableDisplay.isEmpty()) {
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
          } else {
            Box(Modifier.fillMaxSize()) {
              Text("Nothing today :)", Modifier.align(Alignment.Center))
            }
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
        .copy(containerColor = LocalTimetableColors.current.behindTimetableItem)
    ) {
      Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = buildAnnotatedString {
              append(name)
              if (lab) {
                append(" ")
                appendInlineContent("labIcon", "[labIcon]")
              }
            },
            inlineContent = mapOf(
              Pair(
                "labIcon",
                InlineTextContent(
                  placeholder = Placeholder(
                    width = 1.em,
                    height = 1.em,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                  )
                ) {
                    Icon(
                      painter = painterResource(R.drawable.science_24px),
                      contentDescription = "Lab",
                      tint = MaterialTheme.colorScheme.primary
                    )
                })
            ), fontWeight = FontWeight.Medium
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            "Period ${if (start == end) start + 1 else "${start + 1} → ${end + 1}"}",
          )
          Text("•")
          Text(slot.toString())
        }
      }
    }
  }
}
