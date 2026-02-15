package demo.nexa.clinical_transcription_demo.domain.model

enum class MedicalEntryType {
    GENERAL_NOTE,
    PRESCRIPTION,
    LAB_TEST
}

data class MedicalEntry(
    val id: String,
    val type: MedicalEntryType,
    val title: String,
    val content: String,
    val createdAtEpochMs: Long,
    val patientName: String? = null,
    val medicationName: String? = null, // For prescriptions
    val dosage: String? = null,         // For prescriptions
    val labResult: String? = null,      // For lab tests
    val status: String? = null          // e.g., "Pending", "Completed"
)
