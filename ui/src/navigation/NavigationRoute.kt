package dev.sunriseydy.acgn.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.graphics.vector.ImageVector
import dev.sunriseydy.acgn.interfaces.CommonModuleLocalizable
import dev.sunriseydy.acgn.interfaces.EnumLocalizable

enum class NavigationRoute(val icon: ImageVector) : CommonModuleLocalizable, EnumLocalizable {
    RSS(Icons.Default.Email),
    ANIME(Icons.Default.Email),
}