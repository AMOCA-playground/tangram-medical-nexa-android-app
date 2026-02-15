package demo.nexa.clinical_transcription_demo.ui.state

data class NoteUiState(
    val id: String,
    val title: String,
    val date: String,
    val duration: String,
    val hasTranscript: Boolean = false,
    val isProcessing: Boolean = false,
    val status: demo.nexa.clinical_transcription_demo.domain.model.NoteStatus =
        demo.nexa.clinical_transcription_demo.domain.model.NoteStatus.NEW,
    val source: demo.nexa.clinical_transcription_demo.domain.model.NoteSource =
        demo.nexa.clinical_transcription_demo.domain.model.NoteSource.RECORDED,
    val patientName: String? = null,
    val patientId: String? = null,
    val visitType: String? = null,
    val clinicianName: String? = null,
    val department: String? = null,
    val priority: String? = null,
    val tags: List<String>? = null
)
