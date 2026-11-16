package com.sleepsounds.storytelling.presentation.paywall

import android.widget.Toast
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.RewardedAdFreePassCard
import com.sleepsounds.storytelling.ads.findActivity
import com.sleepsounds.storytelling.data.billing.BillingEvent
import com.sleepsounds.storytelling.data.billing.PremiumPlan
import com.sleepsounds.storytelling.presentation.components.ScreenBackground
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.PremiumGold
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.tr

@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    onOpenTerms: () -> Unit = {},
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current ?: remember(context) { context.findActivity() }

    val msgPurchaseSuccess = tr(
        "Welcome to Somni VIP! All sounds, stories, and ad-free mode are unlocked.",
        "Bem-vindo ao Somni VIP! Todos os sons, histórias e modo sem anúncios desbloqueados.",
        "Chào mừng bạn đến với Somni VIP! Đã mở khóa toàn bộ âm thanh, truyện và xóa quảng cáo.",
    )
    val msgPurchaseRestored = tr(
        "Your VIP subscription has been restored.",
        "Sua assinatura VIP foi restaurada.",
        "Đã khôi phục gói đăng ký VIP của bạn.",
    )
    val msgNoPurchaseFound = tr(
        "No active VIP purchase found on this Google Play account.",
        "Nenhuma compra VIP ativa encontrada nesta conta do Google Play.",
        "Không tìm thấy gói VIP đang hoạt động trên tài khoản Google Play này.",
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                BillingEvent.PurchaseSucceeded -> {
                    Toast.makeText(context, msgPurchaseSuccess, Toast.LENGTH_LONG).show()
                    onBack()
                }
                BillingEvent.PurchaseRestored -> {
                    Toast.makeText(context, msgPurchaseRestored, Toast.LENGTH_LONG).show()
                    onBack()
                }
                BillingEvent.NoPurchasesFound -> {
                    Toast.makeText(context, msgNoPurchaseFound, Toast.LENGTH_SHORT).show()
                }
                BillingEvent.PurchaseCancelled -> Unit
                is BillingEvent.BillingError -> {
                    if (event.debugMessage.isNotBlank()) {
                        Toast.makeText(context, event.debugMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val features = listOf(
        tr(
            "Unlock all 70+ ambient sounds, binaural beats & healing music mixes",
            "Desbloqueie mais de 70 sons ambientes, batidas binaurais e músicas curativas",
            "Mở khóa toàn bộ 70+ âm thanh tự nhiên, sóng não & nhạc chữa lành",
        ),
        tr(
            "Full access to all bedtime stories & guided sleep meditations",
            "Acesso total a todas as histórias para dormir e meditações guiadas",
            "Truy cập không giới hạn toàn bộ kho truyện ngủ & thiền dẫn giấc ngủ",
        ),
        tr(
            "100% Ad-Free experience — zero banner, interstitial, or app-open ads",
            "Experiência 100% Sem Anúncios — sem banners ou interrupções",
            "Trải nghiệm Không Quảng Cáo 100% — xóa hoàn toàn mọi quảng cáo",
        ),
        tr(
            "Unlimited custom sound mixes, smart alarm & full sleep stage analytics",
            "Mixes personalizados ilimitados, alarme inteligente e análise completa do sono",
            "Tạo bản phối không giới hạn, báo thức thông minh & phân tích giấc ngủ chuyên sâu",
        ),
    )

    ScreenBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .responsiveContentWidth(600.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(key = "paywall_top_bar") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = PremiumGold,
                                modifier = Modifier.size(26.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SOMNI VIP",
                                color = PremiumGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }

                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SurfaceBlueLight.copy(alpha = 0.8f)),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = TextPrimary,
                            )
                        }
                    }
                }

                item(key = "paywall_hero") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = tr(
                                "Sleep Deeper Every Night with Somni VIP",
                                "Durma Mais Profundamente Todas as Noites com Somni VIP",
                                "Ngủ Sâu Trọn Vẹn Mỗi Đêm Cùng Somni VIP",
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tr(
                                "Choose a VIP plan below or watch 20 rewarded ads to unlock 3 hours of Ad-Free & Full VIP for free.",
                                "Escolha um plano VIP abaixo ou assista a 20 anúncios para ganhar 3h Sem Anúncios e VIP grátis.",
                                "Chọn gói VIP bên dưới hoặc xem đủ 20 quảng cáo thưởng để mở khóa miễn phí 3 giờ Không Quảng Cáo & Toàn Bộ VIP.",
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                item(key = "paywall_features") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceBlue.copy(alpha = 0.88f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        features.forEach { featureText ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PremiumGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = PremiumGold,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                                Text(
                                    text = featureText,
                                    color = TextPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 19.sp,
                                )
                            }
                        }
                    }
                }

                item(key = "paywall_rewarded_pass_card") {
                    RewardedAdFreePassCard(
                        adMobManager = viewModel.adMobManager,
                        onOpenPaywall = {},
                        isInsidePaywall = true,
                    )
                }

                item(key = "paywall_inline_banner") {
                    AdaptiveBannerAdBar(
                        adMobManager = viewModel.adMobManager,
                        slotKey = "paywall_screen_banner",
                    )
                }

                item(key = "paywall_plans") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = tr(
                                "CHOOSE YOUR VIP PLAN (CLEAR & TRANSPARENT)",
                                "ESCOLHA SEU PLANO VIP (CLARO E TRANSPARENTE)",
                                "CHỌN GÓI VIP PHÙ HỢP (MINH BẠCH · DỄ HIỂU)",
                            ),
                            color = PastelPurple,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                        )

                        PlanOptionCard(
                            title = tr("Yearly VIP", "VIP Anual", "VIP Hàng Năm"),
                            subtitle = tr(
                                "Billed once a year · Best savings for 365 nights of deep sleep",
                                "Cobrado uma vez por ano · Maior economia para 365 noites",
                                "Thanh toán 1 lần/năm · Tiết kiệm nhất cho 365 đêm ngon giấc",
                            ),
                            highlightNote = tr(
                                "✓ Cancel anytime on Google Play · No commitment",
                                "✓ Cancele quando quiser no Google Play · Sem fidelidade",
                                "✓ Mua & hủy bất kỳ lúc nào trên Google Play · Không ràng buộc",
                            ),
                            badge = tr("MOST POPULAR · SAVE 58%", "MAIS POPULAR · ECONOMIZE 58%", "PHỔ BIẾN NHẤT · TIẾT KIỆM 58%"),
                            priceText = "${state.planPrices[PremiumPlan.YEARLY] ?: PremiumPlan.YEARLY.fallbackPrice} / ${tr("year", "ano", "năm")}",
                            selected = state.selectedPlan == PremiumPlan.YEARLY,
                            onClick = { viewModel.selectPlan(PremiumPlan.YEARLY) },
                        )

                        PlanOptionCard(
                            title = tr("Monthly VIP", "VIP Mensal", "VIP Hàng Tháng"),
                            subtitle = tr(
                                "Pay month-by-month · Great for short-term flexibility",
                                "Pague mês a mês · Ideal para flexibilidade de curto prazo",
                                "Thanh toán từng tháng · Linh hoạt trải nghiệm ngắn hạn",
                            ),
                            highlightNote = tr(
                                "✓ Cancel anytime with 1 tap · No lock-in contract",
                                "✓ Cancele com 1 toque · Sem contrato de permanência",
                                "✓ Hủy gia hạn bất kỳ lúc nào chỉ với 1 chạm · Không phí hủy",
                            ),
                            badge = tr("FLEXIBLE PLAN", "PLANO FLEXÍVEL", "LINH HOẠT NHẤT"),
                            priceText = "${state.planPrices[PremiumPlan.MONTHLY] ?: PremiumPlan.MONTHLY.fallbackPrice} / ${tr("month", "mês", "tháng")}",
                            selected = state.selectedPlan == PremiumPlan.MONTHLY,
                            onClick = { viewModel.selectPlan(PremiumPlan.MONTHLY) },
                        )

                        PlanOptionCard(
                            title = tr("Lifetime VIP", "VIP Vitalício", "VIP Trọn Đời"),
                            subtitle = tr(
                                "One-time payment · Own full VIP access permanently forever",
                                "Pagamento único · Acesso VIP completo para sempre",
                                "Chỉ thanh toán 1 lần duy nhất · Sở hữu VIP vĩnh viễn trọn đời",
                            ),
                            highlightNote = tr(
                                "✓ Never billed again · Zero recurring subscription fees",
                                "✓ Nunca mais seja cobrado · Sem mensalidades recorrentes",
                                "✓ Không bao giờ thu thêm phí · Không gia hạn định kỳ",
                            ),
                            badge = tr("BEST DEAL · PAY ONCE", "MELHOR OFERTA · PAGUE 1 VEZ", "ƯU ĐÃI TỐT NHẤT · TRẢ 1 LẦN"),
                            priceText = state.planPrices[PremiumPlan.LIFETIME] ?: PremiumPlan.LIFETIME.fallbackPrice,
                            selected = state.selectedPlan == PremiumPlan.LIFETIME,
                            onClick = { viewModel.selectPlan(PremiumPlan.LIFETIME) },
                        )
                    }
                }

                item(key = "paywall_cta") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            onClick = {
                                if (activity != null) {
                                    viewModel.purchaseSelectedPlan(activity)
                                }
                            },
                            enabled = !state.isBillingBusy && activity != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PremiumGold,
                                contentColor = MidnightBlue,
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                        ) {
                            if (state.isBillingBusy) {
                                CircularProgressIndicator(
                                    color = MidnightBlue,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Text(
                                text = when (state.selectedPlan) {
                                    PremiumPlan.YEARLY -> tr(
                                        "Subscribe Yearly VIP",
                                        "Assinar VIP Anual",
                                        "Đăng Ký VIP Hàng Năm",
                                    )
                                    PremiumPlan.MONTHLY -> tr(
                                        "Subscribe Monthly VIP",
                                        "Assinar VIP Mensal",
                                        "Đăng Ký VIP Hàng Tháng",
                                    )
                                    PremiumPlan.LIFETIME -> tr(
                                        "Unlock Lifetime VIP",
                                        "Desbloquear VIP Vitalício",
                                        "Mở Khóa VIP Trọn Đời",
                                    )
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color(0xFF69F0AE),
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = tr(
                                    "100% Secure Google Play Checkout · Cancel Anytime · No Commitment",
                                    "Pagamento 100% Seguro no Google Play · Cancele Quando Quiser · Sem Fidelidade",
                                    "Bảo mật thanh toán 100% qua Google Play · Hủy bất kỳ lúc nào · Không ràng buộc",
                                ),
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = viewModel::restorePurchases) {
                                Text(
                                    text = tr("Restore Purchases", "Restaurar Compras", "Khôi phục giao dịch"),
                                    color = PastelPurple,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            TextButton(onClick = onOpenTerms) {
                                Text(
                                    text = tr("Terms & Privacy", "Termos e Privacidade", "Điều khoản & Bảo mật"),
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }

                item(key = "paywall_trust_guarantees") {
                    SubscriptionTrustGuaranteesCard()
                }
            }
        }
    }
}

@Composable
private fun SubscriptionTrustGuaranteesCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceBlue.copy(alpha = 0.78f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = null,
                tint = Color(0xFF69F0AE),
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = tr(
                    "Subscription & Payment Security Guarantee",
                    "Garantia de Assinatura e Segurança no Pagamento",
                    "Cam Kết Minh Bạch & Bảo Mật Thanh Toán",
                ),
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.07f))

        TrustGuaranteeRow(
            icon = Icons.Filled.EventAvailable,
            title = tr(
                "Cancel Anytime · Zero Commitment",
                "Cancele a Qualquer Momento · Sem Compromisso",
                "Mua & Hủy Bất Kỳ Lúc Nào · Không Ràng Buộc",
            ),
            description = tr(
                "Manage or cancel Monthly/Yearly plans anytime with 1 tap in Google Play → Subscriptions. You keep full VIP access until the end of your paid billing period.",
                "Gerencie ou cancele planos Mensal/Anual quando quiser com 1 toque em Google Play → Assinaturas. Você mantém o acesso VIP até o fim do período pago.",
                "Toàn quyền quản lý hoặc hủy gói Tháng/Năm bất cứ lúc nào chỉ với 1 chạm trong Google Play → Gói đăng ký. Sau khi hủy, bạn vẫn giữ trọn quyền lợi VIP đến hết chu kỳ đã thanh toán.",
            ),
        )

        TrustGuaranteeRow(
            icon = Icons.Filled.Shield,
            title = tr(
                "100% Encrypted Google Play Payment Security",
                "Segurança de Pagamento 100% Criptografada pelo Google Play",
                "Bảo Mật Thanh Toán Tuyệt Đối 100% Qua Google Play",
            ),
            description = tr(
                "All transactions are processed directly by Google Play's official billing system. We never see or store your card, e-wallet, or bank details.",
                "Todas as transações são processadas diretamente pelo Google Play. Nunca acessamos ou armazenamos dados do seu cartão ou conta bancária.",
                "Mọi giao dịch được xử lý và mã hóa trực tiếp bởi hệ thống thanh toán chuẩn quốc tế của Google Play. Ứng dụng tuyệt đối không lưu trữ thông tin thẻ, ví điện tử hay ngân hàng của bạn.",
            ),
        )

        TrustGuaranteeRow(
            icon = Icons.Filled.Sync,
            title = tr(
                "No Hidden Fees & Free Multi-Device Restore",
                "Sem Taxas Ocultas e Restauração Gratuita em Vários Aparelhos",
                "Không Phí Ẩn · Khôi Phục Miễn Phí Khi Đổi Máy",
            ),
            description = tr(
                "Yearly & Monthly plans auto-renew at the listed price unless canceled at least 24h before renewal. Lifetime VIP is billed once forever. Restore VIP free on any Android device signed into your Google account.",
                "Planos Anual e Mensal renovam automaticamente pelo preço exibido, a menos que sejam cancelados 24h antes. O VIP Vitalício é pago uma única vez. Restaure grátis em qualquer Android.",
                "Gói Năm & Tháng tự động gia hạn theo đúng giá hiển thị trừ khi tắt trước ngày gia hạn ít nhất 24 giờ. Gói Trọn Đời chỉ trả 1 lần duy nhất mãi mãi. Khôi phục VIP miễn phí khi đổi điện thoại.",
            ),
        )
    }
}

@Composable
private fun TrustGuaranteeRow(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF69F0AE).copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF69F0AE),
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun PlanOptionCard(
    title: String,
    subtitle: String,
    highlightNote: String? = null,
    badge: String?,
    priceText: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF233166), Color(0xFF1A254E)),
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(SurfaceBlue.copy(alpha = 0.85f), SurfaceBlue.copy(alpha = 0.85f)),
                    )
                },
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) PremiumGold else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (selected) PremiumGold else TextSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (!badge.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) PremiumGold else DeepSleepBlue)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = badge,
                        color = if (selected) MidnightBlue else Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
            if (!highlightNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = highlightNote,
                    color = if (selected) PremiumGold else Color(0xFF69F0AE),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = priceText,
            color = if (selected) PremiumGold else TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
