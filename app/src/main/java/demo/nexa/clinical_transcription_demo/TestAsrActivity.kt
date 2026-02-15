package demo.nexa.clinical_transcription_demo

import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import demo.nexa.clinical_transcription_demo.asr.NexaAsrEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * TestAsrActivity: ASR (Automatic Speech Recognition) benchmarking tool
 *
 * PURPOSE:
 * Benchmarks the Nexa ASR engine by processing multiple audio files and measuring performance metrics including:
 * - Inference time (latency)
 * - Real-Time Factor (RTF = inference_time / audio_duration)
 * - Timestamp intervals
 * - SDK profiling data (TTFT, prompt/decode times, token speeds)
 *
 * This benchmarking tool is used to gather performance statistics for simulating progress bars in the UI.
 * The RTF values obtained from this test are used to calibrate the progress simulation constants in
 * ProgressSimulator.kt, which calculates expectedDurationMs = audio_duration * model_rtf.
 *
 * SETUP - How to place audio files:
 * 1. Connect your Android device via USB with ADB enabled
 * 2. Push your own audio files to /data/local/tmp/ using adb:
 *    adb push /path/to/your/audio.wav /data/local/tmp/
 *    adb push /path/to/your/audio.mp3 /data/local/tmp/
 * 3. Verify files exist:
 *    adb shell ls -la /data/local/tmp/
 * 4. Update testAudioPaths list below with your actual file paths
 *
 * USAGE:
 * Launch this activity and tap "Start ASR Benchmark" to begin testing. Results include
 * per-file statistics, overall summary, and detailed result structure inspection.
 *
 * SUPPORTED FORMATS:
 * .wav, .mp3, and other formats supported by Android MediaMetadataRetriever
 */
class TestAsrActivity : AppCompatActivity() {
    
    private lateinit var outputText: TextView
    private lateinit var startButton: Button
    private lateinit var scrollView: ScrollView
    
    private val testAudioPaths = listOf(
        "/data/local/tmp/jfk.wav",
        "/data/local/tmp/OSR_us_000_0010_16k.wav",
        "/data/local/tmp/YTDown.com_YouTube_CBT-Role-Play-Complete-Session-Social-An_Media_8K4HW6_MvoU_004_144p.mp3"
    )
    
    private val numRuns = 10
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        startButton = Button(this).apply {
            text = "Start ASR Benchmark"
            setOnClickListener { runBenchmark() }
        }
        
        outputText = TextView(this).apply {
            text = "Click 'Start' to begin ASR benchmarking...\n\n"
            textSize = 12f
            setTextIsSelectable(true)
        }
        
        scrollView = ScrollView(this).apply {
            addView(outputText)
        }
        
        layout.addView(startButton)
        layout.addView(scrollView)
        
