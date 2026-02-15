package demo.nexa.clinical_transcription_demo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import demo.nexa.clinical_transcription_demo.R
import demo.nexa.clinical_transcription_demo.common.formatDateForDisplay
import demo.nexa.clinical_transcription_demo.common.formatDurationForDisplay
import demo.nexa.clinical_transcription_demo.common.formatElapsedTime
import demo.nexa.clinical_transcription_demo.domain.model.NoteStatus
import demo.nexa.clinical_transcription_demo.presentation.NotePlaybackViewModel
import demo.nexa.clinical_transcription_demo.presentation.PlaybackUiState
import demo.nexa.clinical_transcription_demo.ui.component.ClinicalSnapshotPanel
import demo.nexa.clinical_transcription_demo.ui.component.GradientOutlineStatusRow
import demo.nexa.clinical_transcription_demo.ui.component.GradientPillButton
import demo.nexa.clinical_transcription_demo.ui.component.PlaybackWaveformView
import demo.nexa.clinical_transcription_demo.ui.component.PriorityBadge
import demo.nexa.clinical_transcription_demo.ui.component.StatusBadge
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors
import demo.nexa.clinical_transcription_demo.ui.theme.AppDimens
import demo.nexa.clinical_transcription_demo.ui.theme.AppGradients
import java.util.Date

@Composable
fun NoteDetailScreen(
    noteId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotePlaybackViewModel = viewModel()
) {
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is PlaybackUiState.Loading -> {
            LoadingScreen(onBackClick = onBackClick, modifier = modifier)
        }
        is PlaybackUiState.Error -> {
            ErrorScreen(
                message = state.message,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
        is PlaybackUiState.Ready -> {
            NoteDetailContent(
                state = state,
                onBackClick = onBackClick,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onScrubStart = { viewModel.beginScrub() },
                onScrubPreviewMs = { viewModel.previewScrub(it) },
                onScrubEndMs = { viewModel.endScrub(it) },
                onGenerateSummary = { viewModel.generateSummary() },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LoadingScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.BackgroundTeal),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.loading),
            color = AppColors.TextPrimary,
            fontSize = AppDimens.textSizeBody
        )
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBarsPadding = WindowInsets.statusBars.asPaddingValues()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.BackgroundTeal)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.padding(
                    start = AppDimens.spacingMedium,
                    top = statusBarsPadding.calculateTopPadding() + AppDimens.spacingSmall
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(AppDimens.iconSizeDefault)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = AppColors.IconGray
                    )
                }
            }
            
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    color = AppColors.TextPrimary,
                    fontSize = AppDimens.textSizeBody
                )
            }
        }
    }
}

