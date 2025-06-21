package me.heftymouse.timetable.ui

import android.view.animation.PathInterpolator
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay

data object Home
data object Registry

@Composable
fun RootScreen() {
  TimetableTheme {
    val backStack = remember { mutableStateListOf<Any>(Home) }
    val context = LocalContext.current
    val thingy =
      PathInterpolator(PathParser.createPathFromPathData("M 0,0 C 0.05, 0, 0.133333, 0.06, 0.166666, 0.4 C 0.208333, 0.82, 0.25, 1, 1, 1"))
    val materialEasing = Easing { thingy.getInterpolation(it) }
    val pxValue = with(LocalDensity.current) { 96.dp.roundToPx() }

    NavDisplay(
      modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
      backStack = backStack,
      onBack = { backStack.removeLastOrNull() },
      entryProvider = entryProvider {
        entry<Home> {
          HomeScreen(openRegistryPage = {
            backStack.add(Registry)
          })
        }
        entry<Registry> { RegistryScreen(goBack = { if (backStack.count() > 1) backStack.removeLastOrNull() }) }
        entry<Unit> { Text("how?") }
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