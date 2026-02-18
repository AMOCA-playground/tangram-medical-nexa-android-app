package demo.nexa.clinical_transcription_demo.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import demo.nexa.clinical_transcription_demo.asr.NexaAsrEngine
import demo.nexa.clinical_transcription_demo.audio.WaveformExtractor
import demo.nexa.clinical_transcription_demo.common.BackgroundProgressManager
import demo.nexa.clinical_transcription_demo.data.audio.AudioFileManager
import demo.nexa.clinical_transcription_demo.data.local.AppDatabase
import demo.nexa.clinical_transcription_demo.data.mapper.toDomain
import demo.nexa.clinical_transcription_demo.data.mapper.toEntity
import demo.nexa.clinical_transcription_demo.domain.model.NoteSource
import demo.nexa.clinical_transcription_demo.domain.model.NoteStatus
import demo.nexa.clinical_transcription_demo.domain.model.RecordingNote
import demo.nexa.clinical_transcription_demo.llm.NexaLlmEngine
import demo.nexa.clinical_transcription_demo.llm.SoapGenerationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import kotlin.math.absoluteValue

/**
 * Repository for managing notes.
 * Thin layer over DAO + file operations.
 * 
 * Maintains its own coroutine scope for background work (transcription, summary generation)
 * that survives ViewModel lifecycle.
 */
