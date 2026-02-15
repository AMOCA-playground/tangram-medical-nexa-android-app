package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.absoluteValue

/**
 * Gesture detection utilities for swipe navigation.
 */

/**
 * Configuration for swipe gesture detection.
 */
data class SwipeConfig(
    val velocityThreshold: Float = 400f,  // pixels per second
    val distanceThreshold: Float = 100f   // minimum pixels to register swipe
)

/**
 * Callback interface for swipe gestures.
 */
interface SwipeCallback {
    fun onSwipeLeft()
    fun onSwipeRight()
    fun onSwipeUp()
    fun onSwipeDown()
}

/**
 * Apply horizontal swipe gesture detection to a Modifier.
 *
 * @param onSwipeLeft Callback when user swipes left
 * @param onSwipeRight Callback when user swipes right
 * @param config Swipe configuration parameters
 */
fun Modifier.onHorizontalSwipe(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    config: SwipeConfig = SwipeConfig()
): Modifier = pointerInput(Unit) {
    var dragAmount = 0f
    detectHorizontalDragGestures(
        onHorizontalDrag = { _, delta ->
            dragAmount += delta
        },
        onDragEnd = {
            if (dragAmount.absoluteValue > config.distanceThreshold) {
                when {
                    dragAmount < 0 -> onSwipeLeft()
                    dragAmount > 0 -> onSwipeRight()
                }
            }
            dragAmount = 0f
        }
    )
}

/**
 * Alternative simpler implementation using a single detectHorizontalDragGestures call.
 */
fun Modifier.detectSwipeGestures(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    config: SwipeConfig = SwipeConfig()
): Modifier = pointerInput(Unit) {
    var dragAmount = 0f
    detectHorizontalDragGestures(
        onHorizontalDrag = { _, delta ->
            dragAmount += delta
        },
        onDragEnd = {
            if (dragAmount.absoluteValue > config.distanceThreshold) {
                when {
                    dragAmount < 0 -> onSwipeLeft()
                    dragAmount > 0 -> onSwipeRight()
                }
            }
            dragAmount = 0f
        }
    )
}

/**
 * Apply vertical swipe gesture detection to a Modifier.
 */
fun Modifier.onVerticalSwipe(
    onSwipeUp: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    config: SwipeConfig = SwipeConfig()
): Modifier = pointerInput(Unit) {
    var dragAmount = 0f
    detectVerticalDragGestures(
        onVerticalDrag = { _, delta ->
            dragAmount += delta
        },
        onDragEnd = {
            if (dragAmount.absoluteValue > config.distanceThreshold) {
                when {
                    dragAmount < 0 -> onSwipeUp()
                    dragAmount > 0 -> onSwipeDown()
                }
            }
            dragAmount = 0f
        }
    )
}

