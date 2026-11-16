package com.sleepsounds.storytelling.presentation.products

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.rememberAdMobManager
import com.sleepsounds.storytelling.data.remote.ShopCategory
import com.sleepsounds.storytelling.data.remote.ShopProduct
import com.sleepsounds.storytelling.presentation.components.ScreenBackground
import com.sleepsounds.storytelling.presentation.components.TabScreenHeader
import com.sleepsounds.storytelling.ui.theme.AccentTeal
import com.sleepsounds.storytelling.ui.theme.Dimens
import com.sleepsounds.storytelling.ui.theme.GlassOverlay
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.tr

private val ProductCardCornerShape = RoundedCornerShape(Dimens.cardRadius)
private val ProductImageCornerShape = RoundedCornerShape(16.dp)
private val ProductImageBoxSize = 104.dp
@Composable
private fun StoreCategoryTabItem(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onSelect)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) TextPrimary else TextSecondary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .then(if (isSelected) Modifier.fillMaxWidth() else Modifier)
                .background(
                    if (isSelected) TextPrimary else Color.Transparent,
                    RoundedCornerShape(1.dp),
                ),
        )
    }
}
@Composable
private fun StoreCategoryFilterStrip(
    categories: List<ShopCategory>,
    selectedCategoryId: String?,
    onSelectCategory: (String?) -> Unit,
    horizontalInset: Dp = Dimens.screenPadding,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        contentPadding = PaddingValues(horizontal = horizontalInset),
    ) {
        item {
            StoreCategoryTabItem(
                title = stringResource(R.string.filter_all),
                isSelected = selectedCategoryId == null,
                onSelect = { onSelectCategory(null) },
            )
        }
        items(categories, key = { it.id }) { category ->
            StoreCategoryTabItem(
                title = tr(category.label),
                isSelected = selectedCategoryId == category.id,
                onSelect = { onSelectCategory(category.id) },
            )
        }
    }
}
@Composable
private fun StoreProductThumbFallback() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBlueLight),
    )
}
@Composable
private fun StoreProductCard(
    item: ShopProduct,
    onOpenProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val trimmedDesc = remember(item.shortDescription) { item.shortDescription.trim() }
    val currentPrice = remember(item.price) { item.price.trim() }
    val originalPrice = remember(item.compareAtPrice) { item.compareAtPrice.trim() }
    val targetPx = with(LocalDensity.current) { ProductImageBoxSize.roundToPx() }
    val coilRequest = remember(item.imageUrl, targetPx, context) {
        ImageRequest.Builder(context)
            .data(item.imageUrl)
            .size(targetPx)
            .precision(Precision.INEXACT)
            .crossfade(150)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(ProductCardCornerShape)
            .background(SurfaceBlue)
            .border(1.dp, GlassOverlay, ProductCardCornerShape)
            .clickable(onClick = onOpenProduct)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(ProductImageBoxSize)) {
            SubcomposeAsyncImage(
                model = coilRequest,
                contentDescription = item.name,
                modifier = Modifier
                    .matchParentSize()
                    .clip(ProductImageCornerShape)
                    .background(SurfaceBlueLight)
                    .border(1.5.dp, PastelPurple.copy(alpha = 0.45f), ProductImageCornerShape),
                contentScale = ContentScale.Crop,
                loading = { StoreProductThumbFallback() },
                error = { StoreProductThumbFallback() },
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
            )
            if (currentPrice.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = currentPrice,
                        color = AccentTeal,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp,
                    )
                    if (originalPrice.isNotEmpty()) {
                        Text(
                            text = originalPrice,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp,
                            textDecoration = TextDecoration.LineThrough,
                        )
                    }
                }
            }
            if (trimmedDesc.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trimmedDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.products_see_more),
                color = PastelPurple,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
            )
        }
    }
}
/**
 * Curated sleep wellness store tab powered by Firebase Realtime Database (`/store`).
 */
@Composable
fun ProductsScreen(
    tabResetGeneration: Int = 0,
    viewModel: ProductsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeCategoryKey = uiState.selectedCategoryId.orEmpty()
    val scrollGridState = remember(activeCategoryKey, tabResetGeneration) { LazyGridState(0, 0) }
    val appContext = LocalContext.current
    val layoutMetrics = rememberResponsiveLayoutInfo()
    val adMobManager = rememberAdMobManager()

    fun launchExternalProductUrl(targetUrl: String) {
        val openUrl = {
            runCatching {
                appContext.startActivity(Intent(Intent.ACTION_VIEW, targetUrl.toUri()))
            }
            Unit
        }
        val manager = adMobManager
        if (manager != null) {
            manager.onUserAction(onFinished = openUrl)
        } else {
            openUrl()
        }
    }

    ScreenBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            TabScreenHeader(title = stringResource(R.string.products_title))

            if (uiState.categories.isNotEmpty()) {
                StoreCategoryFilterStrip(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onSelectCategory = { catId ->
                        adMobManager?.onUserAction()
                        viewModel.selectCategory(catId)
                    },
                    horizontalInset = layoutMetrics.screenHorizontalPadding,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.products.isEmpty()) {
                AdaptiveBannerAdBar(slotKey = "products_tab_banner")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(layoutMetrics.screenHorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.products_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                val firstTwoRowsCount = layoutMetrics.productGridColumns
                val firstTwoRowsProducts = remember(uiState.products, firstTwoRowsCount) {
                    uiState.products.take(firstTwoRowsCount)
                }
                val remainingProducts = remember(uiState.products, firstTwoRowsCount) {
                    uiState.products.drop(firstTwoRowsCount)
                }

                key(activeCategoryKey) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(layoutMetrics.productGridColumns),
                        state = scrollGridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = layoutMetrics.screenHorizontalPadding,
                            end = layoutMetrics.screenHorizontalPadding,
                            top = 4.dp,
                            bottom = 24.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = firstTwoRowsProducts,
                            key = { "${activeCategoryKey}_${it.id}" },
                            contentType = { "store_product_item" },
                        ) { item ->
                            StoreProductCard(
                                item = item,
                                onOpenProduct = { launchExternalProductUrl(item.link) },
                            )
                        }

                        item(
                            key = "${activeCategoryKey}_products_banner_row_2_3",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            AdaptiveBannerAdBar(slotKey = "products_tab_banner")
                        }

                        items(
                            items = remainingProducts,
                            key = { "${activeCategoryKey}_${it.id}" },
                            contentType = { "store_product_item" },
                        ) { item ->
                            StoreProductCard(
                                item = item,
                                onOpenProduct = { launchExternalProductUrl(item.link) },
                            )
                        }
                    }
                }
            }
        }
    }
}
