package me.heftymouse.timetable.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
  val HOME = "home"
  val REGISTRY = "registry"
}

@Composable
fun RootView() {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = Routes.HOME) {
    composable(route = Routes.HOME) {
      HomeScreen()
    }
    composable(route = Routes.REGISTRY) {
      RegistryScreen()
    }
  }
}