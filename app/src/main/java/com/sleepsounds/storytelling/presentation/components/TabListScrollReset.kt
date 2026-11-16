package com.sleepsounds.storytelling.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

private const val UNSET_TAB_RESET = -1
internal object UnspecifiedCategory
@Composable
fun ResetScrollOnTabOrCategoryChange(
    tabResetGeneration: Int,
    categoryKey: Any? = UnspecifiedCategory,
    scrollToTop: () -> Unit,
) {
    val lastAppliedReset = rememberSaveable { mutableIntStateOf(UNSET_TAB_RESET) }
    val previousReset = lastAppliedReset.intValue
    if (tabResetGeneration != previousReset) {
        lastAppliedReset.intValue = tabResetGeneration
        if (!(previousReset == UNSET_TAB_RESET && tabResetGeneration == 0)) {
            scrollToTop()
        }
    }

    val trackCategory = categoryKey !== UnspecifiedCategory
    val previousCategory = remember { mutableStateOf(categoryKey) }
    if (trackCategory && previousCategory.value != categoryKey) {
        previousCategory.value = categoryKey
        scrollToTop()
    }
}
