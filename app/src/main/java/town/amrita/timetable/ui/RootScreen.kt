package town.amrita.timetable.ui

import android.net.Uri
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import town.amrita.timetable.R
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.registry.TimetableSpec
import town.amrita.timetable.ui.components.TooltipContainer
import town.amrita.timetable.ui.picker.ConfigPickerScreen
import town.amrita.timetable.ui.picker.LocalPickerScreen
import town.amrita.timetable.ui.picker.TimetablePickerScreen
import town.amrita.timetable.ui.picker.TimetableShareScreen
import town.amrita.timetable.utils.updateTimetableFromRegistry
import town.amrita.timetable.utils.updateTimetableFromUri

sealed class Route {
  data object RegistryRoute : Route()
  data object LocalPickerRoute : Route()
  data class ShareScreenRoute(val uri: Uri?) : Route()
  data class ConfigPickerRoute(
    val timetable: Timetable,
    val spec: TimetableSpec? = null,
    val uri: Uri? = null
  ) : Route()

  data object SettingsRoute : Route()
}

val LocalGlobalActions = staticCompositionLocalOf<@Composable (RowScope.() -> Unit)> { { } }

@Composable
fun RootScreen(startRoute: Route = Route.RegistryRoute) {
  val context = LocalContext.current

  TimetableTheme {
    val backStack = remember { mutableStateListOf<Any>(startRoute) }
    val thingy =
      PathInterpolator(PathParser.createPathFromPathData("M 0,0 C 0.05, 0, 0.133333, 0.06, 0.166666, 0.4 C 0.208333, 0.82, 0.25, 1, 1, 1"))
    val materialEasing = Easing { thingy.getInterpolation(it) }
    val pxValue = with(LocalDensity.current) { 96.dp.roundToPx() }

    val globalActions: @Composable (RowScope.() -> Unit) = {
      if (backStack.last() == Route.RegistryRoute) {
        var expanded by remember { mutableStateOf(false) }
        Box {
          TooltipContainer(tooltipContent = "More") {
            IconButton(onClick = { expanded = !expanded }) {
              Icon(
                painter = painterResource(R.drawable.more_vert_24px),
                contentDescription = "More"
              )
            }
          }
          DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
              text = { Text("Settings") },
              onClick = {
                expanded = false
                if (backStack.last() != Route.SettingsRoute)
                  backStack.add(Route.SettingsRoute)
              }
            )
          }
        }
      }
    }

    CompositionLocalProvider(LocalGlobalActions provides globalActions) {
      NavDisplay(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
          rememberSaveableStateHolderNavEntryDecorator(),
          rememberViewModelStoreNavEntryDecorator(),
          rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
          entry<Route.RegistryRoute> {
            TimetablePickerScreen(
              goToLocalPicker = { backStack.add(Route.LocalPickerRoute) },
              goToConfig = { timetable, spec -> backStack.add(Route.ConfigPickerRoute(timetable, spec = spec)) }
            )
          }
          entry<Route.LocalPickerRoute> {
            LocalPickerScreen(
              goToConfig = { timetable, uri -> backStack.add(Route.ConfigPickerRoute(timetable, uri = uri)) }
            )
          }
          entry<Route.ShareScreenRoute> { route ->
            TimetableShareScreen(route.uri, goToConfig = { timetable, uri -> backStack.add(Route.ConfigPickerRoute(timetable, uri = uri)) })
          }
          entry<Route.ConfigPickerRoute> { route ->
            ConfigPickerScreen(
              timetable = route.timetable,
              onConfigSelected = { config ->
                withContext(Dispatchers.IO) {
                  if (route.spec != null) {
                    context.updateTimetableFromRegistry(route.spec, config)
                  } else if (route.uri != null) {
                    context.updateTimetableFromUri(route.uri, config)
                  }
                }
              },
            )
          }
          entry<Route.SettingsRoute> {
            SettingsScreen()
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
}
