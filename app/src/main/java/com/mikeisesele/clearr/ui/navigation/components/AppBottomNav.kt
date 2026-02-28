package com.mikeisesele.clearr.ui.navigation.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

internal enum class AppBottomNavItem(
    val label: String,
    val icon: ImageVector
) {
    REMITTANCE("Remittance", Icons.Filled.Payments),
    BUDGET("Budget", Icons.Filled.AccountBalanceWallet),
    TODOS("Todos", Icons.Filled.Checklist),
    GOALS("Goals", Icons.Filled.CheckCircle),
    SETTINGS("Settings", Icons.Filled.Settings)
}

@Composable
internal fun AppBottomNav(
    selectedItem: AppBottomNavItem?,
    onSelect: (AppBottomNavItem) -> Unit
) {
    val colors = LocalDuesColors.current
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