@Composable
private fun NoteDetailContent(
    state: PlaybackUiState.Ready,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onScrubStart: () -> Unit,
    onScrubPreviewMs: (Long) -> Unit,
    onScrubEndMs: (Long) -> Unit,
    onGenerateSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val date = Date(state.note.createdAtEpochMs)
    val createdDate = formatDateForDisplay(date)
    val duration = formatElapsedTime(state.durationMs, forceHours = true)
    val durationHasHours = state.durationMs >= 3600000
    val currentTime = formatElapsedTime(state.currentPositionMs, forceHours = durationHasHours)
    val statusBarsPadding = WindowInsets.statusBars.asPaddingValues()
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.BackgroundTeal)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Top navigation bar ──
            Column(
                modifier = Modifier
                    .padding(
                        start = AppDimens.spacingMedium,
                        end = AppDimens.spacingMedium,
                        top = statusBarsPadding.calculateTopPadding() + AppDimens.spacingSmall,
                        bottom = AppDimens.spacingSmall
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(AppDimens.iconSizeDefault)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = AppColors.IconGray
                        )
                    }
                    Column(
                        modifier = Modifier
                            .padding(start = AppDimens.spacingMedium)
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(-2.dp)
                    ) {
                        Text(
                            text = state.note.title,
                            fontSize = AppDimens.textSizeTitle,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TealDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacingSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = createdDate,
                                fontSize = AppDimens.textSizeCaption,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = duration,
                                fontSize = AppDimens.textSizeCaption,
                                fontWeight = FontWeight.Normal,
                                color = AppColors.TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Patient + badges row below the title
                if (state.note.patientName != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.padding(start = 40.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.note.patientName ?: "",
                            fontSize = AppDimens.textSizeSmall,
                            color = AppColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        if (state.note.patientId != null) {
                            Text(
                                text = "•",
                                fontSize = AppDimens.textSizeSmall,
                                color = AppColors.TextTertiary
                            )
                            Text(
                                text = state.note.patientId ?: "",
                                fontSize = AppDimens.textSizeSmall,
                                color = AppColors.TextTertiary
                            )
                        }
                        StatusBadge(status = state.note.status)
                        PriorityBadge(priority = state.note.priority)
                    }
                }
            }
            
            // ── Waveform section ──
            Column(
                modifier = Modifier.padding(horizontal = AppDimens.spacingMedium, vertical = AppDimens.spacingSmall),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlaybackWaveformView(
                    amplitudes = state.waveformAmplitudes,
                    currentPositionMs = state.currentPositionMs,
                    durationMs = state.durationMs,
                    onScrubStart = onScrubStart,
                    onScrubPreviewMs = onScrubPreviewMs,
                    onScrubEndMs = onScrubEndMs,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Timer + play button pill
                Box(
                    modifier = Modifier
                        .width(328.dp)
                        .height(AppDimens.pillHeight)
                        .background(
                            color = AppColors.ProgressOverlay,
                            shape = RoundedCornerShape(AppDimens.cornerRadiusCircle)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentTime,
                        fontSize = AppDimens.textSizeTitle,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TealDark
                    )
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(AppDimens.pillHeight)
                            .border(
                                width = AppDimens.borderWidthThin,
                                brush = AppGradients.linearGradient,
                                shape = CircleShape
                            )
                            .background(AppColors.SurfaceWhite, CircleShape)
                            .clickable(
                                onClick = onPlayPauseClick,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                if (state.isPlaying) android.R.drawable.ic_media_pause 
                                else R.drawable.play
                            ),
                            contentDescription = stringResource(
                                if (state.isPlaying) R.string.pause else R.string.play
                            ),
                            modifier = Modifier.size(20.dp),
                            tint = if (state.isPlaying) AppColors.TealDark else Color.Unspecified
                        )
                    }
                }
            }
            
            // ── Bottom card with tabs ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(
                    topStart = AppDimens.cornerRadiusXLarge,
                    topEnd = AppDimens.cornerRadiusXLarge
                ),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.SurfaceWhite
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppDimens.spacingMedium)
                ) {
                    // Three-tab selector
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppDimens.pillHeight)
                            .background(
                                color = AppColors.TabBackground,
                                shape = RoundedCornerShape(AppDimens.cornerRadiusMedium)
                            )
                            .padding(AppDimens.spacingXSmall)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Transcription tab
                            TabItem(
                                label = stringResource(R.string.transcription_tab),
                                isSelected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                modifier = Modifier.weight(1f)
                            )
                            // Summary tab
                            TabItem(
                                label = stringResource(R.string.summary_tab),
                                isSelected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                modifier = Modifier.weight(1f)
                            )
                            // Clinical tab
                            TabItem(
                                label = stringResource(R.string.clinical_snapshot),
                                isSelected = selectedTabIndex == 2,
                                onClick = { selectedTabIndex = 2 },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Tab content
                    when (selectedTabIndex) {
                        0 -> {
                            TranscriptionTabContent(
                                note = state.note,
                                transcriptionProgress = state.transcriptionProgress,
                                duration = duration,
                                navigationBarsPadding = navigationBarsPadding
                            )
                        }
                        1 -> {
                            SummaryTabContent(
                                summaryText = state.note.summaryText,
                                isGeneratingSummary = state.isGeneratingSummary,
                                summaryProgress = state.summaryProgress,
                                summaryError = state.summaryError,
                                durationMs = state.durationMs,
                                onGenerateSummaryClick = onGenerateSummary
                            )
                        }
                        2 -> {
                            ClinicalSnapshotPanel(
                                note = state.note,
                                formattedDate = createdDate,
                                formattedDuration = duration
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single tab item in the tab bar.
 */
@Composable
private fun TabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = if (isSelected) AppColors.SurfaceWhite else Color.Transparent,
                shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = AppDimens.textSizeSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) AppColors.TealDark else AppColors.TextTertiary,
            letterSpacing = 0.1.sp,
            maxLines = 1
        )
    }
}

