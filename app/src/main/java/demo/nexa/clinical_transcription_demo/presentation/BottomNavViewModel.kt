package demo.nexa.clinical_transcription_demo.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for managing bottom navigation bar state.
 */
class BottomNavViewModel : ViewModel() {

    private val _selectedNavItem = MutableStateFlow("chat")
    val selectedNavItem: StateFlow<String> = _selectedNavItem.asStateFlow()

    fun selectNavItem(itemId: String) {
        _selectedNavItem.update { itemId }
    }
}

