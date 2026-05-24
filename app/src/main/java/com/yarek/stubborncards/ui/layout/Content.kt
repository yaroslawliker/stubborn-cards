package com.yarek.stubborncards.ui.layout

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yarek.stubborncards.ui.page.AddFlashcardPage
import com.yarek.stubborncards.ui.page.CardsPage
import com.yarek.stubborncards.ui.page.EditCardPage
import com.yarek.stubborncards.ui.page.ExerciseSessionPage
import com.yarek.stubborncards.ui.page.ExercisesHubPage
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.page.ProgressLevelWordsPage
import com.yarek.stubborncards.ui.page.UnitCardPage

@Composable
fun Content(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(navController=navController,
        startDestination=Page.Home.route,
        modifier = Modifier.padding(innerPadding),
        enterTransition = {
            fadeIn(animationSpec = tween (150))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(150))
        }
    ) {
        composable(route=Page.Cards.route) { CardsPage(navController) }
        composable(route=Page.Home.route) { DummyContent("Home") }
        composable(route=Page.Learn.route) { ExercisesHubPage(navController) }
        composable(route=Page.AddFlashcard.route) { AddFlashcardPage() }

        composable(
            route = Page.CategoryWords.route,
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) {
            ProgressLevelWordsPage(navController)
        }

        composable(
            route = Page.CardDetails.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) {
            UnitCardPage(navController)
        }

        composable(
            route = Page.EditCard.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) {
            EditCardPage(onNavigateBack = { navController.navigateUp() })
        }

        composable(
            route = Page.ExerciseSession.route,
            arguments = listOf(
                navArgument("exerciseId") { type = NavType.StringType },
                navArgument("categoryName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ExerciseSessionPage(navController = navController)
        }
    }
}

@Composable
fun DummyContent(msg: String) {
    Text(msg,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight(Alignment.CenterVertically),
        textAlign = TextAlign.Center,

        fontSize = Typography.titleLarge.fontSize
    )
}