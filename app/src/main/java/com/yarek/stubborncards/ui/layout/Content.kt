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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yarek.stubborncards.ui.theme.Typography

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
        composable(route=Page.Cards.route) { DummyContent("Cards") }
        composable(route=Page.Home.route) { DummyContent("Home") }
        composable(route=Page.Learn.route) { DummyContent("Learn") }
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