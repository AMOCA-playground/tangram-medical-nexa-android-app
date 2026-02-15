package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors
import demo.nexa.clinical_transcription_demo.ui.theme.AppDimens

/**
 * A single filter chip used in the horizontal filter row.
 * Selected chips use dark teal fill; unselected use white with border.
 */
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null
) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusCircle)
    val displayText = if (count != null && count > 0) "$label ($count)" else label

    Box(
        modifier = modifier
            .height(AppDimens.chipHeight)
            .background(
                color = if (isSelected) AppColors.ChipSelectedBg else AppColors.ChipDefaultBg,
                shape = shape
            )
            .then(
                if (!isSelected) Modifier.border(AppDimens.borderWidthThin, AppColors.ChipBorder, shape)
                else Modifier
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            fontSize = AppDimens.textSizeSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) AppColors.ChipSelectedFg else AppColors.ChipDefaultFg,
            letterSpacing = 0.3.sp
        )
    }
}

/**
 * Horizontal scrolling row of filter chips.
 * Used to filter notes by status on the notes list screen.
 */
@Composable
fun FilterChipRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    counts: Map<String, Int> = emptyMap()
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                label = filter,
                isSelected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                count = counts[filter]
            )
        }
    }
}
