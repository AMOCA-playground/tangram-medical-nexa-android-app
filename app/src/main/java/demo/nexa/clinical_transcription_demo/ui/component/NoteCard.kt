package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import demo.nexa.clinical_transcription_demo.R
import demo.nexa.clinical_transcription_demo.domain.model.NoteStatus
import demo.nexa.clinical_transcription_demo.ui.state.NoteUiState
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors
import demo.nexa.clinical_transcription_demo.ui.theme.AppDimens

/**
 * Redesigned note card showing clinical metadata:
 * - Title (bold) + duration on the right
 * - Patient name • Department
 * - Date + status/priority badges + tags
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: NoteUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppDimens.cornerRadiusLarge),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.SurfaceCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppDimens.cardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: Title + duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    fontSize = AppDimens.textSizeBodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TealDark,
                    letterSpacing = 0.15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = note.duration,
                    fontSize = AppDimens.textSizeSmall,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextTertiary,
                    letterSpacing = 0.3.sp
                )
            }

            // Row 2: Patient name • Dept (if available)
            val metaText = buildString {
                note.patientName?.let { append(it) }
                if (note.department != null) {
                    if (isNotEmpty()) append("  •  ")
                    append(note.department)
                }
                if (note.clinicianName != null) {
                    if (isNotEmpty()) append("  •  ")
                    append(note.clinicianName)
                }
            }
            if (metaText.isNotEmpty()) {
                Text(
                    text = metaText,
                    fontSize = AppDimens.textSizeSmall,
                    color = AppColors.TextSecondary,
                    letterSpacing = 0.2.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Row 3: Date + badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.date,
                    fontSize = AppDimens.textSizeCaption,
                    color = AppColors.TextTertiary,
                    letterSpacing = 0.4.sp
                )

                // Processing indicator or status badge
                if (note.isProcessing) {
                    Row(
                        modifier = Modifier
                            .height(AppDimens.badgeHeight)
                            .background(
                                color = Color(0xFFEDF2F0),
                                shape = RoundedCornerShape(AppDimens.cornerRadiusCircle)
                            )
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.card_loader),
                            contentDescription = "Processing",
                            modifier = Modifier.size(12.dp),
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "Processing…",
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            color = AppColors.TextPrimary,
                            letterSpacing = 0.3.sp
                        )
                    }
                } else {
                    StatusBadge(status = note.status)
                }

                PriorityBadge(priority = note.priority)

                if (note.hasTranscript && !note.isProcessing) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = AppColors.BadgeBackground,
                                shape = RoundedCornerShape(AppDimens.cornerRadiusMedium)
                            )
                            .padding(horizontal = 5.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.message_square_quote),
                            contentDescription = "Has transcript",
                            modifier = Modifier.size(AppDimens.iconSizeSmall),
                            tint = Color.Unspecified
                        )
                    }
                }
            }

            // Row 4: Tags (if any)
            val tags = note.tags
            if (!tags.isNullOrEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        TagChip(tag = tag)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun NoteCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(AppColors.BackgroundAqua)
                .padding(AppDimens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NoteCard(
                note = NoteUiState(
                    id = "1",
                    title = "MRN-12345-THER-0003",
                    date = "Jan 30, 2026",
                    duration = "00:00:20",
                    hasTranscript = false,
                    isProcessing = true,
                    patientName = "Avery Wells",
                    department = "Cardiology",
                    clinicianName = "Dr. Ortega",
                    priority = "Urgent",
                    tags = listOf("hypertension", "medication review")
                ),
                onClick = {}
            )
            NoteCard(
                note = NoteUiState(
                    id = "2",
                    title = "MRN-12345-THER-0002",
                    date = "Jun 16, 2025",
                    duration = "00:00:20",
                    hasTranscript = true,
                    isProcessing = false,
                    status = NoteStatus.DONE,
                    patientName = "Jordan Patel",
                    department = "Neurology",
                    clinicianName = "Dr. Nguyen",
                    priority = "Routine",
                    tags = listOf("pain management")
                ),
                onClick = {}
            )
        }
    }
}
