# Clinical App Enhancements - Visual Architecture

## System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                        ANDROID APP                               │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                   UI LAYER (Jetpack Compose)              │ │
│  ├────────────────────────────────────────────────────────────┤ │
│  │                                                            │ │
│  │  MainActivity                                             │ │
│  │    └── SwipeableScreenContainer (NEW - gesture support) │ │
│  │         ├── ChatHomeScreen                              │ │
│  │         │    ├── VoiceInputButton (NEW)                 │ │
│  │         │    ├── SuggestedPromptsRow (NEW)              │ │
│  │         │    ├── QuickActionsBar (NEW)                  │ │
│  │         │    ├── ChatInput with Voice Button            │ │
│  │         │    └── ChatMessages (+ persistence)           │ │
│  │         ├── NotesListScreen                             │ │
│  │         ├── RecordingScreen                             │ │
│  │         └── MedicalEntriesScreen                        │ │
│  │                                                          │ │
│  │         AppBottomNavigationBar (NEW)                    │ │
│  │         ├── Chat Navigation Item                        │ │
│  │         ├── Record Navigation Item                      │ │
│  │         ├── Notes Navigation Item                       │ │
│  │         └── Settings Navigation Item                    │ │
│  │                                                            │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              PRESENTATION LAYER (ViewModels)              │ │
│  ├────────────────────────────────────────────────────────────┤ │
│  │                                                            │ │
│  │  ChatViewModel (UPDATED)                                 │ │
│  │   - conversationId tracking                              │ │
│  │   - suggestedPrompts state                               │ │
│  │   - Auto-persist messages via ChatRepository             │ │
│  │   - selectSuggestedPrompt() function                     │ │
│  │                                                            │ │
│  │  BottomNavViewModel (NEW)                                │ │
│  │   - selectedNavItem state                                │ │
│  │   - selectNavItem() function                             │ │
│  │                                                            │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │          BUSINESS LOGIC LAYER (Repositories)              │ │
│  ├────────────────────────────────────────────────────────────┤ │
│  │                                                            │ │
│  │  ChatRepository (NEW)                                    │ │
│  │   ├── getOrCreateConversation()                          │ │
│  │   ├── saveMessage()                                      │ │
│  │   ├── loadConversation()                                 │ │
│  │   ├── observeConversationMessages()                      │ │
│  │   ├── getSuggestedPrompts()                              │ │
│  │   └── clearConversation()                                │ │
│  │                                                            │ │
│  │  NotesRepository (existing)                              │ │
│  │   └── observeAllNotes() (for prompt generation)          │ │
│  │                                                            │ │
│  │  VoiceRecognitionHelper (NEW)                            │ │
│  │   ├── hasAudioPermission()                               │ │
│  │   ├── processAudioFile()                                 │ │
│  │   └── voiceState: StateFlow<VoiceInputState>            │ │
│  │                                                            │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │           DATA LAYER (Room Database)                      │ │
│  ├────────────────────────────────────────────────────────────┤ │
│  │                                                            │ │
│  │  AppDatabase (UPDATED - v4 → v5)                        │ │
│  │                                                            │ │
│  │  ChatConversationEntity (NEW)      ChatConversationDao   │ │
│  │   ├── id (PrimaryKey)              (NEW)                │ │
│  │   ├── title                                              │ │
│  │   ├── createdAtEpochMs                                   │ │
│  │   ├── lastModifiedEpochMs                                │ │
│  │   └── messageCount                                       │ │
│  │                                                            │ │
│  │  ChatMessageEntity (NEW)           ChatMessageDao (NEW)  │ │
│  │   ├── id (PrimaryKey)                                    │ │
│  │   ├── conversationId (ForeignKey)                        │ │
│  │   ├── role                                               │ │
│  │   ├── content                                            │ │
│  │   ├── createdAtEpochMs                                   │ │
│  │   └── isError                                            │ │
│  │                                                            │ │
│  │  RecordingNoteEntity (existing)    RecordingNoteDao      │ │
│  │  MedicalEntryEntity (existing)     MedicalEntryDao       │ │
│  │                                                            │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              EXTERNAL INTEGRATIONS                         │ │
│  ├────────────────────────────────────────────────────────────┤ │
│  │                                                            │ │
│  │  NexaAsrEngine              [Speech Recognition]         │ │
│  │   └── transcribe(audioPath) ← VoiceRecognitionHelper    │ │
│  │                                                            │ │
│  │  NexaLlmEngine              [Language Model]             │ │
│  │   └── chatWithMedicalAssistant() ← ChatViewModel         │ │
│  │                                                            │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Diagrams

