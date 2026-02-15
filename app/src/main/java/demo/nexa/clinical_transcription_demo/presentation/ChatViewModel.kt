package demo.nexa.clinical_transcription_demo.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexa.sdk.bean.ChatMessage
import demo.nexa.clinical_transcription_demo.llm.LlmGenerationResult
import demo.nexa.clinical_transcription_demo.llm.NexaLlmEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessageUi(
    val role: String,
    val content: String,
    val isError: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessageUi> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = ""
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val llmEngine = NexaLlmEngine.getInstance(application)
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
    
    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isLoading) return
        
        val userMessage = ChatMessageUi("user", text)
        _uiState.update { 
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true
            )
        }
        
        viewModelScope.launch {
            val history = _uiState.value.messages.map { ChatMessage(it.role, it.content) }
            val assistantMessageIndex = _uiState.value.messages.size
            
            _uiState.update { 
                it.copy(messages = it.messages + ChatMessageUi("assistant", ""))
            }
            
            var assistantContent = ""
            
            llmEngine.chatWithMedicalAssistant(history).collect { result ->
                when (result) {
                    is LlmGenerationResult.Token -> {
                        assistantContent += result.text
                        updateAssistantMessage(assistantMessageIndex, assistantContent)
                    }
                    is LlmGenerationResult.Completed -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is LlmGenerationResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                messages = it.messages.dropLast(1) + ChatMessageUi(
                                    "assistant", 
                                    "Error: ${result.throwable.message}",
                                    isError = true
                                )
                            )
                        }
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

    fun clearChat() {
        _uiState.update { ChatUiState() }
    }
}
