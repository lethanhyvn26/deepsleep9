package com.sleepsounds.storytelling.presentation.podcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.presentation.components.AssetImage
import com.sleepsounds.storytelling.ui.theme.Dimens
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PremiumGold
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary

@Composable
fun PodcardGridCard(
    podcast: Podcast,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(Dimens.cardRadius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.32f)
                .clip(cardShape)
                .background(SurfaceBlueLight),
        ) {
            val coverPath = podcast.imagePath
            if (!coverPath.isNullOrBlank()) {
                AssetImage(
                    assetUri = coverPath,
                    contentDescription = podcast.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            if (podcast.isPremium) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PremiumGold)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "VIP",
                        tint = MidnightBlue,
                        modifier = Modifier.size(10.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "VIP",
                        color = MidnightBlue,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }

        Text(
            text = podcast.title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 6.dp),
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, lineHeight = 18.sp),
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