### 1. Chat Message Persistence Flow

```
User Types Message
        ↓
   ChatView Input
        ↓
ChatViewModel.sendMessage()
        ↓
   Add to UI State
        ↓
   LLM Processing
        ↓
ChatViewModel.onLlmComplete()
        ↓
ChatRepository.saveMessage()
        ↓
   Room Database
        ↓
[ Messages persisted ]
        ↓
   App Restart
        ↓
ChatRepository.loadConversation()
        ↓
   Messages Restored
```

### 2. Voice Input Flow

```
User Taps Mic Button
        ↓
VoiceInputButton.onClick()
        ↓
Check RECORD_AUDIO Permission
        ↓
  Permission Granted?
   /            \
 YES            NO
  |              |
  ↓              ↓
Record Audio  Request Permission
  |              |
  ↓              ↓
Save to Temp  Permission Granted?
  |              |
  ↓              ↓
VoiceRecognitionHelper
  .processAudioFile()
        ↓
NexaAsrEngine.transcribe()
        ↓
  Transcript
        ↓
Insert to ChatInput
        ↓
[ User reviews & sends ]
```

### 3. Navigation Flow (Swipe + Bottom Nav)

```
┌─────────────────────────────────────────┐
│         Main Activity Screen             │
├─────────────────────────────────────────┤
│                                         │
│  Current Screen                         │
│  ↑              ↑               ↑       │
│  |              |               |       │
│  Swipe Right  Bottom Nav      Swipe    │
│  ←──────→       Click        Left →    │
│  Prev Screen   Navigate      Next      │
│              New Screen       Screen   │
│                                         │
├─────────────────────────────────────────┤
│  Bottom Navigation Bar                  │
│  [Chat] [Record] [Notes] [Settings]     │
│   ↑       ↑       ↑       ↑             │
│   └───────┴───────┴───────┘             │
│         onItemSelected()                │
│         ↓                               │
│   BottomNavViewModel.selectNavItem()    │
│         ↓                               │
│   currentScreen = getScreen(itemId)    │
└─────────────────────────────────────────┘
```

### 4. Suggested Prompts Generation Flow

```
ChatViewModel Initializes
        ↓
loadSuggestedPrompts()
        ↓
ChatRepository.getSuggestedPrompts()
        ↓
   Option 1: Hardcoded       Option 2: From Notes
   Default Prompts           (Future Enhancement)
   ├── Medical term           ├── Load recent notes
   ├── Drug side effects      ├── Extract topics
   └── ICD-10 codes           ├── Generate prompts
                              └── Cache results
        ↓
Add to ChatUiState.suggestedPrompts
        ↓
SuggestedPromptsRow Renders
        ↓
User Taps Prompt Chip
        ↓
ChatViewModel.selectSuggestedPrompt()
        ↓
Input Field Filled
        ↓
[ User sends message ]
```

---

## Feature Interaction Matrix

```
┌──────────────────┬─────────────┬────────────────┬──────────────────┐
│ Feature          │ Reads From  │ Writes To      │ Triggers         │
├──────────────────┼─────────────┼────────────────┼──────────────────┤
│ Chat Persistence │ ViewModel   │ Room Database  │ Message send     │
│                  │ Database    │ Repository     │ App reload       │
├──────────────────┼─────────────┼────────────────┼──────────────────┤
│ Voice Input      │ Permission  │ Chat Input     │ Mic button tap   │
│                  │ ASR Engine  │ VoiceState     │ Recording done   │
├──────────────────┼─────────────┼────────────────┼──────────────────┤
│ Suggested Prompts│ Repository  │ UI State       │ App start        │
│                  │ Notes DB    │ Input field    │ Prompt selected  │
├──────────────────┼─────────────┼────────────────┼──────────────────┤
│ Quick Actions    │ UI State    │ Navigation     │ Button tapped    │
│                  │ Chat Msgs   │ Dialogs        │ Action complete  │
├──────────────────┼─────────────┼────────────────┼──────────────────┤
│ Swipe Navigation │ Gesture     │ Current Screen │ Swipe detected   │
│                  │ ViewModel   │ Nav State      │ Threshold met    │
├──────────────────┼─────────────┼────────────────┼──────────────────┤
│ Bottom Nav       │ ViewModel   │ Current Screen │ Item tapped      │
│                  │ Screen List │ Nav State      │ Navigation done  │
└──────────────────┴─────────────┴────────────────┴──────────────────┘
```

