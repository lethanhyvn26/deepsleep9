package com.sleepsounds.storytelling.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import com.sleepsounds.storytelling.R

/**
 * Navigation destinations for the Deep Sleep application graph.
 */
sealed class AppRoute(val route: String) {
    data object Bootstrap : AppRoute(route = "bootstrap")
    data object Onboarding : AppRoute(route = "onboarding")
    data object Home : AppRoute(route = "home")

    data object SceneDetail : AppRoute(route = "scene/{sceneId}") {
        const val SCENE_ID_ARG: String = "sceneId"
        fun withId(sceneId: String): String = "scene/$sceneId"
    }

    data object Sounds : AppRoute(route = "sounds")
    data object Podcard : AppRoute(route = "podcard")

    data object PodcardDetail : AppRoute(route = "podcard/{podcardId}") {
        const val PODCARD_ID_ARG: String = "podcardId"
        fun withId(podcardId: String): String = "podcard/$podcardId"
    }

    data object Tracker : AppRoute(route = "tracker")
    data object Journal : AppRoute(route = "journal")
    data object DreamDictionary : AppRoute(route = "dream_dictionary")
    data object Products : AppRoute(route = "products")
    data object Profile : AppRoute(route = "profile")
    data object Paywall : AppRoute(route = "paywall")
    data object BackgroundSettings : AppRoute(route = "background_settings")

    data object Mixer : AppRoute(route = "mixer?presetId={presetId}") {
        const val PRESET_ID_ARG: String = "presetId"
        fun route(presetId: String? = null): String {
            val cleanPresetId = presetId?.trim().orEmpty()
            return if (cleanPresetId.isEmpty()) "mixer?presetId=" else "mixer?presetId=$cleanPresetId"
        }
    }

    data object Breathing : AppRoute(route = "breathing")
    data object Terms : AppRoute(route = "terms")
    data object MidnightRescue : AppRoute(route = "midnight_rescue")
    data object NowPlaying : AppRoute(route = "now_playing")
}

/**
 * Descriptor for a primary bottom-navigation bar tab.
 */
data class BottomNavItem(
    val route: AppRoute,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenterHighlight: Boolean = false,
) {
    val icon: ImageVector
        get() = selectedIcon
}

/** Root route identifier for the nested bottom-tab navigation graph. */
const val MAIN_GRAPH_ROUTE: String = "main"

private val primaryBottomTabs: List<BottomNavItem> = listOf(
    BottomNavItem(
        route = AppRoute.Home,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    BottomNavItem(
        route = AppRoute.Sounds,
        labelRes = R.string.nav_sounds,
        selectedIcon = Icons.Filled.GraphicEq,
        unselectedIcon = Icons.Outlined.GraphicEq,
    ),
    BottomNavItem(
        route = AppRoute.Tracker,
        labelRes = R.string.nav_tracker,
        selectedIcon = Icons.Filled.Bedtime,
        unselectedIcon = Icons.Outlined.Bedtime,
        isCenterHighlight = true,
    ),
    BottomNavItem(
        route = AppRoute.Journal,
        labelRes = R.string.nav_journal,
        selectedIcon = Icons.Filled.Assessment,
        unselectedIcon = Icons.Outlined.Assessment,
    ),
)

private val shopBottomTab: BottomNavItem = BottomNavItem(
    route = AppRoute.Products,
    labelRes = R.string.nav_products,
    selectedIcon = Icons.Filled.ShoppingBag,
    unselectedIcon = Icons.Outlined.ShoppingBag,
)

private val accountBottomTab: BottomNavItem = BottomNavItem(
    route = AppRoute.Profile,
    labelRes = R.string.nav_profile,
    selectedIcon = Icons.Filled.Person,
    unselectedIcon = Icons.Outlined.Person,
)

/**
 * Builds the ordered list of bottom navigation tabs.
 */
fun bottomNavItems(
    includeProducts: Boolean = true,
): List<BottomNavItem> {
    val tabs = ArrayList<BottomNavItem>(6)
    tabs.addAll(primaryBottomTabs)
    if (includeProducts) {
        tabs.add(shopBottomTab)
    }
    tabs.add(accountBottomTab)
    return tabs
}
