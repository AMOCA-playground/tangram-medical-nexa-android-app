package demo.nexa.clinical_transcription_demo.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexa.sdk.bean.ChatMessage
import demo.nexa.clinical_transcription_demo.data.repository.ChatRepository
import demo.nexa.clinical_transcription_demo.domain.model.ChatMessage as DomainChatMessage
import demo.nexa.clinical_transcription_demo.llm.LlmGenerationResult
import demo.nexa.clinical_transcription_demo.llm.NexaLlmEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessageUi(
    val role: String,
    val content: String,
    val isError: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessageUi> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val suggestedPrompts: List<String> = emptyList(),
    val conversationId: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val llmEngine = NexaLlmEngine.getInstance(application)
    private val chatRepository = ChatRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    init {
        initializeConversation()
        loadSuggestedPrompts()
    }

    /**
     * Initialize a new conversation on ViewModel creation.
     */
    private fun initializeConversation() {
        viewModelScope.launch {
            try {
                val conversation = chatRepository.getOrCreateConversation()
                _uiState.update { it.copy(conversationId = conversation.id) }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to initialize conversation", e)
            }
        }
    }

    /**
     * Load suggested prompts based on recent topics.
     */
    private fun loadSuggestedPrompts() {
        viewModelScope.launch {
            try {
                val prompts = chatRepository.getSuggestedPrompts()
                _uiState.update { it.copy(suggestedPrompts = prompts) }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to load suggested prompts", e)
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
    
    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isLoading) return
        
        val conversationId = _uiState.value.conversationId ?: return

        val userMessage = ChatMessageUi("user", text)
        _uiState.update { 
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true
            )
        }
        
        // Persist user message
        chatRepository.saveMessage(conversationId, "user", text)

        viewModelScope.launch {
            val history = _uiState.value.messages.map { ChatMessage(it.role, it.content) }
            val assistantMessageIndex = _uiState.value.messages.size
            
            _uiState.update { 
                it.copy(messages = it.messages + ChatMessageUi("assistant", ""))
            }
            
            var assistantContent = ""
            var isError = false

            llmEngine.chatWithMedicalAssistant(history).collect { result ->
                when (result) {
                    is LlmGenerationResult.Token -> {
                        assistantContent += result.text
                        updateAssistantMessage(assistantMessageIndex, assistantContent)
                    }
                    is LlmGenerationResult.Completed -> {
                        _uiState.update { it.copy(isLoading = false) }
                        // Persist assistant message
                        chatRepository.saveMessage(conversationId, "assistant", assistantContent)
                    }
                    is LlmGenerationResult.Error -> {
                        isError = true
                        assistantContent = "Error: ${result.throwable.message}"
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                messages = it.messages.dropLast(1) + ChatMessageUi(
                                    "assistant", 
                                    assistantContent,
                                    isError = true
                                )
                            )
                        }
                        // Persist error message
                        chatRepository.saveMessage(conversationId, "assistant", assistantContent, isError = true)
                    }
                }
            }
        }
    }
    
    private fun updateAssistantMessage(index: Int, content: String) {
        _uiState.update { state ->
            val newMessages = state.messages.toMutableList()
            if (index < newMessages.size) {
                newMessages[index] = newMessages[index].copy(content = content)
            }
            state.copy(messages = newMessages)
        }
    }

    /**
     * Clear current chat and start a new conversation.
     */
    fun clearChat() {
        viewModelScope.launch {
            val conversationId = _uiState.value.conversationId
            if (conversationId != null) {
                try {
                    chatRepository.clearConversation(conversationId)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Failed to clear conversation", e)
                }
            }

            // Initialize new conversation
            _uiState.update { ChatUiState() }
            initializeConversation()
            loadSuggestedPrompts()
        }
    }

    /**
     * Set input text from a suggested prompt.
     */
    fun selectSuggestedPrompt(prompt: String) {
        _uiState.update { it.copy(inputText = prompt) }
    }
}