/**
 * Timestamp indicator icon with two concentric circles
 */
@Composable
private fun TimestampDotIcon(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(AppDimens.timestampDotOuter),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(AppDimens.timestampDotOuter)
                .background(
                    color = AppColors.TealLight,
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(AppDimens.timestampDotInner)
                .background(
                    color = AppColors.TealPrimary.copy(alpha = 0.6f),
                    shape = CircleShape
                )
        )
    }
}

/**
 * Displays a transcript segment with timestamp and text
 */
@Composable
private fun TranscriptSegmentView(
    timestampText: String,
    transcriptText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacingXSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacingSmall),
            modifier = Modifier.padding(vertical = AppDimens.spacingXSmall)
        ) {
            TimestampDotIcon()
            
            Text(
                text = timestampText,
                fontSize = AppDimens.textSizeBody,
                fontWeight = FontWeight.Normal,
                color = AppColors.TextTertiary,
                lineHeight = AppDimens.lineHeightBody,
                letterSpacing = 0.15.sp
            )
        }
        
        Text(
            text = transcriptText,
            fontSize = AppDimens.textSizeBodyLarge,
            fontWeight = FontWeight.Normal,
            color = AppColors.TextPrimary,
            lineHeight = AppDimens.lineHeightBodyLarge,
            letterSpacing = 0.25.sp
        )
    }
}

/**
 * Content for the Transcription tab
 */
@Composable
private fun TranscriptionTabContent(
    note: demo.nexa.clinical_transcription_demo.domain.model.RecordingNote,
    transcriptionProgress: Int?,
    duration: String,
    navigationBarsPadding: androidx.compose.foundation.layout.PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = AppDimens.spacingSmall),
        contentAlignment = Alignment.Center
    ) {
        when {
            note.status == NoteStatus.TRANSCRIBING -> {
                val progressText = if (transcriptionProgress != null) {
                    stringResource(R.string.transcribing_progress, transcriptionProgress)
                } else {
                    stringResource(R.string.transcribing)
                }
                GradientOutlineStatusRow(
                    text = progressText,
                    iconRes = R.drawable.loader,
                    iconContentDescription = stringResource(R.string.transcribing)
                )
            }
            note.status == NoteStatus.ERROR && note.transcriptText == null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppDimens.spacingSmall)
                ) {
                    GradientOutlineStatusRow(
                        text = stringResource(R.string.transcription_failed),
                        iconRes = R.drawable.message_square_quote,
                        iconContentDescription = stringResource(R.string.transcription_failed)
                    )
                    note.errorMessage?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            fontSize = AppDimens.textSizeCaption,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = AppDimens.spacingSmall),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            note.transcriptText != null -> {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(
                            start = AppDimens.spacingSmall,
                            end = AppDimens.spacingSmall,
                            bottom = navigationBarsPadding.calculateBottomPadding() + AppDimens.spacingMedium
                        )
                ) {
                    TranscriptSegmentView(
                        timestampText = "00:00:00 - $duration",
                        transcriptText = note.transcriptText
                    )
                }
            }
            else -> {
                GradientOutlineStatusRow(
                    text = stringResource(R.string.no_transcript_yet),
                    iconRes = R.drawable.message_square_quote,
                    iconContentDescription = stringResource(R.string.no_transcript_yet)
                )
            }
        }
    }
}

/**
 * Content for the Summary tab
 */
