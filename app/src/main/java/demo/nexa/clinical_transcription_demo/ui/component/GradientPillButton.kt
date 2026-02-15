package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import demo.nexa.clinical_transcription_demo.ui.theme.AppDimens
import demo.nexa.clinical_transcription_demo.ui.theme.AppGradients

/**
 * A pill-shaped button with gradient background, icon, and text.
 */
@Composable
fun GradientPillButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContentDescription: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.pillHeight)
            .background(
                brush = AppGradients.linearGradient,
                shape = RoundedCornerShape(AppDimens.cornerRadiusPill)
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = AppDimens.spacingMedium),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = iconContentDescription,
            modifier = Modifier.size(AppDimens.iconSizeMedium),
            tint = Color.Unspecified
        )
        
        Text(
            text = text,
            fontSize = AppDimens.textSizeBodyLarge,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.15.sp,
            color = Color(0xFFFCFCFC),
            modifier = Modifier.padding(start = AppDimens.spacingSmall)
        )
    }
}
