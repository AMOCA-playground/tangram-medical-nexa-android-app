package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors
import demo.nexa.clinical_transcription_demo.ui.theme.AppDimens

/**
 * Modern search bar with rounded corners, search icon, and clear button.
 * Used on the notes list screen to filter notes by patient name, MRN, or title.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String = "Search name, MRN, or title",
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusLarge)

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = AppDimens.textSizeBody,
            fontWeight = FontWeight.Normal,
            color = AppColors.TextPrimary
        ),
        cursorBrush = SolidColor(AppColors.TealPrimary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.searchBarHeight)
                    .background(AppColors.SearchBarBg, shape)
                    .border(AppDimens.borderWidthThin, AppColors.ChipBorder, shape)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AppColors.TextTertiary
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = hint,
                            fontSize = AppDimens.textSizeBody,
                            color = AppColors.TextTertiary,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp),
                            tint = AppColors.TextTertiary
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}
