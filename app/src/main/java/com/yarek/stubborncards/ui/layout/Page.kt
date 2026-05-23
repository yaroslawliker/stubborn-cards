package com.yarek.stubborncards.ui.layout

sealed class Page(val route: String) {
    object Home : Page("home")
    object Cards : Page("cards")
    object Learn : Page("learn")
    object AddFlashcard : Page("add_flashcard")

    object CategoryWords : Page("category_words/{categoryName}") {
        fun createRoute(categoryName: String) = "category_words/$categoryName"
    }
}