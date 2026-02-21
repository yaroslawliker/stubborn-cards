package com.yarek.stubborncards.ui.layout

sealed class Page(val route: String) {
    object Home : Page("home")
    object Cards : Page("cards")
    object Learn : Page("learn")
}