package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import demo.nexa.clinical_transcription_demo.domain.model.NoteStatus
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors
import demo.nexa.clinical_transcription_demo.ui.theme.AppDimens

/**
 * Small colored badge showing the note's processing status.
 * Each status maps to a distinct background/foreground color pair.
 */
@Composable
fun StatusBadge(
    status: NoteStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg, label) = when (status) {
        NoteStatus.NEW -> Triple(AppColors.StatusNewBg, AppColors.StatusNewFg, "New")
        NoteStatus.TRANSCRIBING -> Triple(AppColors.StatusTranscribingBg, AppColors.StatusTranscribingFg, "Transcribing")
        NoteStatus.SUMMARIZING -> Triple(AppColors.StatusSummarizingBg, AppColors.StatusSummarizingFg, "Summarizing")
        NoteStatus.DONE -> Triple(AppColors.StatusDoneBg, AppColors.StatusDoneFg, "Done")
        NoteStatus.ERROR -> Triple(AppColors.StatusErrorBg, AppColors.StatusErrorFg, "Error")
    }

    Row(
        modifier = modifier
            .height(AppDimens.badgeHeight)
            .background(bg, RoundedCornerShape(AppDimens.cornerRadiusCircle))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(AppDimens.badgeDot)
                .background(fg, CircleShape)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = fg,
            letterSpacing = 0.3.sp,
            lineHeight = 12.sp
        )
    }
}

/**
 * Small colored badge showing clinical priority (Urgent / Routine).
 */
@Composable
fun PriorityBadge(
    priority: String?,
    modifier: Modifier = Modifier
) {
    if (priority == null) return

    val isUrgent = priority.equals("Urgent", ignoreCase = true)
    val bg = if (isUrgent) AppColors.PriorityUrgentBg else AppColors.PriorityRoutineBg
    val fg = if (isUrgent) AppColors.PriorityUrgentFg else AppColors.PriorityRoutineFg

    Box(
        modifier = modifier
            .height(AppDimens.badgeHeight)
            .background(bg, RoundedCornerShape(AppDimens.cornerRadiusCircle))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            letterSpacing = 0.3.sp,
            lineHeight = 12.sp
        )
    }
}

/**
 * Generic colored tag chip.
 * Used to display clinical tags like "hypertension", "medication review", etc.
 */
@Composable
fun TagChip(
    tag: String,
    modifier: Modifier = Modifier,
    bgColor: Color = AppColors.TealMuted,
    fgColor: Color = AppColors.TealDark
) {
    Box(
        modifier = modifier
            .height(22.dp)
            .background(bgColor, RoundedCornerShape(AppDimens.cornerRadiusCircle))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tag,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = fgColor,
            letterSpacing = 0.2.sp,
            lineHeight = 12.sp
        )
    }
}
