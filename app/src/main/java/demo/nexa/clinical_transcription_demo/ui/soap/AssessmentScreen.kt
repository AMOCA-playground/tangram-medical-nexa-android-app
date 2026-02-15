package demo.nexa.clinical_transcription_demo.ui.soap

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AssessmentScreen() {
    var assessmentNotes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Assessment")
        OutlinedTextField(
            value = assessmentNotes,
            onValueChange = { assessmentNotes = it },
            label = { Text("Enter assessment notes here") },
            modifier = Modifier.fillMaxSize()
        )
    }
}