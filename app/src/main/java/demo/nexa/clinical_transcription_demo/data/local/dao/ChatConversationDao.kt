package demo.nexa.clinical_transcription_demo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import demo.nexa.clinical_transcription_demo.data.local.entity.ChatConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chat conversations.
 */
@Dao
interface ChatConversationDao {

    @Query("SELECT * FROM chat_conversations ORDER BY lastModifiedEpochMs DESC")
    fun observeAll(): Flow<List<ChatConversationEntity>>

    @Query("SELECT * FROM chat_conversations WHERE id = :id")
    fun observeById(id: String): Flow<ChatConversationEntity?>

    @Query("SELECT * FROM chat_conversations WHERE id = :id")
    suspend fun getById(id: String): ChatConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ChatConversationEntity)

    @Delete
    suspend fun delete(conversation: ChatConversationEntity)

    @Query("DELETE FROM chat_conversations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM chat_conversations")
    suspend fun deleteAll()
}

