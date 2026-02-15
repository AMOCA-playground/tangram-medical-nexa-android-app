package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors

/**
 * Voice input button component for chat.
 * Displays a microphone icon that can be tapped to initiate voice input.
 *
 * @param onVoiceInputClicked Callback when user taps voice input button
 * @param isRecording Whether currently recording/processing
 * @param modifier Modifier for styling
 */
@Composable
fun VoiceInputButton(
    onVoiceInputClicked: () -> Unit,
    isRecording: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isRecording -> Color(0xFFFF6B6B)  // Red when recording
            isPressed -> AppColors.TealPrimary
            else -> AppColors.TealDark
        },
        label = "voiceButtonColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        label = "voiceButtonScale"
    )

    Box(
        modifier = modifier
            .size(52.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                onClick = {
                    isPressed = !isPressed
                    onVoiceInputClicked()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Filled.MicOff else Icons.Filled.Mic,
            contentDescription = if (isRecording) "Stop voice input" else "Start voice input",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Expanded voice input button with label.
 * Shows microphone icon and text.
 *
 * @param onVoiceInputClicked Callback when user taps
 * @param isRecording Whether currently recording
 * @param modifier Modifier for styling
 */
@Composable
fun VoiceInputButtonWithLabel(
    onVoiceInputClicked: () -> Unit,
    isRecording: Boolean = false,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onVoiceInputClicked,
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(
                if (isRecording) Color(0xFFFF6B6B) else AppColors.TealDark
            )
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Filled.MicOff else Icons.Filled.Mic,
            contentDescription = if (isRecording) "Stop voice input" else "Start voice input",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
