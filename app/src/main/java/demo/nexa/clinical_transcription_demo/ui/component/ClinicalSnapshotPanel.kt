package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import demo.nexa.clinical_transcription_demo.R
import demo.nexa.clinical_transcription_demo.domain.model.NoteSource
import demo.nexa.clinical_transcription_demo.domain.model.NoteStatus
import demo.nexa.clinical_transcription_demo.domain.model.RecordingNote
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors
import demo.nexa.clinical_transcription_demo.ui.theme.AppDimens

/**
 * Clinical Snapshot panel showing all patient/encounter metadata.
 * Displayed as the third tab in the NoteDetailScreen.
 * Uses a modern card-based layout with labeled fields.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClinicalSnapshotPanel(
    note: RecordingNote,
    formattedDate: String,
    formattedDuration: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Section: Patient Information
        SectionHeader(title = "Patient Information")
        InfoCard {
            InfoRow(
                label = stringResource(R.string.clinical_patient),
                value = note.patientName ?: "—"
            )
            InfoDivider()
            InfoRow(
                label = stringResource(R.string.clinical_mrn),
                value = note.patientId ?: "—"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Encounter Details
        SectionHeader(title = "Encounter Details")
        InfoCard {
            InfoRow(
                label = stringResource(R.string.clinical_visit),
                value = note.visitType ?: "—"
            )
            InfoDivider()
            InfoRow(
                label = stringResource(R.string.clinical_department),
                value = note.department ?: "—"
            )
            InfoDivider()
            InfoRow(
                label = stringResource(R.string.clinical_clinician),
                value = note.clinicianName ?: "—"
            )
            InfoDivider()
            InfoRow(
                label = "Date",
                value = formattedDate
            )
            InfoDivider()
            InfoRow(
                label = "Duration",
                value = formattedDuration
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Status & Priority
        SectionHeader(title = "Status & Priority")
        InfoCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.clinical_status),
                    fontSize = AppDimens.textSizeBody,
                    color = AppColors.TextSecondary,
                    fontWeight = FontWeight.Normal
                )
                StatusBadge(status = note.status)
            }
            InfoDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.clinical_priority),
                    fontSize = AppDimens.textSizeBody,
                    color = AppColors.TextSecondary,
                    fontWeight = FontWeight.Normal
                )
                PriorityBadge(priority = note.priority ?: "Routine")
            }
            InfoDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.clinical_source),
                    fontSize = AppDimens.textSizeBody,
                    color = AppColors.TextSecondary,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = when (note.source) {
                        NoteSource.RECORDED -> stringResource(R.string.source_recorded)
                        NoteSource.IMPORTED -> stringResource(R.string.source_imported)
                    },
                    fontSize = AppDimens.textSizeBody,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary
                )
            }
        }

        // Section: Tags
        val tags = note.tags
        if (!tags.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = stringResource(R.string.clinical_tags))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        TagChip(tag = tag)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // On-device processing badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(
                    color = AppColors.SurfaceMuted,
                    shape = RoundedCornerShape(AppDimens.cornerRadiusMedium)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔒", fontSize = AppDimens.textSizeBody)
                Text(
                    text = "All data processed on-device • HIPAA-friendly",
                    fontSize = AppDimens.textSizeSmall,
                    color = AppColors.TextSecondary,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = AppDimens.textSizeSmall,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.TextTertiary,
        letterSpacing = 0.8.sp,
        modifier = modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = AppColors.SurfaceSoft,
                shape = RoundedCornerShape(AppDimens.cornerRadiusLarge)
            )
            .border(
                width = AppDimens.borderWidthThin,
                color = AppColors.DividerLight,
                shape = RoundedCornerShape(AppDimens.cornerRadiusLarge)
            )
    ) {
        content()
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = AppDimens.textSizeBody,
            color = AppColors.TextSecondary,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = AppDimens.textSizeBody,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextPrimary
        )
    }
}

@Composable
private fun InfoDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(1.dp)
            .background(AppColors.DividerLight)
    )
}
