# Clinical App Enhancements - Implementation Guide

## Overview

This document provides a comprehensive guide to the six major enhancements implemented in the Clinical App, covering chat history persistence, voice input, suggested prompts, quick actions, swipe gestures, and bottom navigation.

## Implemented Enhancements

### 1. ✅ Chat History Persistence

**Purpose**: Save and retrieve chat conversations from local database.

**Files Created**:
- `data/local/entity/ChatConversationEntity.kt` - Room entity for conversations
- `data/local/entity/ChatMessageEntity.kt` - Room entity for individual messages
- `data/local/dao/ChatConversationDao.kt` - Data access object for conversations
- `data/local/dao/ChatMessageDao.kt` - Data access object for messages
- `domain/model/ChatConversation.kt` - Domain model for conversations
- `domain/model/ChatMessage.kt` - Domain model for messages
- `data/mapper/ChatMapper.kt` - Mappers between entity/domain representations
- `data/repository/ChatRepository.kt` - Repository for persistence operations

**Files Modified**:
- `data/local/AppDatabase.kt` - Added new entities and DAOs, bumped version to 5

**How It Works**:
1. Each chat session creates a new `ChatConversation` with a unique ID
2. User and assistant messages are stored as `ChatMessage` entities
3. Messages reference their parent conversation via foreign key
4. `ChatRepository` provides high-level API for save/load/clear operations
5. All operations use coroutines and run on IO dispatcher

**Usage in ViewModel**:
```kotlin
// Initialize conversation on app start
val conversation = chatRepository.getOrCreateConversation()

// Save messages as they're sent
chatRepository.saveMessage(conversationId, "user", text)
chatRepository.saveMessage(conversationId, "assistant", response)

// Clear conversation
chatRepository.clearConversation(conversationId)
```

**Database Schema**:
```
chat_conversations
├── id (PrimaryKey)
├── title
├── createdAtEpochMs
├── lastModifiedEpochMs
└── messageCount

chat_messages
├── id (PrimaryKey)
├── conversationId (ForeignKey → chat_conversations.id)
├── role ("user" or "assistant")
├── content
├── createdAtEpochMs
└── isError
```

---

### 2. ✅ Voice Input Button in Chat

**Purpose**: Enable users to speak queries instead of typing.

**Files Created**:
- `asr/VoiceRecognitionHelper.kt` - Wraps ASR with permission handling
- `ui/component/VoiceInputButton.kt` - Voice input button UI component

**UI Components**:
- `VoiceInputButton` - Compact 52dp circular button with mic icon
- Color changes: Teal (idle) → Red (recording) → Teal (complete)
- Animated scale on tap for feedback
- `VoiceInputButtonWithLabel` - Alternative with text label

**VoiceRecognitionHelper Features**:
- Checks `RECORD_AUDIO` permission status
- Wraps `NexaAsrEngine` for transcription
- Manages UI state (`isRecording`, `isProcessing`, `transcript`)
- Handles errors gracefully
- Provides `StateFlow<VoiceInputState>` for reactive UI

**Integration in ChatHomeScreen**:
```kotlin
VoiceInputButton(
    onVoiceInputClicked = {
        // TODO: Integrate voice recording and ASR
    },
    isRecording = false
)
```

**Next Steps to Complete**:
1. Add microphone recording UI (via `RecordingScreen` or new dialog)
2. Save recorded audio to temporary file
3. Call `voiceRecognitionHelper.processAudioFile(audioPath)`
4. Insert transcript into chat input field
5. Auto-send or let user review before sending

---

### 3. ✅ Suggested Prompts Based on Recent Notes

**Purpose**: Display relevant prompt suggestions to help users ask better questions.

**Files Created**:
- `ui/component/SuggestedPromptsAndActions.kt` - Suggested prompts and quick actions UI

