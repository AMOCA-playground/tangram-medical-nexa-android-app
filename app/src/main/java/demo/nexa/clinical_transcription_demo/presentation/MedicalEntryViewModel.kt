package demo.nexa.clinical_transcription_demo.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import demo.nexa.clinical_transcription_demo.data.repository.MedicalEntryRepository
import demo.nexa.clinical_transcription_demo.domain.model.MedicalEntry
import demo.nexa.clinical_transcription_demo.domain.model.MedicalEntryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class MedicalEntryUiState(
    val entries: List<MedicalEntry> = emptyList(),
    val selectedFilter: MedicalEntryType? = null,
    val isLoading: Boolean = false
)

class MedicalEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MedicalEntryRepository.getInstance(application)
    
    private val _selectedFilter = MutableStateFlow<MedicalEntryType?>(null)
    
    val uiState: StateFlow<MedicalEntryUiState> = combine(
        repository.observeAll(),
        _selectedFilter
    ) { entries, filter ->
        val filteredEntries = if (filter == null) entries else entries.filter { it.type == filter }
        MedicalEntryUiState(entries = filteredEntries, selectedFilter = filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MedicalEntryUiState())

    fun setFilter(filter: MedicalEntryType?) {
        _selectedFilter.value = filter
    }

    fun addGeneralNote(title: String, content: String, patientName: String?) {
        viewModelScope.launch {
            val entry = MedicalEntry(
                id = UUID.randomUUID().toString(),
                type = MedicalEntryType.GENERAL_NOTE,
                title = title,
                content = content,
                createdAtEpochMs = System.currentTimeMillis(),
                patientName = patientName
            )
            repository.saveEntry(entry)
        }
    }

    fun addPrescription(medicationName: String, dosage: String, content: String, patientName: String?) {
        viewModelScope.launch {
            val entry = MedicalEntry(
                id = UUID.randomUUID().toString(),
                type = MedicalEntryType.PRESCRIPTION,
                title = "Prescription: $medicationName",
                content = content,
                createdAtEpochMs = System.currentTimeMillis(),
                patientName = patientName,
                medicationName = medicationName,
                dosage = dosage
            )
            repository.saveEntry(entry)
        }
    }

    fun addLabTest(title: String, labResult: String, content: String, patientName: String?) {
        viewModelScope.launch {
            val entry = MedicalEntry(
                id = UUID.randomUUID().toString(),
                type = MedicalEntryType.LAB_TEST,
                title = title,
                content = content,
                createdAtEpochMs = System.currentTimeMillis(),
                patientName = patientName,
                labResult = labResult,
                status = "Completed"
            )
            repository.saveEntry(entry)
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            repository.deleteEntry(id)
        }
    }
}
