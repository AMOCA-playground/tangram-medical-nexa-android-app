package demo.nexa.clinical_transcription_demo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_entries")
data class MedicalEntryEntity(
    @PrimaryKey
    val id: String,
    val type: String, // Enum name: GENERAL_NOTE, PRESCRIPTION, LAB_TEST
    val title: String,
    val content: String,
    val createdAtEpochMs: Long,
    val patientName: String? = null,
    val medicationName: String? = null,
    val dosage: String? = null,
    val labResult: String? = null,
    val status: String? = null
)
