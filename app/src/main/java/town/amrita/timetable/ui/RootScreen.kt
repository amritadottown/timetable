package town.amrita.timetable.ui

import android.view.animation.PathInterpolator
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import town.amrita.timetable.R

data object HomeRoute
data object RegistryRoute
data object AboutRoute

@Composable
fun RootScreen() {
  TimetableTheme {
    val backStack = remember { mutableStateListOf<Any>(RegistryRoute) }
    val thingy =
      PathInterpolator(PathParser.createPathFromPathData("M 0,0 C 0.05, 0, 0.133333, 0.06, 0.166666, 0.4 C 0.208333, 0.82, 0.25, 1, 1, 1"))
    val materialEasing = Easing { thingy.getInterpolation(it) }
    val pxValue = with(LocalDensity.current) { 96.dp.roundToPx() }

    val globalActions: @Composable (RowScope.() -> Unit) = {
      var expanded by remember { mutableStateOf(false) }
      Box {
        IconButton(onClick = {
          expanded = !expanded
        }) {
          Icon(painter = painterResource(R.drawable.more_vert_24px), contentDescription = "More")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          DropdownMenuItem(
            text = { Text("About") },
            onClick = { backStack.add(AboutRoute) }
          )
        }
      }
    }

    NavDisplay(
      modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
      backStack = backStack,
      onBack = { backStack.removeLastOrNull() },
      entryDecorators = listOf(
        rememberSceneSetupNavEntryDecorator(),
        rememberSavedStateNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator()
      ),
      entryProvider = entryProvider {
        entry<HomeRoute> {
          HomeScreen(openRegistryPage = {
            backStack.add(RegistryRoute)
          })
        }
        entry<RegistryRoute> {
          TimetablePickerScreen(
            globalActions = globalActions
          )
        }
        entry<AboutRoute> {
          AboutScreen()
        }
      },
      transitionSpec = {
        ContentTransform(
          slideIn(
            animationSpec = tween(
              durationMillis = 450,
              easing = materialEasing
            )
          ) { _ -> IntOffset(pxValue, 0) },
          fadeOut(
            animationSpec = tween(
              durationMillis = 83,
              easing = LinearEasing,
              delayMillis = 50
            )
          )
                  + slideOut(
            animationSpec = tween(
              durationMillis = 450,
              easing = materialEasing
            )
          ) { _ -> IntOffset(-pxValue, 0) },
          -1f
        )
      },
      popTransitionSpec = {
        ContentTransform(
          slideIn(
            animationSpec = tween(
              durationMillis = 450,
              easing = materialEasing
            )
          ) { _ -> IntOffset(-pxValue, 0) },
          fadeOut(
            animationSpec = tween(
              durationMillis = 83,
              easing = LinearEasing,
              delayMillis = 35
            )
          )
                  + slideOut(
            animationSpec = tween(
              durationMillis = 450,
              easing = materialEasing
            )
          ) { _ -> IntOffset(pxValue, 0) },
          -1f
        )
      },
//        predictivePopTransitionSpec =
    )
  }
}