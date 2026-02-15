package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Wrapper composable that enables swipe gestures for screen navigation.
 * Allows users to swipe left/right to navigate between screens.
 *
 * @param content The screen content to display
 * @param onSwipeLeft Callback when user swipes left
 * @param onSwipeRight Callback when user swipes right
 * @param modifier Modifier for styling
 */
@Composable
fun SwipeableScreenContainer(
    content: @Composable () -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.detectSwipeGestures(
            onSwipeLeft = onSwipeLeft,
            onSwipeRight = onSwipeRight,
            config = SwipeConfig(
                velocityThreshold = 400f,
                distanceThreshold = 100f
            )
        )
    ) {
        content()
    }
}