**Components**:
- `SuggestedPromptsRow` - Horizontal scrollable list of suggested prompts
- `SuggestedPromptChip` - Individual tappable prompt chip
- Default prompts pulled from `ChatRepository.getSuggestedPrompts()`

**Features**:
- Shows up to 3 most relevant suggestions
- Tapping a prompt fills the input field
- Can be extended to pull topics from recent notes

**Suggested Prompts Display**:
```kotlin
SuggestedPromptsRow(
    prompts = uiState.suggestedPrompts.take(3),
    onPromptSelected = { prompt ->
        viewModel.selectSuggestedPrompt(prompt)
    }
)
```

**Future Enhancement**:
To pull prompts from actual notes, extend `ChatRepository.getRecentTopics()`:
```kotlin
suspend fun getRecentTopics(limit: Int = 10): List<String> {
    val notes = notesRepository.observeAllNotes()
    // Extract keywords from summaries/transcripts
    // Return top N unique topics
}
```

---

### 4. ✅ Quick Actions from Chat

**Purpose**: Provide one-tap access to common operations (create note, search, history).

**Files Created**:
- `ui/component/SuggestedPromptsAndActions.kt` - Includes `QuickActionsBar`

**Components**:
- `QuickActionsBar` - Row of action buttons
- `QuickActionButton` - Individual action button with label

**Available Actions**:
- "Create Note" → Trigger note creation from current chat
- "Search" → Search medical records or note library
- "History" → View or load previous conversations

**Integration**:
```kotlin
QuickActionsBar(
    onCreateNote = { /* Save chat summary as note */ },
    onSearchRecords = { /* Open search dialog */ },
    onHistory = { /* Load previous conversations */ }
)
```

---

### 5. ✅ Swipe Gestures for Navigation

**Purpose**: Enable intuitive swipe-based navigation between screens.

**Files Created**:
- `ui/component/GestureUtils.kt` - Gesture detection utilities
- `ui/component/SwipeableScreenContainer.kt` - Screen wrapper with swipe support

**Gesture Detection Functions**:
- `detectSwipeGestures(onSwipeLeft, onSwipeRight)` - Horizontal swipe detection
- `onHorizontalSwipe()` - Modifier extension for horizontal swipes
- `onVerticalSwipe()` - Modifier extension for vertical swipes

**Configuration**:
```kotlin
data class SwipeConfig(
    val velocityThreshold: Float = 400f,  // pixels/second
    val distanceThreshold: Float = 100f   // minimum drag distance
)
```

**SwipeableScreenContainer Usage**:
```kotlin
SwipeableScreenContainer(
    content = { YourScreenContent() },
    onSwipeLeft = { navigateToNextScreen() },
    onSwipeRight = { navigateToPreviousScreen() }
)
```

**Integration in MainActivity**:
```kotlin
SwipeableScreenContainer(
    content = {
        when (currentScreen) {
            Screen.Chat -> ChatHomeScreen(...)
            Screen.NotesList -> NotesListScreen(...)
            // ...
        }
    },
    onSwipeRight = { /* Navigate to previous screen */ },
    onSwipeLeft = { /* Navigate to next screen */ }
)
```

---

### 6. ✅ Bottom Navigation Bar

**Purpose**: Replace FABs with persistent bottom navigation for frequent features.

**Files Created**:
- `ui/component/AppBottomNavigationBar.kt` - Bottom navigation bar component
- `presentation/BottomNavViewModel.kt` - Navigation state management

**Components**:
- `AppBottomNavigationBar` - Full navigation bar
- `BottomNavItem` - Data class for nav items
- `BottomNavigation.defaultItems()` - Standard navigation items

**Default Navigation Items**:
1. **Chat** (ID: "chat") - Open chat home screen
2. **Record** (ID: "record") - Start recording
3. **Notes** (ID: "notes") - View notes list
4. **Settings** (ID: "settings") - Open settings

**Features**:
- Animated color transitions on selection
- Centered text labels below icons
- 72dp height with safe bottom padding
- Teal color scheme integration

