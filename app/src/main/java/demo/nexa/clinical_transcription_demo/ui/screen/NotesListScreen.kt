package demo.nexa.clinical_transcription_demo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import demo.nexa.clinical_transcription_demo.R
import demo.nexa.clinical_transcription_demo.domain.model.NoteStatus
import demo.nexa.clinical_transcription_demo.ui.component.FilterChipRow
import demo.nexa.clinical_transcription_demo.ui.component.NoteCard
import demo.nexa.clinical_transcription_demo.ui.component.SearchBar
import demo.nexa.clinical_transcription_demo.ui.state.NoteUiState
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors
import demo.nexa.clinical_transcription_demo.ui.theme.AppDimens
import demo.nexa.clinical_transcription_demo.ui.theme.AppGradients

private val FILTER_ALL = "All"
private val FILTERS = listOf("All", "New", "Transcribing", "Summarizing", "Done", "Error")

@Composable
fun NotesListScreen(
    notes: List<NoteUiState>,
    onNoteClick: (NoteUiState) -> Unit,
    onRecordClick: () -> Unit,
    onImportClick: () -> Unit,
    onChatClick: () -> Unit,
    onMedicalRecordsClick: () -> Unit,
    onTestAsrClick: (() -> Unit)? = null,
    onTestLlmClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val statusBarsPadding = WindowInsets.statusBars.asPaddingValues()
    val listState = rememberLazyListState()

    var previousNotesCount by rememberSaveable { mutableIntStateOf(notes.size) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(FILTER_ALL) }

    LaunchedEffect(notes.size) {
        if (notes.size > previousNotesCount) {
            listState.animateScrollToItem(0)
        }
        previousNotesCount = notes.size
    }

    // Compute counts per filter
    val filterCounts by remember(notes) {
        derivedStateOf {
            mapOf(
                "All" to notes.size,
                "New" to notes.count { it.status == NoteStatus.NEW },
                "Transcribing" to notes.count { it.status == NoteStatus.TRANSCRIBING },
                "Summarizing" to notes.count { it.status == NoteStatus.SUMMARIZING },
                "Done" to notes.count { it.status == NoteStatus.DONE },
                "Error" to notes.count { it.status == NoteStatus.ERROR }
            )
        }
    }

    // Filtered + searched notes
    val filteredNotes by remember(notes, selectedFilter, searchQuery) {
        derivedStateOf {
            var result = notes

            // Apply status filter
            if (selectedFilter != FILTER_ALL) {
                val targetStatus = when (selectedFilter) {
                    "New" -> NoteStatus.NEW
                    "Transcribing" -> NoteStatus.TRANSCRIBING
                    "Summarizing" -> NoteStatus.SUMMARIZING
                    "Done" -> NoteStatus.DONE
                    "Error" -> NoteStatus.ERROR
                    else -> null
                }
                if (targetStatus != null) {
                    result = result.filter { it.status == targetStatus }
                }
            }

            // Apply search query
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                result = result.filter { note ->
                    note.title.lowercase().contains(q) ||
                            note.patientName?.lowercase()?.contains(q) == true ||
                            note.patientId?.lowercase()?.contains(q) == true ||
                            note.department?.lowercase()?.contains(q) == true ||
                            note.clinicianName?.lowercase()?.contains(q) == true
                }
            }

            result
        }
    }

    // Stats
    val totalCount = notes.size
    val inProgressCount = notes.count { it.status == NoteStatus.TRANSCRIBING || it.status == NoteStatus.SUMMARIZING }
    val errorCount = notes.count { it.status == NoteStatus.ERROR }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.BackgroundAqua)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Header ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = AppDimens.spacingMedium,
                        end = AppDimens.spacingMedium,
                        top = statusBarsPadding.calculateTopPadding() + AppDimens.spacingSmall
                    )
            ) {
                // App name + subtitle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.notes_title),
                            fontSize = AppDimens.textSizeHeadline,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TealDark
                        )
                        Text(
                            text = stringResource(R.string.notes_subtitle),
                            fontSize = AppDimens.textSizeSmall,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    // Medical Records Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.TealDark.copy(alpha = 0.1f))
                            .clickable(onClick = onMedicalRecordsClick)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AppColors.TealDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Records",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TealDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    hint = stringResource(R.string.notes_search_hint)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter chips
                FilterChipRow(
                    filters = FILTERS,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    counts = filterCounts
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Text(
                    text = stringResource(R.string.notes_stats, totalCount, inProgressCount, errorCount),
                    fontSize = AppDimens.textSizeCaption,
                    color = AppColors.TextTertiary,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Notes list ──
            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (notes.isEmpty()) "No recordings yet" else "No matching notes",
                            fontSize = AppDimens.textSizeBodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = if (notes.isEmpty()) "Tap the record button to get started"
                            else "Try adjusting your filters or search",
                            fontSize = AppDimens.textSizeBody,
                            color = AppColors.TextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = AppDimens.spacingMedium,
                        end = AppDimens.spacingMedium,
                        top = AppDimens.spacingSmall,
                        bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note) }
                        )
                    }
                }
            }
        }

        // ── Record FAB (center bottom) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navigationBarsPadding.calculateBottomPadding() + AppDimens.spacingLarge)
                .size(AppDimens.fabSizeLarge)
                .background(AppColors.SurfaceWhite, CircleShape)
                .border(AppDimens.borderWidthThin, AppColors.BorderMedium, CircleShape)
                .clickable(
                    onClick = onRecordClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(AppDimens.fabInnerSize)
                    .background(
                        brush = AppGradients.horizontalGradient,
                        shape = CircleShape
                    )
            )
        }

        // ── AI Assistant FAB (bottom-left) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = AppDimens.spacingLarge,
                    bottom = navigationBarsPadding.calculateBottomPadding() + 32.dp
                )
                .size(AppDimens.fabSizeSmall)
                .background(AppColors.SurfaceWhite, CircleShape)
                .border(AppDimens.borderWidthThin, AppColors.BorderLight, CircleShape)
                .clickable(
                    onClick = onChatClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Medical Assistant",
                modifier = Modifier.size(AppDimens.iconSizeDefault),
                tint = AppColors.TealDark
            )
        }

        // ── Import FAB (bottom-right) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = AppDimens.spacingLarge,
                    bottom = navigationBarsPadding.calculateBottomPadding() + 32.dp
                )
                .size(AppDimens.fabSizeSmall)
                .background(AppColors.SurfaceWhite, CircleShape)
                .border(AppDimens.borderWidthThin, AppColors.BorderLight, CircleShape)
                .clickable(
                    onClick = onImportClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.notes_import),
                modifier = Modifier.size(AppDimens.iconSizeDefault),
                tint = AppColors.IconGray
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotesListScreenPreview() {
    MaterialTheme {
        NotesListScreen(
            notes = listOf(
                NoteUiState(
                    id = "1",
                    title = "MRN-12345-THER-0002",
                    date = "Jun 16, 2025",
                    duration = "00:00:20",
                    hasTranscript = true,
                    status = NoteStatus.DONE,
                    patientName = "Avery Wells",
                    department = "Cardiology",
                    clinicianName = "Dr. Ortega",
                    priority = "Urgent",
                    tags = listOf("hypertension")
                ),
                NoteUiState(
                    id = "2",
                    title = "MRN-12345-THER-0001",
                    date = "Jun 16, 2025",
                    duration = "00:00:20",
                    hasTranscript = true,
                    status = NoteStatus.NEW,
                    patientName = "Jordan Patel",
                    department = "Neurology",
                    priority = "Routine"
                )
            ),
            onNoteClick = {},
            onRecordClick = {},
            onImportClick = {},
            onChatClick = {},
            onMedicalRecordsClick = {}
        )
    }
}
