package com.equationl.wxsteplog.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.equationl.wxsteplog.constants.Route
import com.equationl.wxsteplog.ui.view.home.screen.HomeScreen
import com.equationl.wxsteplog.ui.view.statistics.screen.StatisticsScreen

val LocalNavController = staticCompositionLocalOf<NavHostController> { error("No NavController provided") }

@Composable
fun HomeNavHost(
    startDestination: String = Route.HOME
) {
    CompositionLocalProvider(
        LocalNavController provides rememberNavController(),
    ) {
        NavHost(navController = LocalNavController.current, startDestination) {
            composable(Route.HOME) {
                HomeScreen()
            }

            composable(Route.STATISTIC) {
                StatisticsScreen()
            }
        }
    }
}