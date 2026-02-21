package com.yarek.stubborncards.ui.layout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLayout() {

    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?:""

    Scaffold(
        topBar = { Header(
            pageToHeader(currentRoute)
        ) },
        bottomBar = { Navbar(navController) }
    ) { innerPadding ->
        Content(navController, innerPadding)
    }
}

fun pageToHeader(page: String): String {
    return when (page) {
        Page.Home.route -> "Home"
        Page.Cards.route -> "Flash-cards"
        Page.Learn.route -> "Exercises"
        else -> "Stubborn cards"
    }
}