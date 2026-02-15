package demo.nexa.clinical_transcription_demo.data.repository

import android.content.Context
import demo.nexa.clinical_transcription_demo.data.local.AppDatabase
import demo.nexa.clinical_transcription_demo.data.local.dao.MedicalEntryDao
import demo.nexa.clinical_transcription_demo.data.local.entity.MedicalEntryEntity
import demo.nexa.clinical_transcription_demo.domain.model.MedicalEntry
import demo.nexa.clinical_transcription_demo.domain.model.MedicalEntryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MedicalEntryRepository private constructor(private val dao: MedicalEntryDao) {

    fun observeAll(): Flow<List<MedicalEntry>> = dao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    fun observeByType(type: MedicalEntryType): Flow<List<MedicalEntry>> = 
        dao.observeByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun saveEntry(entry: MedicalEntry) {
        dao.insert(entry.toEntity())
    }

    suspend fun deleteEntry(id: String) {
        dao.deleteById(id)
    }

    private fun MedicalEntryEntity.toDomain() = MedicalEntry(
        id = id,
        type = MedicalEntryType.valueOf(type),
        title = title,
        content = content,
        createdAtEpochMs = createdAtEpochMs,
        patientName = patientName,
        medicationName = medicationName,
        dosage = dosage,
        labResult = labResult,
        status = status
    )

    private fun MedicalEntry.toEntity() = MedicalEntryEntity(
        id = id,
        type = type.name,
        title = title,
        content = content,
        createdAtEpochMs = createdAtEpochMs,
        patientName = patientName,
        medicationName = medicationName,
        dosage = dosage,
        labResult = labResult,
        status = status
    )

    companion object {
        @Volatile
        private var INSTANCE: MedicalEntryRepository? = null

        fun getInstance(context: Context): MedicalEntryRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getInstance(context)
                INSTANCE ?: MedicalEntryRepository(database.medicalEntryDao()).also { INSTANCE = it }
            }
        }
    }
}
