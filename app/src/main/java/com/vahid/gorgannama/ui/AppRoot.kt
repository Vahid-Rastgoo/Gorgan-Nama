package com.vahid.gorgannama.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.vahid.gorgannama.data.Store

private class TabItem(val title: String, val icon: ImageVector)

@Composable
fun AppRoot() {
    val top = Nav.stack.last()
    BackHandler(enabled = Nav.stack.size > 1 || Nav.tab != 0) {
        if (Nav.stack.size > 1) Nav.pop() else Nav.tab = 0
    }
    LaunchedEffect(Unit) { Store.autoSync() }

    Box(Modifier.fillMaxSize()) {
        // main tabs stay alive underneath, so search text and filters survive opening a shop
        MainScaffold()
        if (top !is Route.Main) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                key(top) {
                    when (top) {
                        is Route.Detail -> DetailScreen(top.id)
                        is Route.EditBusiness -> BusinessEditor(top.id, top.fromLeadId)
                        is Route.EditLead -> LeadEditor(top.id)
                        Route.Admin -> AdminScreen(showBack = true)
                        Route.Main -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScaffold() {
    val showAdmin = Store.settings.showAdminTab || Store.adminUnlocked
    val tabs = buildList {
        add(TabItem("خانه", Icons.Filled.Home))
        add(TabItem("جستجو", Icons.Filled.Search))
        add(TabItem("تخفیف‌ها", Icons.Filled.LocalOffer))
        add(TabItem("نشان‌شده", Icons.Filled.Favorite))
        if (showAdmin) add(TabItem("مدیریت", Icons.Filled.AdminPanelSettings))
    }
    val tab = Nav.tab.coerceIn(0, tabs.size - 1)
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { Nav.tab = i },
                        icon = { Icon(t.icon, null) },
                        label = { Text(t.title) }
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> HomeScreen()
                1 -> SearchScreen()
                2 -> OffersScreen()
                3 -> FavoritesScreen()
                else -> AdminScreen(showBack = false)
            }
        }
    }
}