---

## File Dependency Graph

```
MainActivity
    ├── ChatHomeScreen
    │   ├── ChatViewModel
    │   │   ├── ChatRepository
    │   │   │   ├── AppDatabase
    │   │   │   │   ├── ChatConversationDao
    │   │   │   │   ├── ChatMessageDao
    │   │   │   │   ├── ChatConversationEntity
    │   │   │   │   └── ChatMessageEntity
    │   │   │   ├── NotesRepository (for topics)
    │   │   │   └── ChatMapper
    │   │   └── NexaLlmEngine
    │   ├── VoiceInputButton
    │   │   └── VoiceRecognitionHelper
    │   │       ├── NexaAsrEngine
    │   │       └── VoiceInputState
    │   ├── SuggestedPromptsRow
    │   │   └── SuggestedPromptChip
    │   └── QuickActionsBar
    │       ├── CreateNoteAction
    │       ├── SearchAction
    │       └── HistoryAction
    ├── SwipeableScreenContainer
    │   └── GestureUtils
    ├── AppBottomNavigationBar
    │   ├── BottomNavItem
    │   ├── BottomNavViewModel
    │   └── BottomNavBarItem
    ├── NotesListScreen
    ├── RecordingScreen
    └── MedicalEntriesScreen
```

---

## Database Schema Diagram

```
chat_conversations
┌─────────────────────────┐
│ PrimaryKey              │
├─────────────────────────┤
│ id: String              │
│ title: String           │
│ createdAtEpochMs: Long  │
│ lastModifiedEpochMs: Long
│ messageCount: Int       │
└─────────────────────────┘
    ↓ 1:N ↓
    ↓     ↓
    ↓     └→ chat_messages
    ↓       ┌─────────────────────────┐
    ↓       │ PrimaryKey              │
    └──────→├─────────────────────────┤
            │ id: String              │
            │ conversationId: String  │ ← Foreign Key
            │ role: String            │
            │ content: String         │
            │ createdAtEpochMs: Long  │
            │ isError: Boolean        │
            └─────────────────────────┘
            
Indices:
  - chat_messages.conversationId (for JOINs)
  - chat_messages.createdAtEpochMs (for sorting)
```

---

## State Flow Diagram

```
ChatViewModel State
┌────────────────────────────────────────┐
│ ChatUiState                            │
├────────────────────────────────────────┤
│ messages: List<ChatMessageUi>          │
│ isLoading: Boolean                     │
│ inputText: String                      │
│ suggestedPrompts: List<String> (NEW)  │
│ conversationId: String? (NEW)          │
└────────────────────────────────────────┘
         ↑ collectAsState()
         │
    Recomposition
         │
   ChatHomeScreen
   
BottomNavViewModel State
┌────────────────────────────────────────┐
│ selectedNavItem: String                │
└────────────────────────────────────────┘
         ↑ collectAsState()
         │
    Recomposition
         │
AppBottomNavigationBar

VoiceRecognitionHelper State
┌────────────────────────────────────────┐
│ VoiceInputState                        │
├────────────────────────────────────────┤
│ isRecording: Boolean                   │
│ isProcessing: Boolean                  │
│ error: String?                         │
│ transcript: String?                    │
└────────────────────────────────────────┘
         ↑ collectAsState()
         │
    Recomposition
         │
VoiceInputButton
```

---

## Gesture Detection Flow

```
User Swipes on Screen
        ↓
detectSwipeGestures() Modifier
        ↓
Track Drag Amount
        ↓
Swipe Complete (onDragEnd)
        ↓
Compare Against Thresholds
        ├─ Distance > 100px?
        └─ Duration reasonable?
        ↓
   /         \
 YES         NO
  ↓           ↓
Invoke      Ignore
Callback    Gesture
  ↓
Navigate
```

