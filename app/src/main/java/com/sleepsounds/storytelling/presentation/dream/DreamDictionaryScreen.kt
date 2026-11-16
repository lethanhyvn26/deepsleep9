package com.sleepsounds.storytelling.presentation.dream

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.rememberAdMobManager
import com.sleepsounds.storytelling.data.local.DreamInterpretationCatalog
import com.sleepsounds.storytelling.data.local.DreamSymbolItem
import com.sleepsounds.storytelling.data.local.localized
import com.sleepsounds.storytelling.presentation.components.ScreenBackground
import com.sleepsounds.storytelling.ui.theme.LocalAppLanguage
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.statusAndCutoutPadding
import com.sleepsounds.storytelling.ui.theme.tr

@Composable
private fun DreamMeaningSectionBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceBlue.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PastelPurple,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 20.sp,
        )
    }
}
@Composable
private fun DreamInterpretationDetailModal(
    symbol: DreamSymbolItem,
    onClose: () -> Unit,
) {
    val currentLang = LocalAppLanguage.current
    val localizedSymbol = symbol.localized()
    val localizedCategory = DreamInterpretationCatalog.localizeCategory(symbol.category, currentLang)
    Column(
        modifier = Modifier
            .responsiveContentWidth(640.dp)
            .padding(top = 24.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MidnightBlue)
            .border(
                1.dp,
                Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            )
            .clickable(enabled = false) {}
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 14.dp, bottom = 28.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepSleepBlue.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${symbol.emoji}  ${localizedCategory.uppercase()}",
                    color = DeepSleepBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.action_close),
                    tint = TextSecondary,
                )
            }
        }

        Text(
            text = localizedSymbol.title,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = localizedSymbol.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = PastelPurple,
        )

        Spacer(modifier = Modifier.height(18.dp))

        DreamMeaningSectionBox(
            icon = Icons.Filled.AutoAwesome,
            title = tr("Dream Meaning", "Significado do Sonho", "Ý nghĩa giấc mơ"),
            body = localizedSymbol.summary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        DreamMeaningSectionBox(
            icon = Icons.Filled.Psychology,
            title = tr("Psychological Insight", "Visão Psicológica", "Góc nhìn tâm lý học"),
            body = localizedSymbol.psychologicalInsight,
        )

        Spacer(modifier = Modifier.height(12.dp))

        DreamMeaningSectionBox(
            icon = Icons.Filled.Spa,
            title = tr("Bedtime Reflection", "Reflexão antes de Dormir", "Lời khuyên trước khi ngủ"),
            body = localizedSymbol.calmingTip,
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepSleepBlue,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = tr("Done Reading", "Concluir Leitura", "Đã hiểu"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
@Composable
private fun DreamInterpretationSymbolCard(
    symbol: DreamSymbolItem,
    onClick: () -> Unit,
) {
    val currentLang = LocalAppLanguage.current
    val localizedSymbol = symbol.localized()
    val localizedCategory = DreamInterpretationCatalog.localizeCategory(symbol.category, currentLang)
    val badgeGradient = remember(symbol.category) {
        when (symbol.category) {
            "Element" -> listOf(Color(0xFF2563EB), Color(0xFF1E3A8A))
            "Body" -> listOf(Color(0xFF7C3AED), Color(0xFF4C1D95))
            "Action" -> listOf(Color(0xFFDB2777), Color(0xFF831843))
            else -> listOf(Color(0xFF0D9488), Color(0xFF115E59))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceBlue.copy(alpha = 0.88f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(badgeGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = symbol.emoji,
                fontSize = 24.sp,
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = localizedSymbol.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = localizedCategory,
                        color = PastelPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = localizedSymbol.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
        )
    }
}
@Composable
fun DreamDictionaryScreen(
    initialDreamId: String? = null,
    onBack: () -> Unit,
) {
    val allSymbols = remember { DreamInterpretationCatalog.SYMBOLS }
    val categories = remember {
        listOf("All") + allSymbols.map { it.category }.distinct()
    }

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var activeSymbol by remember { mutableStateOf<DreamSymbolItem?>(null) }

    BackHandler(enabled = activeSymbol != null) {
        if (!initialDreamId.isNullOrBlank()) {
            onBack()
        } else {
            activeSymbol = null
        }
    }

    LaunchedEffect(initialDreamId) {
        if (!initialDreamId.isNullOrBlank()) {
            activeSymbol = DreamInterpretationCatalog.getById(initialDreamId)
        }
    }

    val currentLang = LocalAppLanguage.current
    val filteredSymbols = remember(allSymbols, selectedCategory, searchQuery, currentLang) {
        allSymbols.filter { item ->
            val loc = DreamInterpretationCatalog.localizeSymbol(item, currentLang)
            val matchesCategory = selectedCategory == "All" ||
                item.category.equals(selectedCategory, ignoreCase = true)
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                item.title.contains(q, ignoreCase = true) ||
                item.subtitle.contains(q, ignoreCase = true) ||
                item.summary.contains(q, ignoreCase = true) ||
                loc.title.contains(q, ignoreCase = true) ||
                loc.subtitle.contains(q, ignoreCase = true) ||
                loc.summary.contains(q, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val layout = rememberResponsiveLayoutInfo()
    val adMobManager = rememberAdMobManager()
    ScreenBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier.responsiveContentWidth(760.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.dream_analysis_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(R.string.dream_analysis_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = layout.screenHorizontalPadding),
                        placeholder = {
                            Text(
                                text = tr(
                                    "Search dream symbol (e.g. Fire, Falling, Flying)...",
                                    "Buscar símbolo do sonho (ex.: Fogo, Cair, Voar)...",
                                    "Tìm kiếm giấc mơ (VD: Ngọn lửa, Rụng răng, Bay lượn)...",
                                ),
                                color = TextSecondary.copy(alpha = 0.75f),
                                fontSize = 14.sp,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceBlue.copy(alpha = 0.85f),
                            unfocusedContainerColor = SurfaceBlue.copy(alpha = 0.65f),
                            focusedBorderColor = DeepSleepBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = layout.screenHorizontalPadding),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(categories, key = { it }) { category ->
                            val isSelected = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) DeepSleepBlue else SurfaceBlueLight.copy(alpha = 0.75f),
                                    )
                                    .clickable {
                                        if (selectedCategory != category) {
                                            selectedCategory = category
                                            adMobManager?.onUserAction()
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = DreamInterpretationCatalog.localizeCategory(category, currentLang),
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val listState = remember(selectedCategory, searchQuery) { LazyListState(0, 0) }
                    key(selectedCategory, searchQuery) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = layout.screenHorizontalPadding,
                                end = layout.screenHorizontalPadding,
                                top = 4.dp,
                                bottom = 24.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            val firstTwoRows = filteredSymbols.take(2)
                            val remainingRows = filteredSymbols.drop(2)

                            items(firstTwoRows, key = { "${selectedCategory}_${it.id}" }) { symbol ->
                                DreamInterpretationSymbolCard(
                                    symbol = symbol,
                                    onClick = {
                                        val manager = adMobManager
                                        if (manager != null) {
                                            manager.onUserAction { activeSymbol = symbol }
                                        } else {
                                            activeSymbol = symbol
                                        }
                                    },
                                )
                            }

                            item(key = "dream_dictionary_banner_row_2_3") {
                                AdaptiveBannerAdBar(slotKey = "dream_dictionary_banner")
                            }

                            items(remainingRows, key = { "${selectedCategory}_${it.id}" }) { symbol ->
                                DreamInterpretationSymbolCard(
                                    symbol = symbol,
                                    onClick = {
                                        val manager = adMobManager
                                        if (manager != null) {
                                            manager.onUserAction { activeSymbol = symbol }
                                        } else {
                                            activeSymbol = symbol
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = activeSymbol != null,
                enter = fadeIn(tween(200)) + slideInVertically(tween(240)) { it / 4 },
                exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 4 },
            ) {
                activeSymbol?.let { selected ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable { activeSymbol = null }
                            .statusAndCutoutPadding(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        DreamInterpretationDetailModal(
                            symbol = selected,
                            onClose = { activeSymbol = null },
                        )
                    }
                }
            }
        }
    }
}