class NotesRepository(
    private val database: AppDatabase,
    private val audioFileManager: AudioFileManager,
    private val context: Context
) {
    
    private val dao = database.recordingNoteDao()
    private val waveformExtractor = WaveformExtractor.getInstance()
    private val progressManager = BackgroundProgressManager.getInstance()
    
    // Background scope for long-running tasks (transcription, summary generation)
    // Uses SupervisorJob so individual task failures don't cancel other tasks
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * Observe all notes as a Flow.
     */
    fun observeAllNotes(): Flow<List<RecordingNote>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Observe a single note by ID.
     */
    fun observeNoteById(id: String): Flow<RecordingNote?> {
        return dao.observeById(id).map { it?.toDomain() }
    }
    
    /**
     * Get a single note by ID (one-shot, not reactive).
     */
    suspend fun getNoteById(id: String): RecordingNote? {
        return dao.getById(id)?.toDomain()
    }

    /**
     * Create a new multifunctional note.
     */
    suspend fun createNote(
        title: String,
        source: NoteSource,
        audioFile: File? = null,
        audioUri: Uri? = null,
        audioExtension: String = "m4a",
        durationMs: Long? = null,
        transcriptText: String? = null,
        summaryText: String? = null
    ): RecordingNote {
        val noteId = if (source == NoteSource.RECORDED && audioFile != null) {
            audioFile.nameWithoutExtension
        } else {
            UUID.randomUUID().toString()
        }
        
        val now = System.currentTimeMillis()
        val metadata = generateClinicalMetadata(noteId, now)

        var finalAudioFileName = ""
        var finalWaveformData: List<Float>? = null

        when (source) {
            NoteSource.RECORDED -> {
                if (audioFile != null) {
                    finalAudioFileName = audioFile.name
                    finalWaveformData = waveformExtractor.extractWaveform(audioFile, targetSampleCount = 200).getOrNull()
                }
            }
            NoteSource.IMPORTED -> {
                if (audioUri != null) {
                    finalAudioFileName = audioFileManager.copyImportedAudio(audioUri, noteId, audioExtension)
                    val importedFile = audioFileManager.getAudioFile(finalAudioFileName)
                    finalWaveformData = waveformExtractor.extractWaveform(importedFile, targetSampleCount = 200).getOrNull()
                }
            }
            NoteSource.TEXT -> {
                // No audio for text notes
            }
        }

        val note = RecordingNote(
            id = noteId,
            createdAtEpochMs = now,
            title = title,
            audioFileName = finalAudioFileName,
            durationMs = durationMs,
            source = source,
            status = if (source == NoteSource.TEXT) NoteStatus.DONE else NoteStatus.NEW,
            transcriptText = transcriptText,
            summaryText = summaryText,
            errorMessage = null,
            waveformData = finalWaveformData,
            patientName = metadata.patientName,
            patientId = metadata.patientId,
            visitType = metadata.visitType,
            clinicianName = metadata.clinicianName,
            department = metadata.department,
            priority = metadata.priority,
            tags = metadata.tags
        )
        
        dao.insert(note.toEntity())
        return note
    }
    
    /**
     * Create a new note for a recording.
     * @deprecated Use [createNote] instead.
     */
    suspend fun createRecordedNote(
        id: String,
        title: String,
        audioFile: File,
        durationMs: Long? = null
    ): RecordingNote {
        return createNote(
            title = title,
            source = NoteSource.RECORDED,
            audioFile = audioFile,
            durationMs = durationMs
        )
    }
    
    /**
     * Create a new note for an imported audio file.
     * @deprecated Use [createNote] instead.
     */
    suspend fun createImportedNote(
        title: String,
        sourceUri: Uri,
        extension: String = "m4a",
        durationMs: Long? = null
    ): RecordingNote {
        return createNote(
            title = title,
            source = NoteSource.IMPORTED,
            audioUri = sourceUri,
            audioExtension = extension,
            durationMs = durationMs
        )
    }
    
    /**
     * Update the transcript for a note.
     */
    suspend fun updateTranscript(id: String, text: String, status: NoteStatus) {
        dao.updateTranscript(id, text, status.name)
    }
    
    /**
     * Update the summary for a note.
     */
    suspend fun updateSummary(id: String, text: String, status: NoteStatus) {
        dao.updateSummary(id, text, status.name)
    }
    
    /**
     * Update the status of a note.
     */
    suspend fun updateStatus(id: String, status: NoteStatus, errorMessage: String? = null) {
        dao.updateStatus(id, status.name, errorMessage)
    }
    
    /**
     * Delete a note and its associated audio file.
     */
    suspend fun deleteNote(id: String) {
        val note = dao.getById(id)
        if (note != null) {
            if (note.audioFileName.isNotEmpty()) {
                audioFileManager.deleteAudioFile(note.audioFileName)
            }
            dao.deleteById(id)
        }
    }
    
    /**
     * Get the audio file for a note.
     */
    fun getAudioFile(note: RecordingNote): File {
        return audioFileManager.getAudioFile(note.audioFileName)
    }
    
    /**
     * Delete all notes and audio files (use with caution).
     */
    suspend fun deleteAllNotes() {
        audioFileManager.deleteAllAudioFiles()
        dao.deleteAll()
    }
    
    /**
     * Transcribe a note's audio file using Nexa ASR.
     * Updates the note's status and transcript text in the database.
     * Runs in background scope, independent of ViewModel lifecycle.
     * 
     * @param noteId The note ID to transcribe
     * @param language Language code for transcription (default: "en")
     */
    fun startTranscription(noteId: String, language: String = "en") {
        backgroundScope.launch {
            try {
                val note = dao.getById(noteId) ?: run {
                    Log.w(TAG, "Note not found for transcription")
                    return@launch
                }
                
                if (note.audioFileName.isEmpty()) {
                    Log.w(TAG, "No audio file for transcription")
                    return@launch
                }

                dao.updateStatus(noteId, NoteStatus.TRANSCRIBING.name, null)
                
                val audioFile = audioFileManager.getAudioFile(note.audioFileName)
                if (!audioFile.exists()) {
                    val error = "Audio file not found"
                    Log.e(TAG, error)
                    dao.updateStatus(noteId, NoteStatus.ERROR.name, error)
                    progressManager.stopProgress(noteId, BackgroundProgressManager.ProgressType.TRANSCRIPTION)
                    return@launch
                }
                
                val result = NexaAsrEngine.getInstance(context).transcribe(
                    audioPath = audioFile.absolutePath,
                    language = language
                )
                
                result.onSuccess { transcript ->
                    dao.updateTranscript(noteId, transcript, NoteStatus.DONE.name)
                    progressManager.stopProgress(noteId, BackgroundProgressManager.ProgressType.TRANSCRIPTION)
                }.onFailure { error ->
                    Log.e(TAG, "Transcription failed", error)
                    dao.updateStatus(noteId, NoteStatus.ERROR.name, "Transcription failed: ${error.message}")
                    progressManager.stopProgress(noteId, BackgroundProgressManager.ProgressType.TRANSCRIPTION)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during transcription", e)
                dao.updateStatus(noteId, NoteStatus.ERROR.name, e.message)
                progressManager.stopProgress(noteId, BackgroundProgressManager.ProgressType.TRANSCRIPTION)
            }
        }
    }
    
    /**
     * Start background summary generation for a note.
     * Runs in background scope and survives ViewModel lifecycle.
     * Updates note status to SUMMARIZING and handles completion/errors automatically.
     * Starts progress simulation based on transcript length.
     * 
     * @param noteId The note ID to generate summary for
     */
    fun startSummaryGeneration(noteId: String) {
        backgroundScope.launch {
            try {
                val note = dao.getById(noteId) ?: run {
                    Log.w(TAG, "Note not found for summary generation")
                    return@launch
                }
                
                if (note.transcriptText.isNullOrEmpty()) {
                    val error = "No transcript available for summary generation"
                    Log.w(TAG, error)
                    dao.updateStatus(noteId, NoteStatus.ERROR.name, error)
                    progressManager.stopProgress(noteId, BackgroundProgressManager.ProgressType.SUMMARY)
                    return@launch
                }
                
                dao.updateStatus(noteId, NoteStatus.SUMMARIZING.name, null)
                
                val transcriptLength = note.transcriptText.length
                val isShortTranscript = transcriptLength < NexaLlmEngine.SEGMENT_SIZE
                
                if (isShortTranscript) {
                    val promptLength = NexaLlmEngine.SOAP_SYSTEM_PROMPT.length +
                                      NexaLlmEngine.SOAP_USER_PREFIX.length
                    
                    progressManager.startSummarySinglePhaseProgress(
                        noteId = noteId,
                        transcriptLength = transcriptLength,
                        msPerChar = NexaLlmEngine.SOAP_CREATOR_MS_PER_CHAR,
                        promptLength = promptLength
                    )
                } else {
                    val promptLength = NexaLlmEngine.SECTION_SUMMARIZER_PROMPT.length +
                                      NexaLlmEngine.SUMMARIZER_USER_PREFIX.length
                    
                    progressManager.startSummaryPhase1Progress(
                        noteId = noteId,
                        transcriptLength = transcriptLength,
                        segmentSize = NexaLlmEngine.SEGMENT_SIZE,
                        msPerChar = NexaLlmEngine.SUMMARIZER_MS_PER_CHAR,
                        promptLength = promptLength
                    )
                }
                
                val summaryBuilder = StringBuilder()
                
                NexaLlmEngine.getInstance(context).generateSoapSummary(note.transcriptText).collect { result ->
                    when (result) {
                        is SoapGenerationResult.Token -> {
                            summaryBuilder.append(result.text)
                        }
                        is SoapGenerationResult.Completed -> {
                            dao.updateSummary(noteId, summaryBuilder.toString(), NoteStatus.DONE.name)
                            progressManager.stopProgress(noteId, BackgroundProgressManager.ProgressType.SUMMARY)
                        }
                        is SoapGenerationResult.Error -> {
                            val errorMessage = "Summary generation failed: ${result.throwable.message}"
                            Log.e(TAG, errorMessage, result.throwable)
                            dao.updateStatus(noteId, NoteStatus.ERROR.name, errorMessage)
                            progressManager.stopProgress(noteId, BackgroundProgressManager.ProgressType.SUMMARY)
                        }
                        is SoapGenerationResult.SummarizerCompleted -> {
                            val promptLength = NexaLlmEngine.SOAP_SYSTEM_PROMPT.length +
                                              NexaLlmEngine.SOAP_USER_PREFIX.length
                            
                            progressManager.startSummaryPhase2Progress(
                                noteId = noteId,
                                summaryLength = result.totalSummaryLength,
                                msPerChar = NexaLlmEngine.SOAP_CREATOR_MS_PER_CHAR,
                                promptLength = promptLength
                            )
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during summary generation", e)
                dao.updateStatus(noteId, NoteStatus.ERROR.name, e.message)
                progressManager.stopProgress(noteId, BackgroundProgressManager.ProgressType.SUMMARY)
            }
        }
    }
    
    /**
     * Generate a SOAP summary for a note's transcript using Nexa LLM.
     * Updates the note's summary text in the database.
     * 
     * @param noteId The note ID to generate summary for
     * @return Result indicating success or failure
     */
    suspend fun generateSummary(noteId: String): Result<String> {
        return try {
            val note = dao.getById(noteId) ?: run {
                val error = "Note not found for summary generation"
                Log.w(TAG, error)
                return Result.failure(IllegalStateException(error))
            }
            
            if (note.transcriptText.isNullOrEmpty()) {
                val error = "No transcript available for summary generation"
                Log.w(TAG, error)
                return Result.failure(IllegalStateException(error))
            }
            
            val result = NexaLlmEngine.getInstance(context).generateSoapSummaryBlocking(
                transcript = note.transcriptText
            )
            
            result.onSuccess { summary ->
                dao.updateSummary(noteId, summary, NoteStatus.DONE.name)
            }.onFailure { error ->
                Log.e(TAG, "Summary generation failed", error)
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during summary generation", e)
            Result.failure(e)
        }
    }
    
    companion object {
        private const val TAG = "NotesRepository"
        @Volatile
        private var INSTANCE: NotesRepository? = null
        
        fun getInstance(context: Context): NotesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotesRepository(
                    database = AppDatabase.getInstance(context),
                    audioFileManager = AudioFileManager.getInstance(context),
                    context = context.applicationContext
                ).also { INSTANCE = it }
            }
        }
    }

    private data class ClinicalMetadata(
        val patientName: String,
        val patientId: String,
        val visitType: String,
        val clinicianName: String,
        val department: String,
        val priority: String,
        val tags: List<String>
    )

    private fun generateClinicalMetadata(noteId: String, createdAtEpochMs: Long): ClinicalMetadata {
        val patientNames = listOf(
            "Avery Wells",
            "Jordan Patel",
            "Riley Chen",
            "Morgan Diaz",
            "Casey Brown",
            "Quinn Li",
            "Taylor Brooks",
            "Cameron Hayes"
        )

        val clinicians = listOf(
            "Dr. Ortega",
            "Dr. Nguyen",
            "Dr. Shah",
            "Dr. Brooks",
            "Dr. Kim",
            "Dr. Adeyemi"
        )

        val departments = listOf(
            "Family Medicine",
            "Cardiology",
            "Neurology",
            "Orthopedics",
            "Emergency",
            "Internal Medicine"
        )

        val visitTypes = listOf(
            "Consult",
            "Follow-up",
            "Procedure",
            "ED",
            "Telehealth"
        )

        val priorities = listOf("Routine", "Urgent")

        val tagSets = listOf(
            listOf("hypertension", "medication review"),
            listOf("pain management", "imaging"),
            listOf("diabetes", "lab follow-up"),
            listOf("post-op", "wound check"),
            listOf("respiratory", "asthma"),
            listOf("cardiac", "EKG")
        )

        val seed = (noteId.hashCode().toLong() xor createdAtEpochMs).absoluteValue

        fun <T> pick(list: List<T>, offset: Long): T {
            val index = ((seed + offset) % list.size).toInt()
            return list[index]
        }

        val patientName = pick(patientNames, 3)
        val clinicianName = pick(clinicians, 7)
        val department = pick(departments, 11)
        val visitType = pick(visitTypes, 17)
        val priority = pick(priorities, 23)
        val tags = pick(tagSets, 31)

        val mrnSuffix = (seed % 900000 + 100000).toInt()
        val patientId = "MRN-$mrnSuffix"

        return ClinicalMetadata(
            patientName = patientName,
            patientId = patientId,
            visitType = visitType,
            clinicianName = clinicianName,
            department = department,
            priority = priority,
            tags = tags
        )
    }
}