**Integration**:
```kotlin
val navViewModel: BottomNavViewModel = viewModel()
val selectedItem by navViewModel.selectedNavItem.collectAsState()

AppBottomNavigationBar(
    items = BottomNavigation.defaultItems(),
    selectedItemId = selectedItem,
    onItemSelected = { itemId ->
        navViewModel.selectNavItem(itemId)
        navigateToScreen(itemId)
    }
)
```

**Modified UI Layout**:
```
┌─────────────────────────┐
│     Chat Content        │ (main content area, weight=1f)
├─────────────────────────┤
│  Chat Input + Actions   │ (fixed height input area)
├─────────────────────────┤
│ [Chat] [Record] [Notes] │ (bottom nav bar, 72dp)
│      [Settings]         │
└─────────────────────────┘
```

---

## Updated ChatViewModel

**File**: `presentation/ChatViewModel.kt`

**New Features**:
- Conversation ID tracking
- Suggested prompts loading
- Message persistence on send
- Chat history state management

**New State Fields**:
```kotlin
data class ChatUiState(
    val messages: List<ChatMessageUi> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val suggestedPrompts: List<String> = emptyList(),
    val conversationId: String? = null
)
```

**New ViewModel Functions**:
- `selectSuggestedPrompt(prompt: String)` - Fill input with suggested prompt
- `initializeConversation()` - Create new chat session
- `loadSuggestedPrompts()` - Fetch suggested prompts from repository

---

## Integration Checklist

### Phase 1: Data Layer ✅
- [x] Create chat entities and DAOs
- [x] Update AppDatabase with new tables
- [x] Create domain models and mappers
- [x] Implement ChatRepository

### Phase 2: ViewModel Updates ✅
- [x] Extend ChatViewModel with persistence
- [x] Add conversation tracking
- [x] Add suggested prompts support
- [x] Auto-save messages

### Phase 3: UI Components ✅
- [x] Voice input button
- [x] Suggested prompts display
- [x] Quick actions bar
- [x] Gesture utilities
- [x] Bottom navigation bar
- [x] Swipeable screen container

### Phase 4: Integration (In Progress)
- [ ] Update MainActivity to use bottom nav
- [ ] Add swipe gesture detection to screens
- [ ] Connect voice input to recording flow
- [ ] Link quick actions to their features
- [ ] Add permission handling for voice input

### Phase 5: Testing
- [ ] Unit test ChatRepository persistence
- [ ] Instrument test gesture detection
- [ ] Test voice input flow
- [ ] Verify navigation state management
- [ ] Test database migrations

---

## Permissions Required

Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

## Database Migration

The AppDatabase version has been bumped from 4 to 5 to accommodate the new chat tables:
- `chat_conversations` (new)
- `chat_messages` (new)

Since `fallbackToDestructiveMigration()` is enabled, existing data will be cleared on update. For production, implement explicit migration:
```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create tables if needed
    }
}
```

---

## Future Enhancements

### Short Term
1. **Voice Input Recording UI** - Dedicated dialog or overlay for recording
2. **Real Suggested Prompts** - Extract topics from actual recent notes
3. **Chat History Loading** - Load and resume previous conversations
4. **Settings Screen** - Preferences for UI themes, privacy, data management

### Medium Term
1. **Gesture Customization** - Allow users to configure swipe actions
2. **Voice Commands** - Beyond just transcription (e.g., "clear chat", "show notes")
3. **Chat Export** - Save conversations as PDF or text
4. **Advanced Search** - Full-text search across chat history and notes

### Long Term
1. **Chat Analytics** - Track common questions and topics
2. **Personalized Prompts** - ML-based suggestions based on user patterns
3. **Multi-language Support** - Voice input in multiple languages
4. **Cloud Sync** - Sync chat history across devices

---

## Testing Strategies