@Composable
private fun SummaryTabContent(
    summaryText: String?,
    isGeneratingSummary: Boolean,
    summaryProgress: Int?,
    summaryError: String?,
    durationMs: Long,
    onGenerateSummaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = AppDimens.spacingSmall),
        contentAlignment = Alignment.Center
    ) {
        when {
            summaryText != null -> {
                val scrollState = rememberScrollState()
                var showContextMenu by remember { mutableStateOf(false) }
                var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
                val density = LocalDensity.current
                
                val fullContentText = buildString {
                    appendLine("Session Notes:")
                    appendLine()
                    appendLine("⏱️ ${formatDurationForDisplay(durationMs)}")
                    appendLine("🔒 On-device processed")
                    appendLine()
                    appendLine("---")
                    appendLine()
                    append(summaryText)
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(AppDimens.spacingSmall)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = AppColors.SurfaceWhite,
                                shape = RoundedCornerShape(AppDimens.cornerRadiusLarge)
                            )
                            .border(
                                width = 1.dp,
                                color = AppColors.DividerLight,
                                shape = RoundedCornerShape(AppDimens.cornerRadiusLarge)
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { offset ->
                                        pressOffset = with(density) {
                                            DpOffset(
                                                x = offset.x.toDp(),
                                                y = offset.y.toDp()
                                            )
                                        }
                                        showContextMenu = true
                                    }
                                )
                            }
                            .padding(AppDimens.spacingMedium)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(AppDimens.spacingMedium)
                        ) {
                            // Session Notes Header
                            Column(
                                verticalArrangement = Arrangement.spacedBy(AppDimens.spacingXSmall)
                            ) {
                                Text(
                                    text = "Session Notes:",
                                    fontSize = AppDimens.textSizeBodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.TextPrimary
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "⏱️", fontSize = AppDimens.textSizeBody)
                                    Text(
                                        text = formatDurationForDisplay(durationMs),
                                        fontSize = AppDimens.textSizeBody,
                                        color = AppColors.TextSecondary
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🔒", fontSize = AppDimens.textSizeBody)
                                    Text(
                                        text = "On-device processed",
                                        fontSize = AppDimens.textSizeBody,
                                        color = AppColors.TextSecondary
                                    )
                                }
                            }
                            
                            // Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(AppColors.DividerLight)
                            )
                            
                            // Markdown content
                            Material3RichText(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Markdown(content = summaryText)
                            }
                        }
                    }
                    
                    // Context Menu
                    DropdownMenu(
                        expanded = showContextMenu,
                        onDismissRequest = { showContextMenu = false },
                        offset = pressOffset,
                        modifier = Modifier
                            .width(180.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(8.dp),
                                ambientColor = Color(0x0A1C4F54),
                                spotColor = Color(0x1F1C4F54)
                            )
                            .background(
                                color = Color(0xFFFCFCFC),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE7E7E7),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Copy",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF1F1F1F),
                                        letterSpacing = 0.25.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.ic_copy),
                                        contentDescription = "Copy",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color(0xFF454545)
                                    )
                                }
                            },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(fullContentText))
                                showContextMenu = false
                            },
                            modifier = Modifier.height(48.dp)
                        )
                        
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Export",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF1F1F1F),
                                        letterSpacing = 0.25.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.ic_export),
                                        contentDescription = "Export",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color(0xFF454545)
                                    )
                                }
                            },
                            onClick = {
                                Toast.makeText(context, "Export is not supported yet", Toast.LENGTH_SHORT).show()
                                showContextMenu = false
                            },
                            modifier = Modifier.height(48.dp)
                        )
                    }
                }
            }
            isGeneratingSummary -> {
                val progressText = if (summaryProgress != null) {
                    "Generating Summary ($summaryProgress%)..."
                } else {
                    stringResource(R.string.generating_summary)
                }
                GradientOutlineStatusRow(
                    text = progressText,
                    iconRes = R.drawable.loader,
                    iconContentDescription = stringResource(R.string.generating_summary)
                )
            }
            summaryError != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppDimens.spacingSmall)
                ) {
                    GradientOutlineStatusRow(
                        text = stringResource(R.string.llm_generation_failed),
                        iconRes = R.drawable.message_square_quote,
                        iconContentDescription = stringResource(R.string.llm_generation_failed)
                    )
                    Text(
                        text = summaryError,
                        fontSize = AppDimens.textSizeCaption,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = AppDimens.spacingSmall),
                        textAlign = TextAlign.Center
                    )
                    GradientPillButton(
                        text = stringResource(R.string.generate_summary),
                        iconRes = R.drawable.sparkles,
                        onClick = onGenerateSummaryClick,
                        iconContentDescription = stringResource(R.string.generate_summary)
                    )
                }
            }
            else -> {
                GradientPillButton(
                    text = stringResource(R.string.generate_summary),
                    iconRes = R.drawable.sparkles,
                    onClick = onGenerateSummaryClick,
                    iconContentDescription = stringResource(R.string.generate_summary)
                )
            }
        }
    }
}
