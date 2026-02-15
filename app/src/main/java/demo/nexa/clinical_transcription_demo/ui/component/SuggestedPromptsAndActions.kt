package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors

/**
 * Suggested prompts component for chat.
 * Displays suggested questions that users can tap to quickly fill the input.
 *
 * @param prompts List of suggested prompt strings
 * @param onPromptSelected Callback when user selects a prompt
 * @param modifier Modifier for styling
 */
@Composable
fun SuggestedPromptsRow(
    prompts: List<String>,
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (prompts.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Suggested",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextTertiary,
            modifier = Modifier.padding(start = 4.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prompts) { prompt ->
                SuggestedPromptChip(
                    text = prompt,
                    onClick = { onPromptSelected(prompt) }
                )
            }
        }
    }
}

/**
 * Individual suggested prompt chip.
 */
@Composable
private fun SuggestedPromptChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .widthIn(max = 200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.SurfaceWhite)
            .border(1.dp, AppColors.BorderLight, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TealDark,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

/**
 * Quick action buttons for chat operations.
 * Provides quick access to common features like create note, search records.
 *
 * @param onCreateNote Callback for create note action
 * @param onSearchRecords Callback for search records action
 * @param onHistory Callback for view history action
 * @param modifier Modifier for styling
 */
@Composable
fun QuickActionsBar(
    onCreateNote: () -> Unit,
    onSearchRecords: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.BackgroundAqua)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            label = "Create Note",
            onClick = onCreateNote,
            modifier = Modifier.weight(1f)
        )

        QuickActionButton(
            label = "Search",
            onClick = onSearchRecords,
            modifier = Modifier.weight(1f)
        )

        QuickActionButton(
            label = "History",
            onClick = onHistory,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual quick action button.
 */
@Composable
private fun QuickActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.SurfaceWhite)
            .border(1.5.dp, AppColors.TealPrimary, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TealPrimary
        )
    }
}
