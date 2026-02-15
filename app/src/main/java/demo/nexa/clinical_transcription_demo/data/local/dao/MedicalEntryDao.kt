package demo.nexa.clinical_transcription_demo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import demo.nexa.clinical_transcription_demo.data.local.entity.MedicalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalEntryDao {
    @Query("SELECT * FROM medical_entries ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<MedicalEntryEntity>>

    @Query("SELECT * FROM medical_entries WHERE id = :id")
    fun observeById(id: String): Flow<MedicalEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MedicalEntryEntity)

    @Query("DELETE FROM medical_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM medical_entries WHERE type = :type ORDER BY createdAtEpochMs DESC")
    fun observeByType(type: String): Flow<List<MedicalEntryEntity>>
}
