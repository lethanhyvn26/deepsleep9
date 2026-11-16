package com.sleepsounds.storytelling.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepsounds.storytelling.presentation.navigation.AppRoute
import com.sleepsounds.storytelling.presentation.navigation.BottomNavItem
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.DeepSleepCardNavy
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth

/**
 * Primary bottom navigation bar featuring the highlighted center Sleep Tracker orb.
 */
@Composable
fun DeepSleepBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepSleepCardNavy.copy(alpha = 0.98f)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.08f)),
        )

        Row(
            modifier = Modifier
                .responsiveContentWidth(680.dp)
                .height(68.dp)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEach { tabItem ->
                val active = currentRoute == tabItem.route.route ||
                    (tabItem.route == AppRoute.Sounds && currentRoute == AppRoute.Podcard.route)
                val isCenterTracker = tabItem.route == AppRoute.Tracker
                val tabTitle = stringResource(tabItem.labelRes)
                val clickInteraction = remember(tabItem.route.route) { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = clickInteraction,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onItemSelected(tabItem) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isCenterTracker) {
                        CenterTrackerTabContent(
                            tabItem = tabItem,
                            tabTitle = tabTitle,
                            active = active,
                        )
                    } else {
                        StandardTabContent(
                            tabItem = tabItem,
                            tabTitle = tabTitle,
                            active = active,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun CenterTrackerTabContent(
    tabItem: BottomNavItem,
    tabTitle: String,
    active: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .shadow(
                    elevation = if (active) 8.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = DeepSleepBlue,
                    spotColor = DeepSleepBlue,
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (active) {
                            listOf(Color(0xFF4F8BFF), DeepSleepBlue)
                        } else {
                            listOf(
                                DeepSleepBlue.copy(alpha = 0.88f),
                                PastelPurple.copy(alpha = 0.78f),
                            )
                        },
                    ),
                )
                .border(
                    width = if (active) 1.75.dp else 1.dp,
                    color = Color.White.copy(alpha = if (active) 0.90f else 0.45f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (active) tabItem.selectedIcon else tabItem.unselectedIcon,
                contentDescription = tabTitle,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = tabTitle,
            color = if (active) TextPrimary else TextSecondary,
            fontSize = 10.5.sp,
            lineHeight = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StandardTabContent(
    tabItem: BottomNavItem,
    tabTitle: String,
    active: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (active) DeepSleepBlue.copy(alpha = 0.22f) else Color.Transparent,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (active) tabItem.selectedIcon else tabItem.unselectedIcon,
                contentDescription = tabTitle,
                tint = if (active) DeepSleepBlue else TextSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = tabTitle,
            color = if (active) TextPrimary else TextSecondary,
            fontSize = 10.5.sp,
            lineHeight = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
