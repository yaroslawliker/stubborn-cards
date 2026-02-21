package com.yarek.stubborncards.ui.layout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yarek.stubborncards.ui.theme.Typography



private class NavbarItem(
    val route: String,
    val icon: ImageVector,
)


@Composable
fun Navbar(navController: NavHostController) {

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route


    val items = listOf<NavbarItem>(
        NavbarItem(Page.Cards.route, Icons.Default.Add),
        NavbarItem(Page.Home.route, Icons.Default.Home),
        NavbarItem(Page.Learn.route, Icons.Default.Check)
    )

    NavigationBar {
        items.map {
            NavigationBarItem(
                selected = currentRoute == it.route,
                onClick = { navController.navigate(it.route) },
                icon = {
                    Icon(
                        imageVector = it.icon,
                        contentDescription = "Open ${it.route}"
                    )
                },
                label = {
                    Text(
                        it.route.capitalize(),
                        fontSize = Typography.titleLarge.fontSize
                    )
                }
            )
        }
    }
}