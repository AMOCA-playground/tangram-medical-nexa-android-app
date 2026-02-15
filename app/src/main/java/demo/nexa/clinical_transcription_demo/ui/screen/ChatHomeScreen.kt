package demo.nexa.clinical_transcription_demo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import demo.nexa.clinical_transcription_demo.presentation.ChatMessageUi
import demo.nexa.clinical_transcription_demo.presentation.ChatViewModel
import demo.nexa.clinical_transcription_demo.ui.component.SuggestedPromptsRow
import demo.nexa.clinical_transcription_demo.ui.component.VoiceInputButton
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors
import demo.nexa.clinical_transcription_demo.ui.theme.AppDimens
import demo.nexa.clinical_transcription_demo.ui.theme.AppGradients

/**
 * Enhanced Chat Home Screen - the main landing screen of the app
 * Features a polished chat interface with seamless access to notes
 */
@Composable
fun ChatHomeScreen(
    onNotesClick: () -> Unit,
    onRecordClick: () -> Unit,
    viewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val statusBarsPadding = WindowInsets.statusBars.asPaddingValues()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.BackgroundAqua)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Header with Branding ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.SurfaceWhite)
                    .padding(
                        start = AppDimens.spacingLarge,
                        end = AppDimens.spacingLarge,
                        top = statusBarsPadding.calculateTopPadding() + 16.dp,
                        bottom = 16.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Medical AI Assistant",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TealDark
                        )
                        Text(
                            text = "Your intelligent clinical companion",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            letterSpacing = 0.3.sp
                        )
                    }

                    // Clear Chat Button
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Chat",
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── Chat Messages Area ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(AppColors.BackgroundAqua)
            ) {
                if (uiState.messages.isEmpty()) {
                    WelcomeContent(modifier = Modifier.padding(horizontal = 24.dp))
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.messages) { message ->
                            ChatMessageBubble(message)
                        }
                    }
                }
            }

            // ── Chat Input Area ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.SurfaceWhite)
            ) {
                HorizontalDivider(thickness = 1.dp, color = AppColors.BorderLight)

                // Suggested Prompts Row
                if (uiState.suggestedPrompts.isNotEmpty()) {
                    SuggestedPromptsRow(
                        prompts = uiState.suggestedPrompts.take(3),
                        onPromptSelected = { prompt ->
                            viewModel.selectSuggestedPrompt(prompt)
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Input Button
                    VoiceInputButton(
                        onVoiceInputClicked = {
                            // TODO: Integrate voice input
                            // Will require recording and ASR processing
                        },
                        isRecording = false,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.onInputTextChanged(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Ask about medical terms, drugs, diagnoses...",
                                fontSize = 14.sp,
                                color = AppColors.TextTertiary
                            )
                        },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.TealPrimary,
                            unfocusedBorderColor = AppColors.BorderMedium,
                            focusedContainerColor = AppColors.SurfaceWhite,
                            unfocusedContainerColor = AppColors.SurfaceWhite
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = { viewModel.sendMessage() },
                        enabled = !uiState.isLoading && uiState.inputText.isNotBlank(),
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (!uiState.isLoading && uiState.inputText.isNotBlank())
                                    AppColors.TealDark
                                else
                                    AppColors.BorderMedium
                            )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(navigationBarsPadding.calculateBottomPadding()))
            }
        }

        // ── Floating Action Buttons ──

        // Record FAB (center bottom, above input)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
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

        // Notes FAB (bottom-right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = AppDimens.spacingLarge,
                    bottom = 115.dp
                )
                .size(AppDimens.fabSizeSmall)
                .background(AppColors.TealDark, CircleShape)
                .clickable(
                    onClick = onNotesClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "View Notes",
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun WelcomeContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // AI Icon representation
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = AppGradients.horizontalGradient
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AI",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "How can I help you today?",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TealDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ask me to explain medical terms, find information about drugs, diagnoses, or help with clinical documentation.",
            fontSize = 15.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Try asking:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextTertiary,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExamplePrompt("What is Atrial Fibrillation?")
        ExamplePrompt("Side effects of Metformin")
        ExamplePrompt("What is the ICD-10 code for hypertension?")
    }
}

@Composable
private fun ExamplePrompt(text: String) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .widthIn(max = 320.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.SurfaceWhite)
            .border(1.dp, AppColors.BorderLight, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = AppColors.TealDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessageUi) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(
                    when {
                        isUser -> AppColors.TealDark
                        message.isError -> Color(0xFFFFEBEE)
                        else -> AppColors.SurfaceWhite
                    }
                )
                .border(
                    width = if (isUser || message.isError) 0.dp else 1.dp,
                    color = if (isUser || message.isError) Color.Transparent else AppColors.BorderLight,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.content,
                color = when {
                    isUser -> Color.White
                    message.isError -> Color(0xFFC62828)
                    else -> AppColors.TextPrimary
                },
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }
    }
}


