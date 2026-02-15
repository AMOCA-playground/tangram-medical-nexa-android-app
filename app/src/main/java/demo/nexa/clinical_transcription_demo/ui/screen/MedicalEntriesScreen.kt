package demo.nexa.clinical_transcription_demo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import demo.nexa.clinical_transcription_demo.domain.model.MedicalEntry
import demo.nexa.clinical_transcription_demo.domain.model.MedicalEntryType
import demo.nexa.clinical_transcription_demo.presentation.MedicalEntryViewModel
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MedicalEntriesScreen(
    onBackClick: () -> Unit,
    viewModel: MedicalEntryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEntryTypeToAdd by remember { mutableStateOf(MedicalEntryType.GENERAL_NOTE) }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.TealDark
                        )
                    }
                    Text(
                        text = "Medical Records",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TealDark,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                val tabs = listOf("All", "Notes", "Prescriptions", "Labs")
                val selectedTabIndex = when(uiState.selectedFilter) {
                    null -> 0
                    MedicalEntryType.GENERAL_NOTE -> 1
                    MedicalEntryType.PRESCRIPTION -> 2
                    MedicalEntryType.LAB_TEST -> 3
                }
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = AppColors.TealDark,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                viewModel.setFilter(when(index) {
                                    1 -> MedicalEntryType.GENERAL_NOTE
                                    2 -> MedicalEntryType.PRESCRIPTION
                                    3 -> MedicalEntryType.LAB_TEST
                                    else -> null
                                })
                            },
                            text = { Text(title, fontSize = 12.sp) }
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = AppColors.BorderLight)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AppColors.TealDark,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.BackgroundAqua)
        ) {
            if (uiState.entries.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.entries) { entry ->
                        MedicalEntryCard(entry, onDelete = { viewModel.deleteEntry(entry.id) })
                    }
                }
            }
        }

        if (showAddDialog) {
            AddEntryDialog(
                onDismiss = { showAddDialog = false },
                onAddNote = { title, content, patient ->
                    viewModel.addGeneralNote(title, content, patient)
                    showAddDialog = false
                },
                onAddPrescription = { med, dose, content, patient ->
                    viewModel.addPrescription(med, dose, content, patient)
                    showAddDialog = false
                },
                onAddLab = { title, result, content, patient ->
                    viewModel.addLabTest(title, result, content, patient)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No records found", color = AppColors.TextSecondary)
        Text("Tap + to add a new record", fontSize = 14.sp, color = AppColors.TextTertiary)
    }
}

@Composable
fun MedicalEntryCard(entry: MedicalEntry, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(entry.createdAtEpochMs))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = when(entry.type) {
                    MedicalEntryType.GENERAL_NOTE -> Color(0xFF4CAF50)
                    MedicalEntryType.PRESCRIPTION -> Color(0xFF2196F3)
                    MedicalEntryType.LAB_TEST -> Color(0xFFFF9800)
                }
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppColors.TextTertiary, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(dateStr, fontSize = 12.sp, color = AppColors.TextTertiary)
            
            if (entry.patientName != null) {
                Text("Patient: ${entry.patientName}", fontSize = 12.sp, color = AppColors.TealDark, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            if (entry.type == MedicalEntryType.PRESCRIPTION) {
                Text("Medication: ${entry.medicationName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Dosage: ${entry.dosage}", fontSize = 14.sp)
            } else if (entry.type == MedicalEntryType.LAB_TEST) {
                Text("Result: ${entry.labResult}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TealDark)
            }

            Text(entry.content, fontSize = 14.sp, color = AppColors.TextSecondary)
        }
    }
}

@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onAddNote: (String, String, String?) -> Unit,
    onAddPrescription: (String, String, String, String?) -> Unit,
    onAddLab: (String, String, String, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(MedicalEntryType.GENERAL_NOTE) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var patientName by remember { mutableStateOf("") }
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var labResult by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medical Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    MedicalEntryType.values().forEach { type ->
                        val isSelected = selectedType == type
                        TextButton(
                            onClick = { selectedType = type },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (isSelected) AppColors.TealDark else AppColors.TextTertiary
                            )
                        ) {
                            Text(type.name.replace("_", " ").lowercase().capitalize(Locale.ROOT), fontSize = 10.sp)
                        }
                    }
                }

                OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("Patient Name") }, modifier = Modifier.fillMaxWidth())
                
                if (selectedType == MedicalEntryType.PRESCRIPTION) {
                    OutlinedTextField(value = medicationName, onValueChange = { medicationName = it }, label = { Text("Medication Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage") }, modifier = Modifier.fillMaxWidth())
                } else {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                }

                if (selectedType == MedicalEntryType.LAB_TEST) {
                    OutlinedTextField(value = labResult, onValueChange = { labResult = it }, label = { Text("Lab Result") }, modifier = Modifier.fillMaxWidth())
                }

                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Notes/Details") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pName = if (patientName.isBlank()) null else patientName
                    when(selectedType) {
                        MedicalEntryType.GENERAL_NOTE -> onAddNote(title, content, pName)
                        MedicalEntryType.PRESCRIPTION -> onAddPrescription(medicationName, dosage, content, pName)
                        MedicalEntryType.LAB_TEST -> onAddLab(title, labResult, content, pName)
                    }
                },
                enabled = (selectedType != MedicalEntryType.PRESCRIPTION && title.isNotBlank()) || (selectedType == MedicalEntryType.PRESCRIPTION && medicationName.isNotBlank()),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.TealDark)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
