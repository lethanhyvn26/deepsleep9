package com.sleepsounds.storytelling.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.data.remote.ShopCatalogRepository
import com.sleepsounds.storytelling.data.remote.ShopCategory
import com.sleepsounds.storytelling.data.remote.ShopProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel filtering enabled store products by category from [ShopCatalogRepository].
 */
@HiltViewModel
class ProductsViewModel @Inject constructor(
    shopCatalogRepository: ShopCatalogRepository,
) : ViewModel() {

    private val chosenCategoryId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProductsUiState> = combine(
        shopCatalogRepository.shopCatalog,
        chosenCategoryId,
    ) { storeCatalog, activeCategoryId ->
        val selectableCategories = storeCatalog.visibleCategories().filterNot { it.isAllCategory }
        val activeProducts = storeCatalog.enabledProducts
        val validCategoryId = activeCategoryId?.takeIf { filterId ->
            selectableCategories.any { it.id.equals(filterId, ignoreCase = true) }
        }
        val matchingProducts = if (validCategoryId == null) {
            activeProducts
        } else {
            activeProducts.filter { it.categoryId.equals(validCategoryId, ignoreCase = true) }
        }
        ProductsUiState(
            products = matchingProducts,
            categories = selectableCategories,
            selectedCategoryId = validCategoryId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductsUiState())

    fun selectCategory(categoryId: String?) {
        chosenCategoryId.value = categoryId
    }
}
/**
 * UI state for the sleep wellness store product showcase.
 */
data class ProductsUiState(
    val products: List<ShopProduct> = emptyList(),
    val categories: List<ShopCategory> = emptyList(),
    val selectedCategoryId: String? = null,
)
