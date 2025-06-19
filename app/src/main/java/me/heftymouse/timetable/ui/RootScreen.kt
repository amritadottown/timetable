package me.heftymouse.timetable.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable
object Home
@Serializable
object Registry

@Composable
fun RootScreen() {
  TimetableTheme {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Home) {
      composable<Home> { HomeScreen() }
      composable<Registry> { RegistryScreen() }
    }
  }
}