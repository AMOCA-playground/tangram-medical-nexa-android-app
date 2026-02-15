package demo.nexa.clinical_transcription_demo.ui.soap

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import demo.nexa.clinical_transcription_demo.ui.soap.AssessmentScreen
import demo.nexa.clinical_transcription_demo.ui.soap.ObjectiveScreen
import demo.nexa.clinical_transcription_demo.ui.soap.PlanScreen
import demo.nexa.clinical_transcription_demo.ui.soap.SubjectiveScreen

@Composable
fun SoapScreen() {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Subjective", "Objective", "Assessment", "Plan")

    Column {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        when (tabIndex) {
            0 -> SubjectiveScreen()
            1 -> ObjectiveScreen()
            2 -> AssessmentScreen()
            3 -> PlanScreen()
        }
    }
}