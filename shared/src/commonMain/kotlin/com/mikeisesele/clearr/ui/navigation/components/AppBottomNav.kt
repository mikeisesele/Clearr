package com.mikeisesele.clearr.ui.navigation.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

enum class AppBottomNavItem(
    val label: String,
    val icon: ImageVector
) {
    HOME("Home", Icons.Filled.Home),
    BUDGET("Budget", Icons.Filled.AccountBalanceWallet),
    TODOS("Todos", Icons.Filled.Checklist),
    GOALS("Goals", Icons.Filled.CheckCircle)
}

@Composable
fun AppBottomNav(
    selectedItem: AppBottomNavItem?,
    onSelect: (AppBottomNavItem) -> Unit
) {
    val colors = LocalClearrUiColors.current
    NavigationBar(
        containerColor = colors.surface,
        contentColor = colors.text,
        modifier = Modifier.navigationBarsPadding()
    ) {
        AppBottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = item == selectedItem,
                onClick = { onSelect(item) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.accent,
                    selectedTextColor = colors.accent,
                    unselectedIconColor = colors.muted,
                    unselectedTextColor = colors.muted,
                    indicatorColor = ClearrColors.BrandPrimary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