        setContentView(layout)
    }
    
    private fun runBenchmark() {
        startButton.isEnabled = false
        outputText.text = "Starting benchmark...\n\n"
        
        lifecycleScope.launch {
            try {
                val asrEngine = NexaAsrEngine.getInstance(applicationContext)
                
                appendOutput("=== ASR BENCHMARK TEST ===\n")
                appendOutput("Audio files: ${testAudioPaths.size}\n")
                appendOutput("Runs per file: $numRuns\n\n")
                
                val allRtfs = mutableListOf<Double>()
                
                for ((index, audioPath) in testAudioPaths.withIndex()) {
                    appendOutput("--- Audio ${index + 1}: $audioPath ---\n")
                    
                    val audioFile = File(audioPath)
                    if (!audioFile.exists()) {
                        appendOutput("ERROR: File not found!\n\n")
                        continue
                    }
                    
                    val audioDurationMs = getAudioDuration(audioFile)
                    appendOutput("Audio duration: ${audioDurationMs}ms (${audioDurationMs / 1000.0}s)\n\n")
                    
                    val latencies = mutableListOf<Long>()
                    val rtfs = mutableListOf<Double>()
                    
                    for (run in 1..numRuns) {
                        appendOutput("Run $run: ")
                        
                        var wasSuccess = false
                        val inferenceTimeMs = withContext(Dispatchers.IO) {
                            measureTimeMillis {
                                val result = asrEngine.transcribe(audioPath, "en")
                                result.onSuccess {
                                    wasSuccess = true
                                }.onFailure { error ->
                                    appendOutput("ERROR - ${error.message}\n")
                                }
                            }
                        }
                        
                        if (!wasSuccess) {
                            continue  // Skip this run if it failed
                        }
                        
                        latencies.add(inferenceTimeMs)
                        
                        val rtf = inferenceTimeMs.toDouble() / audioDurationMs.toDouble()
                        rtfs.add(rtf)
                        allRtfs.add(rtf)
                        
                        appendOutput("${inferenceTimeMs}ms, RTF=${String.format("%.3f", rtf)}\n")
                    }
                    
                    val avgLatency = latencies.average()
                    val avgRtf = rtfs.average()
                    
                    appendOutput("\nAudio ${index + 1} Summary:\n")
                    appendOutput("  Avg Latency: ${String.format("%.2f", avgLatency)}ms\n")
                    appendOutput("  Avg RTF: ${String.format("%.3f", avgRtf)}\n")
                    appendOutput("  Min RTF: ${String.format("%.3f", rtfs.minOrNull() ?: 0.0)}\n")
                    appendOutput("  Max RTF: ${String.format("%.3f", rtfs.maxOrNull() ?: 0.0)}\n\n")
                    
                    printSampleResult(asrEngine, audioPath, index + 1)
                }
                
                if (allRtfs.isNotEmpty()) {
                    appendOutput("=== OVERALL SUMMARY ===\n")
                    appendOutput("Total runs: ${allRtfs.size}\n")
                    appendOutput("Average RTF: ${String.format("%.3f", allRtfs.average())}\n")
                    appendOutput("Min RTF: ${String.format("%.3f", allRtfs.minOrNull() ?: 0.0)}\n")
                    appendOutput("Max RTF: ${String.format("%.3f", allRtfs.maxOrNull() ?: 0.0)}\n\n")
                }
                
                appendOutput("=== TESTING TIMESTAMP INTERVALS ===\n")
                testTimestampIntervals(asrEngine)
                
                appendOutput("\n=== BENCHMARK COMPLETE ===\n")
                
            } catch (e: Exception) {
                appendOutput("\nFATAL ERROR: ${e.message}\n")
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    startButton.isEnabled = true
                }
            }
        }
    }
    
    private suspend fun testTimestampIntervals(asrEngine: NexaAsrEngine) {
        withContext(Dispatchers.IO) {
            val testFile = testAudioPaths.firstOrNull { File(it).exists() }
            if (testFile == null) {
                appendOutput("No valid audio file found for timestamp test\n")
                return@withContext
            }
            
            appendOutput("Testing with: $testFile\n\n")
            
            try {
                val asrWrapper = getAsrWrapperForInspection(asrEngine)
                if (asrWrapper != null) {
                    val transcribeResult = asrWrapper.transcribe(
                        com.nexa.sdk.bean.AsrTranscribeInput(
                            audioPath = testFile,
                            language = "en",
                            config = null
                        )
                    )
                    
                    transcribeResult.onSuccess { output ->
                        appendOutput("Transcript: ${output.result.transcript?.take(200)}...\n\n")
                        
                        // Examine timestamps
                        val timestamps = output.result.timestamps
                        if (timestamps != null && timestamps.isNotEmpty()) {
                            appendOutput("Timestamps found: ${timestamps.size} entries\n")
                            
                            val intervals = mutableListOf<Float>()
                            for (i in 1 until timestamps.size) {
                                intervals.add(timestamps[i] - timestamps[i-1])
                            }
                            
                            if (intervals.isNotEmpty()) {
                                val avgInterval = intervals.average()
                                val minInterval = intervals.minOrNull() ?: 0f
                                val maxInterval = intervals.maxOrNull() ?: 0f
                                
                                appendOutput("Average timestamp interval: ${String.format("%.1f", avgInterval)}ms\n")
                                appendOutput("Min interval: ${String.format("%.1f", minInterval)}ms\n")
                                appendOutput("Max interval: ${String.format("%.1f", maxInterval)}ms\n\n")
                                
                                appendOutput("First 10 timestamps:\n")
                                for (i in 0 until minOf(10, timestamps.size)) {
                                    val intervalStr = if (i > 0) {
                                        " (${String.format("%.1f", timestamps[i] - timestamps[i-1])}ms)"
                                    } else ""
                                    appendOutput("  [$i] ${String.format("%.1f", timestamps[i])}ms$intervalStr\n")
                                }
                            }
                        } else {
                            appendOutput("No timestamps found in result\n")
                            appendOutput("Timestamps may not be enabled by default\n")
                        }
                        
                        appendOutput("\nProfiling data from SDK:\n")
                        appendOutput("  TTFT: ${String.format("%.2f", output.profileData.ttftMs)}ms\n")
                        appendOutput("  Prompt time: ${String.format("%.2f", output.profileData.promptTimeMs)}ms\n")
                        appendOutput("  Decode time: ${String.format("%.2f", output.profileData.decodeTimeMs)}ms\n")
                        appendOutput("  Audio duration: ${output.profileData.audioDurationMs}ms\n")
                        appendOutput("  SDK RTF: ${String.format("%.3f", output.profileData.realTimeFactor)}\n")
                        appendOutput("  Prefill speed: ${String.format("%.2f", output.profileData.prefillSpeed)} tokens/s\n")
                        appendOutput("  Decoding speed: ${String.format("%.2f", output.profileData.decodingSpeed)} tokens/s\n")
                        
                    }.onFailure { error ->
                        appendOutput("Timestamp test failed: ${error.message}\n")
                    }
                } else {
                    appendOutput("Could not access ASR wrapper for detailed inspection\n")
                }
            } catch (e: Exception) {
                appendOutput("Error examining timestamps: ${e.message}\n")
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun getAsrWrapperForInspection(asrEngine: NexaAsrEngine): com.nexa.sdk.AsrWrapper? {
        return try {
            asrEngine.ensureReady()
            
            val field = asrEngine.javaClass.getDeclaredField("asrWrapper")
            field.isAccessible = true
            field.get(asrEngine) as? com.nexa.sdk.AsrWrapper
        } catch (e: Exception) {
            appendOutput("Warning: Could not access AsrWrapper via reflection: ${e.message}\n")
            null
        }
    }
    
    private suspend fun printSampleResult(asrEngine: NexaAsrEngine, audioPath: String, audioNum: Int) {
        withContext(Dispatchers.IO) {
            appendOutput("--- Sample Result Structure (Audio $audioNum) ---\n")
            
            try {
                val asrWrapper = getAsrWrapperForInspection(asrEngine)
                if (asrWrapper != null) {
                    val transcribeResult = asrWrapper.transcribe(
                        com.nexa.sdk.bean.AsrTranscribeInput(
                            audioPath = audioPath,
                            language = "en",
                            config = null
                        )
                    )
                    
                    transcribeResult.onSuccess { output ->
                        appendOutput("AsrTranscribeOutput {\n")
                        appendOutput("  result: AsrResult {\n")
                        
                        val transcript = output.result.transcript
                        if (transcript != null) {
                            val preview = if (transcript.length > 100) {
                                "${transcript.take(100)}..."
                            } else {
                                transcript
                            }
                            appendOutput("    transcript: \"$preview\"\n")
                            appendOutput("    transcript.length: ${transcript.length}\n")
                        } else {
                            appendOutput("    transcript: null\n")
                        }
                        
                        val confidenceScores = output.result.confidenceScores
                        if (confidenceScores != null && confidenceScores.isNotEmpty()) {
                            appendOutput("    confidenceScores: [${confidenceScores.size} entries]\n")
                            appendOutput("      First 5: ${confidenceScores.take(5)}\n")
                        } else {
                            appendOutput("    confidenceScores: ${confidenceScores?.size ?: "null"}\n")
                        }
                        
                        val timestamps = output.result.timestamps
                        if (timestamps != null && timestamps.isNotEmpty()) {
                            appendOutput("    timestamps: [${timestamps.size} entries]\n")
                            appendOutput("      First 10: ${timestamps.take(10)}\n")
                            
                            if (timestamps.size > 1) {
                                val intervals = (1 until minOf(10, timestamps.size)).map { i ->
                                    timestamps[i] - timestamps[i - 1]
                                }
                                appendOutput("      Intervals: $intervals\n")
                            }
                        } else {
                            appendOutput("    timestamps: ${timestamps?.size ?: "null"}\n")
                        }
                        
                        appendOutput("  }\n")
                        
                        appendOutput("  profileData: ProfilingData {\n")
                        appendOutput("    ttftMs: ${String.format("%.2f", output.profileData.ttftMs)}\n")
                        appendOutput("    promptTimeMs: ${String.format("%.2f", output.profileData.promptTimeMs)}\n")
                        appendOutput("    decodeTimeMs: ${String.format("%.2f", output.profileData.decodeTimeMs)}\n")
                        appendOutput("    audioDurationMs: ${output.profileData.audioDurationMs}\n")
                        appendOutput("    realTimeFactor: ${String.format("%.3f", output.profileData.realTimeFactor)}\n")
                        appendOutput("    prefillSpeed: ${String.format("%.2f", output.profileData.prefillSpeed)} tokens/s\n")
                        appendOutput("    decodingSpeed: ${String.format("%.2f", output.profileData.decodingSpeed)} tokens/s\n")
                        appendOutput("    promptTokens: ${output.profileData.promptTokens}\n")
                        appendOutput("    generatedTokens: ${output.profileData.generatedTokens}\n")
                        appendOutput("    stopReason: \"${output.profileData.stopReason}\"\n")
                        appendOutput("  }\n")
                        appendOutput("}\n\n")
                        
                    }.onFailure { error ->
                        appendOutput("Failed to get sample result: ${error.message}\n\n")
                    }
                } else {
                    appendOutput("Could not access AsrWrapper for sample result\n\n")
                }
            } catch (e: Exception) {
                appendOutput("Error printing sample result: ${e.message}\n\n")
            }
        }
    }
    
    private fun getAudioDuration(audioFile: File): Long {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(audioFile.absolutePath)
            val duration = retriever.extractMetadata(
                android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L
            retriever.release()
            duration
        } catch (e: Exception) {
            0L
        }
    }
    
    private suspend fun appendOutput(text: String) {
        withContext(Dispatchers.Main) {
            outputText.append(text)
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }
}
