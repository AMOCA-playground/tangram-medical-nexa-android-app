package demo.nexa.clinical_transcription_demo.asr

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import demo.nexa.clinical_transcription_demo.audio.WaveformExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * State for voice input processing.
 */
data class VoiceInputState(
    val isRecording: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val transcript: String? = null
)

/**
 * Helper class to handle voice input for chat.
 * Wraps NexaAsrEngine with permissions checking and UI state management.
 */
class VoiceRecognitionHelper(
    private val application: Application
) {
    private val context: Context = application.applicationContext
    private val asrEngine = NexaAsrEngine.getInstance(application)
    private val waveformExtractor = WaveformExtractor.getInstance()

    private val _voiceState = MutableStateFlow(VoiceInputState())
    val voiceState: StateFlow<VoiceInputState> = _voiceState.asStateFlow()

    /**
     * Check if RECORD_AUDIO permission is granted.
     */
    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if RECORD_AUDIO permission was denied (and can show rationale).
     */
    fun shouldShowPermissionRationale(activity: androidx.activity.ComponentActivity): Boolean {
        return androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            android.Manifest.permission.RECORD_AUDIO
        )
    }

    /**
     * Get the permission to request.
     */
    fun getAudioPermission(): String {
        return android.Manifest.permission.RECORD_AUDIO
    }

    /**
     * Process a recorded audio file using ASR.
     */
    suspend fun processAudioFile(audioPath: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            _voiceState.update { it.copy(isProcessing = true, error = null) }

            val result = asrEngine.transcribe(audioPath, "en")

            result.onSuccess { transcript ->
                _voiceState.update {
                    it.copy(
                        isProcessing = false,
                        transcript = transcript,
                        error = null
                    )
                }
                Log.d(TAG, "Transcription successful: ${transcript.take(100)}")
            }.onFailure { error ->
                val errorMsg = "Transcription failed: ${error.message}"
                _voiceState.update {
                    it.copy(
                        isProcessing = false,
                        error = errorMsg
                    )
                }
                Log.e(TAG, errorMsg, error)
            }

            result
        } catch (e: Exception) {
            val errorMsg = "Error processing audio: ${e.message}"
            _voiceState.update {
                it.copy(
                    isProcessing = false,
                    error = errorMsg
                )
            }
            Log.e(TAG, errorMsg, e)
            Result.failure(e)
        }
    }

    /**
     * Clear the current voice state.
     */
    fun clearState() {
        _voiceState.update { VoiceInputState() }
    }

    /**
     * Get transcript and clear state.
     */
    fun getTranscriptAndClear(): String? {
        val transcript = _voiceState.value.transcript
        clearState()
        return transcript
    }

    companion object {
        private const val TAG = "VoiceRecognitionHelper"

        @Volatile
        private var INSTANCE: VoiceRecognitionHelper? = null

        fun getInstance(application: Application): VoiceRecognitionHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VoiceRecognitionHelper(application).also { INSTANCE = it }
            }
        }
    }
}

