package town.amrita.timetable.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import town.amrita.timetable.ui.LocalGlobalActions

val LocalSnackbarState = compositionLocalOf<SnackbarHostState> { error("No snackbar state") }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TimetableScaffold(
  title: String,
  actions: @Composable (RowScope.() -> Unit) = LocalGlobalActions.current,
  content: @Composable () -> Unit,
) {
  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    topBar = @Composable {
      LargeTopAppBar(
        title = @Composable { Text(title, style = MaterialTheme.typography.displaySmall) },
        colors = TopAppBarDefaults.topAppBarColors()
          .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 8.dp, right = 8.dp)),
        actions = actions
      )
    },
    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.add(
      WindowInsets(
        left = 24.dp,
        right = 24.dp
      )
    ),
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
  ) { innerPadding ->
    CompositionLocalProvider(LocalSnackbarState provides snackbarHostState) {
      Box(Modifier.padding(innerPadding)) {
        content()
      }
    }
  }
}