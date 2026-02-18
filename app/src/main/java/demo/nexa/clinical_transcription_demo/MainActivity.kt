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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import demo.nexa.clinical_transcription_demo.data.mapper.toUiState
import demo.nexa.clinical_transcription_demo.data.repository.NotesRepository
import demo.nexa.clinical_transcription_demo.presentation.MainViewModel
import demo.nexa.clinical_transcription_demo.presentation.RecordingViewModel
import demo.nexa.clinical_transcription_demo.ui.component.AppBottomNavigationBar
import demo.nexa.clinical_transcription_demo.ui.component.LoadingOverlay
import demo.nexa.clinical_transcription_demo.ui.screen.ChatHomeScreen
import demo.nexa.clinical_transcription_demo.ui.screen.MedicalEntriesScreen
import demo.nexa.clinical_transcription_demo.ui.screen.NoteDetailScreen
import demo.nexa.clinical_transcription_demo.ui.screen.NotesListScreen
import demo.nexa.clinical_transcription_demo.ui.screen.RecordingScreen
import demo.nexa.clinical_transcription_demo.ui.screen.SettingsScreen
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
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel()
                val isImporting by mainViewModel.isImporting.collectAsState()
                
                // Track recording session key to reset RecordingViewModel
                var recordingSessionKey by remember { mutableIntStateOf(0) }
                
                val notes by repository.observeAllNotes()
                    .map { domainNotes -> domainNotes.map { it.toUiState() } }
                    .collectAsState(initial = emptyList())
                
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
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // Hide bottom bar on specific screens
                val showBottomBar = currentRoute in listOf("chat", "notes_list", "settings")

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            AppBottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = "chat"
                            ) {
                                composable("chat") {
                                    ChatHomeScreen(
                                        onNotesClick = { navController.navigate("notes_list") },
                                        onRecordClick = { 
                                            recordingSessionKey++
                                            navController.navigate("recording") 
                                        }
                                    )
                                }
                                
                                composable("recording") {
                                    val recordingViewModel: RecordingViewModel = viewModel(key = recordingSessionKey.toString())
                                    RecordingScreen(
                                        viewModel = recordingViewModel,
                                        onBackClick = { navController.popBackStack() },
                                        onRecordingSaved = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                                
                                composable("notes_list") {
                                    NotesListScreen(
                                        notes = notes,
                                        onNoteClick = { note -> 
                                            navController.navigate("note_detail/${note.id}")
                                        },
                                        onRecordClick = { 
                                            recordingSessionKey++
                                            navController.navigate("recording") 
                                        },
                                        onImportClick = { 
                                            importLauncher.launch("audio/*")
                                        },
                                        onChatClick = {
                                            navController.navigate("chat")
                                        },
                                        onMedicalRecordsClick = {
                                            navController.navigate("medical_records")
                                        },
                                        onTestAsrClick = {
                                            startActivity(Intent(this@MainActivity, TestAsrActivity::class.java))
                                        },
                                        onTestLlmClick = { 
                                            navController.navigate("soap_note")
                                         }
                                    )
                                }
                                
                                composable(
                                    route = "note_detail/{noteId}",
                                    arguments = listOf(navArgument("noteId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val noteId = backStackEntry.arguments?.getString("noteId")
                                    noteId?.let { id ->
                                        NoteDetailScreen(
                                            noteId = id,
                                            onBackClick = { navController.popBackStack() }
                                        )
                                    }
                                }
                                
                                composable("medical_records") {
                                    MedicalEntriesScreen(
                                        onBackClick = { navController.popBackStack() }
                                    )
                                }
                                
                                composable("soap_note") {
                                    SoapScreen()
                                }
                                
                                composable("settings") {
                                    SettingsScreen()
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
    }
}
