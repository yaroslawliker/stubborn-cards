package com.yarek.stubborncards.ui.layout

sealed class Page(val route: String) {
    object Home : Page("home")
    object Cards : Page("cards")
    object Learn : Page("learn")
    object AddFlashcard : Page("add_flashcard")

    object SetupPromotionTable : Page("setup_promotion_table")

    object CategoryWords : Page("category_words/{categoryName}") {
        fun createRoute(categoryName: String) = "category_words/$categoryName"
    }

    object CardDetails : Page("card_details/{categoryName}/{cardId}") {
        fun createRoute(categoryName: String, cardId: Long) = "card_details/$categoryName/$cardId"
    }

    object EditCard : Page("edit_card/{cardId}") {
        fun createRoute(cardId: Long) = "edit_card/$cardId"
    }

    object ExerciseSession : Page("exercise_session/{exerciseId}?categoryName={categoryName}") {
        fun createRoute(exerciseId: String, categoryName: String? = null): String {
            return if (categoryName != null) {
                "exercise_session/$exerciseId?categoryName=$categoryName"
            } else {
                "exercise_session/$exerciseId"
            }
        }
    }
}