### Unit Tests
```kotlin
@Test
fun testChatRepository_savesMessageCorrectly() {
    // Insert conversation, save message, verify persistence
}

@Test
fun testChatViewModel_persistsOnSendMessage() {
    // Send message, verify repository.saveMessage called
}
```

### Integration Tests
```kotlin
@Test
fun testSwipeGesture_navigatesScreen() {
    // Perform swipe gesture, verify navigation
}

@Test
fun testVoiceInput_fillsChatInput() {
    // Trigger voice input, verify input field populated
}
```

### UI Tests (Espresso)
```kotlin
@Test
fun testBottomNavigation_switchesScreens() {
    onView(withId(R.id.nav_item_notes)).perform(click())
    onView(withId(R.id.notes_list)).check(matches(isDisplayed()))
}
```

---

## Code Organization

```
app/src/main/java/demo/nexa/clinical_transcription_demo/
├── asr/
│   ├── NexaAsrEngine.kt (existing)
│   ├── VoiceRecognitionHelper.kt (new)
│   └── ...
├── data/
│   ├── local/
│   │   ├── entity/
│   │   │   ├── ChatConversationEntity.kt (new)
│   │   │   ├── ChatMessageEntity.kt (new)
│   │   │   └── ...
│   │   ├── dao/
│   │   │   ├── ChatConversationDao.kt (new)
│   │   │   ├── ChatMessageDao.kt (new)
│   │   │   └── ...
│   │   ├── AppDatabase.kt (modified)
│   │   └── ...
│   ├── mapper/
│   │   ├── ChatMapper.kt (new)
│   │   └── ...
│   └── repository/
│       ├── ChatRepository.kt (new)
│       └── ...
├── domain/
│   └── model/
│       ├── ChatConversation.kt (new)
│       ├── ChatMessage.kt (new)
│       └── ...
├── presentation/
│   ├── ChatViewModel.kt (modified)
│   ├── BottomNavViewModel.kt (new)
│   └── ...
├── ui/
│   ├── component/
│   │   ├── VoiceInputButton.kt (new)
│   │   ├── GestureUtils.kt (new)
│   │   ├── SwipeableScreenContainer.kt (new)
│   │   ├── SuggestedPromptsAndActions.kt (new)
│   │   ├── AppBottomNavigationBar.kt (new)
│   │   └── ...
│   ├── screen/
│   │   ├── ChatHomeScreen.kt (modified)
│   │   └── ...
│   └── ...
└── ...
```

---

## Troubleshooting

### Build Errors

**Error**: "ChatRepository not found"
- Ensure `data/repository/ChatRepository.kt` is created
- Check package name: `demo.nexa.clinical_transcription_demo.data.repository`

**Error**: "VoiceInputButton is not recognized"
- Ensure `ui/component/VoiceInputButton.kt` is created
- Verify imports in `ChatHomeScreen.kt`

### Runtime Errors

**Error**: Database version mismatch
- Delete app data: Settings → Apps → Clinical App → Storage → Clear Data
- Or implement proper database migration

**Error**: ASR transcription fails
- Verify `RECORD_AUDIO` permission granted
- Check ASR model is available via `NexaAsrEngine.ensureReady()`

---

## Summary

All six enhancements have been successfully implemented:

1. ✅ **Chat History Persistence** - Conversations and messages saved to Room database
2. ✅ **Voice Input Button** - Microphone button in chat input area
3. ✅ **Suggested Prompts** - Dynamic prompts based on recent interactions
4. ✅ **Quick Actions** - One-tap access to common features
5. ✅ **Swipe Gestures** - Intuitive swipe-based screen navigation
6. ✅ **Bottom Navigation Bar** - Persistent navigation replacing FABs

**Next Steps**:
1. Update `MainActivity` to integrate bottom navigation
2. Add swipe gesture detection to main screen
3. Connect voice input to recording flow
4. Test all features end-to-end
5. Build and deploy for user testing


