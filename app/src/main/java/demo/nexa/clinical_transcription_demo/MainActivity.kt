package demo.nexa.clinical_transcription_demo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import demo.nexa.clinical_transcription_demo.data.mapper.toUiState
import demo.nexa.clinical_transcription_demo.data.repository.NotesRepository
import demo.nexa.clinical_transcription_demo.presentation.MainViewModel
import demo.nexa.clinical_transcription_demo.presentation.RecordingViewModel
import demo.nexa.clinical_transcription_demo.ui.component.LoadingOverlay
import demo.nexa.clinical_transcription_demo.ui.screen.ChatHomeScreen
import demo.nexa.clinical_transcription_demo.ui.screen.MedicalEntriesScreen
import demo.nexa.clinical_transcription_demo.ui.screen.NoteDetailScreen
import demo.nexa.clinical_transcription_demo.ui.screen.NotesListScreen
import demo.nexa.clinical_transcription_demo.ui.screen.RecordingScreen
import demo.nexa.clinical_transcription_demo.ui.soap.SoapScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    
    private val repository by lazy { NotesRepository.getInstance(this) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Chat) }
                    var recordingSessionKey by remember { mutableStateOf(0) }
                    var selectedNoteId by remember { mutableStateOf<String?>(null) }
                    val mainViewModel: MainViewModel = viewModel()
                    
                    val notes by repository.observeAllNotes()
                        .map { domainNotes -> domainNotes.map { it.toUiState() } }
                        .collectAsState(initial = emptyList())
                    
                    val isImporting by mainViewModel.isImporting.collectAsState()
                    
                    val importLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let { mainViewModel.importAudio(it) }
                    }
                    
                    LaunchedEffect(Unit) {
                        mainViewModel.uiEvents.collectLatest { event ->
                            when (event) {
                                is MainViewModel.UiEvent.ShowToast -> {
                                    Toast.makeText(
                                        this@MainActivity,
                                        event.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            Screen.Recording -> {
                                val recordingViewModel: RecordingViewModel = viewModel(key = recordingSessionKey.toString())
                                RecordingScreen(
                                    viewModel = recordingViewModel,
                                    onBackClick = { currentScreen = Screen.Chat },
                                    onRecordingSaved = {
                                        currentScreen = Screen.Chat
                                    }
                                )
                            }
                            Screen.SoapNote -> {
                                SoapScreen()
                            }
                            Screen.Chat -> {
                                ChatHomeScreen(
                                    onNotesClick = { currentScreen = Screen.NotesList },
                                    onRecordClick = {
                                        recordingSessionKey++
                                        currentScreen = Screen.Recording
                                    }
                                )
                            }
                            Screen.MedicalRecords -> {
                                MedicalEntriesScreen(
                                    onBackClick = { currentScreen = Screen.Chat }
                                )
                            }
                            Screen.NoteDetail -> {
                                selectedNoteId?.let { id ->
                                    NoteDetailScreen(
                                        noteId = id,
                                        onBackClick = { 
                                            currentScreen = Screen.NotesList
                                            selectedNoteId = null 
                                        }
                                    )
                                } ?: run { currentScreen = Screen.Chat }
                            }
                            Screen.NotesList -> {
                                NotesListScreen(
                                    notes = notes,
                                    onNoteClick = { note -> 
                                        selectedNoteId = note.id
                                        currentScreen = Screen.NoteDetail
                                    },
                                    onRecordClick = { 
                                        recordingSessionKey++
                                        currentScreen = Screen.Recording 
                                    },
                                    onImportClick = { 
                                        importLauncher.launch("audio/*")
                                    },
                                    onChatClick = {
                                        currentScreen = Screen.Chat
                                    },
                                    onMedicalRecordsClick = {
                                        currentScreen = Screen.MedicalRecords
                                    },
                                    onTestAsrClick = {
                                        startActivity(Intent(this@MainActivity, TestAsrActivity::class.java))
                                    },
                                    onTestLlmClick = { 
                                        currentScreen = Screen.SoapNote
                                     }
                                )
                            }
                        }
                        
                        if (isImporting) {
                            LoadingOverlay(message = "Importing audio...")
                        }
                    }
                }
            }
        }
    }

    private sealed class Screen {
        object NotesList : Screen()
        object Recording : Screen()
        object SoapNote : Screen()
        object Chat : Screen()
        object MedicalRecords : Screen()
        object NoteDetail : Screen()
    }
}
