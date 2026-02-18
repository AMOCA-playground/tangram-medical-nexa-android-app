package demo.nexa.clinical_transcription_demo.domain.model

/**
 * Source of the note.
 */
enum class NoteSource {
    RECORDED,  // Audio recorded within the app
    IMPORTED,  // Audio imported from external source
    TEXT       // Text-only note
}