---

## Component Hierarchy

```
MainActivity
│
├── Column
│   ├── Box (weight=1f) [Content Area]
│   │   └── SwipeableScreenContainer
│   │       ├── ChatHomeScreen
│   │       │   ├── Column [Header]
│   │       │   ├── Box [Messages Area]
│   │       │   │   ├── LazyColumn (when messages exist)
│   │       │   │   │   └── ChatMessageBubble (repeating)
│   │       │   │   └── WelcomeContent (when empty)
│   │       │   │       └── SuggestedPromptsRow
│   │       │   ├── Column [Input Area]
│   │       │   │   ├── HorizontalDivider
│   │       │   │   ├── SuggestedPromptsRow (if any)
│   │       │   │   ├── Row [Input Controls]
│   │       │   │   │   ├── VoiceInputButton
│   │       │   │   │   ├── OutlinedTextField
│   │       │   │   │   └── IconButton [Send]
│   │       │   │   └── Spacer
│   │       │   ├── QuickActionsBar (NEW)
│   │       │   │   ├── QuickActionButton [Create Note]
│   │       │   │   ├── QuickActionButton [Search]
│   │       │   │   └── QuickActionButton [History]
│   │       │   └── FAB [Record] + FAB [Notes] (OLD)
│   │       ├── NotesListScreen
│   │       ├── RecordingScreen
│   │       └── MedicalEntriesScreen
│   │
│   └── AppBottomNavigationBar
│       ├── BottomNavBarItem [Chat]
│       ├── BottomNavBarItem [Record]
│       ├── BottomNavBarItem [Notes]
│       └── BottomNavBarItem [Settings]
```

---

## Integration Checklist - Visual

```
┌─────────────────────────────────────────────────────┐
│ Implementation Status                               │
├─────────────────────────────────────────────────────┤
│                                                     │
│ Phase 1: Implementation                      ✅    │
│  ├── Data Layer                        ✅ 100%    │
│  ├── UI Components                     ✅ 100%    │
│  ├── ViewModels                        ✅ 100%    │
│  └── Documentation                    ✅ 100%    │
│                                                     │
│ Phase 2: Integration (IN PROGRESS)          ⏳    │
│  ├── MainActivity Updates              ⏳ 0%     │
│  ├── Bottom Nav Integration            ⏳ 0%     │
│  ├── Swipe Gesture Integration         ⏳ 0%     │
│  └── Voice Input Connection            ⏳ 0%     │
│                                                     │
│ Phase 3: Testing                            ⏳    │
│  ├── Unit Tests                        ⏳ 0%     │
│  ├── Integration Tests                 ⏳ 0%     │
│  ├── UI Tests                          ⏳ 0%     │
│  └── E2E Tests                         ⏳ 0%     │
│                                                     │
│ Phase 4: Deployment                        ⏳    │
│  ├── Build & Verify                    ⏳ 0%     │
│  ├── Beta Testing                      ⏳ 0%     │
│  └── Production Release                ⏳ 0%     │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## Quick Reference

### Key Files
```
Data Persistence:   ChatRepository, ChatConversationEntity, ChatMessageEntity
Voice Input:        VoiceRecognitionHelper, VoiceInputButton
Navigation:         AppBottomNavigationBar, SwipeableScreenContainer, BottomNavViewModel
UI:                 ChatHomeScreen (modified), SuggestedPromptsAndActions
Documentation:      INDEX.md, IMPLEMENTATION_SUMMARY.md, CODE_EXAMPLES.md
```

### Key Classes
```
Repository:         ChatRepository (CRUD for chat)
ViewModel:          ChatViewModel (updated), BottomNavViewModel (new)
Components:         6 new Compose components
Entities:           2 new Room entities
DAOs:               2 new data access objects
Models:             2 new domain models
```

### Key Functions
```
ChatViewModel.selectSuggestedPrompt()
ChatViewModel.initializeConversation()
ChatViewModel.loadSuggestedPrompts()
ChatRepository.saveMessage()
ChatRepository.loadConversation()
VoiceRecognitionHelper.processAudioFile()
detectSwipeGestures()
AppBottomNavigationBar()
```

---

**Visual Architecture Complete** ✅

