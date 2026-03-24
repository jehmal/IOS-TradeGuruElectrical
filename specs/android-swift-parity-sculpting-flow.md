# Plan: Android-Swift Complete Parity — The Sculpting Flow

## Task Description
Build the complete TradeGuru Android app (Kotlin/Jetpack Compose) to achieve 100% feature and visual parity with the existing Swift/SwiftUI iOS app. We start from the existing boilerplate (models, Room DB, theme, build system — 27 Kotlin files) and systematically build every layer until the Android app is functionally identical to the 55-file Swift app.

The approach is sculptural: start with the rough block and carve layer by layer — foundation, skeleton, flesh, nervous system, skin — until the final form emerges.

## Objective
When complete, the Android app will have:
- Every screen the iOS app has (Chat, Onboarding, Disclaimer, Settings, Sidebar)
- Every component (MessageBubble, all 12 block types, ModeSelector, InputBar, PipelineStatus)
- Every service (SSE streaming API, auth, device management, stream parsing)
- Every ViewModel (ChatViewModel, ChatEngine, ConversationManager)
- Navigation matching iOS flow (Onboarding → Disclaimer → Chat)
- Visual parity verified against iOS HTML preview
- 45 new Kotlin files porting all remaining Swift functionality

## Problem Statement
We have the Android foundation (models, DB, theme) but zero UI, zero services, zero ViewModels. The iOS app has 55 Swift files across Views, ViewModels, Services, and Models. We need to create ~45 new Kotlin files porting all remaining Swift functionality while maintaining exact visual and behavioral parity. The Code Reader agent has already gathered 98 patterns across 14 expertise files from real Android PRs and our codebase to inform this build.

## Solution Approach
Multi-phase autoresearch pipeline using specialized agents. Each phase builds on the previous, with validation gates between phases. Agents read the Swift source file, read the relevant expertise from `autoresearch/library/`, and produce the Kotlin equivalent. A structure coder ensures architectural integrity. A final parity audit validates the result.

## Relevant Files

### Swift Source (read-only reference — the spec)
- `ios/Tradeguruelectrical/Services/TradeGuruAPI.swift` — SSE streaming API client
- `ios/Tradeguruelectrical/Services/StreamParser.swift` — SSE line parser
- `ios/Tradeguruelectrical/Services/AuthManager.swift` — OAuth/JWT auth
- `ios/Tradeguruelectrical/Services/DeviceManager.swift` — Device ID management
- `ios/Tradeguruelectrical/Services/APIConfig.swift` — Base URL/headers
- `ios/Tradeguruelectrical/Services/KeychainHelper.swift` — Secure storage
- `ios/Tradeguruelectrical/Services/PKCEHelper.swift` — PKCE challenge generation
- `ios/Tradeguruelectrical/Services/JWTDecoder.swift` — JWT claim extraction
- `ios/Tradeguruelectrical/ViewModels/ChatViewModel.swift` — Thin orchestrator
- `ios/Tradeguruelectrical/ViewModels/ChatEngine.swift` — Streaming engine
- `ios/Tradeguruelectrical/ViewModels/ConversationManager.swift` — Persistence CRUD
- `ios/Tradeguruelectrical/ContentView.swift` — Root router
- `ios/Tradeguruelectrical/ChatView.swift` — Main chat screen
- `ios/Tradeguruelectrical/ChatInputBar.swift` — Input with attachments
- `ios/Tradeguruelectrical/ModeSelector.swift` — Mode pill selector
- `ios/Tradeguruelectrical/ModeInfoCard.swift` — Mode description card
- `ios/Tradeguruelectrical/Views/MessageBubble.swift` — Message rendering
- `ios/Tradeguruelectrical/Views/SidebarView.swift` — Conversation drawer
- `ios/Tradeguruelectrical/Views/CameraView.swift` — Camera capture
- `ios/Tradeguruelectrical/Views/PipelineStatusView.swift` — Status indicator
- `ios/Tradeguruelectrical/Views/PipelineStatusDots.swift` — Animated dots
- `ios/Tradeguruelectrical/Views/Chat/ChatMessageList.swift` — Scrollable list
- `ios/Tradeguruelectrical/Views/Chat/ChatNavBar.swift` — Top nav bar
- `ios/Tradeguruelectrical/Views/Chat/ChatErrorBanner.swift` — Error display
- `ios/Tradeguruelectrical/Views/Blocks/TextBlockView.swift` — Text block
- `ios/Tradeguruelectrical/Views/Blocks/CodeBlockView.swift` — Code block
- `ios/Tradeguruelectrical/Views/Blocks/StepListView.swift` — Step list
- `ios/Tradeguruelectrical/Views/Blocks/PartsListView.swift` — Parts list
- `ios/Tradeguruelectrical/Views/Blocks/TableBlockView.swift` — Table
- `ios/Tradeguruelectrical/Views/Blocks/WarningCardView.swift` — Warning card
- `ios/Tradeguruelectrical/Views/Blocks/CalloutView.swift` — Callout
- `ios/Tradeguruelectrical/Views/Blocks/RegulationView.swift` — Regulation
- `ios/Tradeguruelectrical/Views/Onboarding/OnboardingView.swift` — Carousel
- `ios/Tradeguruelectrical/Views/Onboarding/OnboardingPageView.swift` — Single page
- `ios/Tradeguruelectrical/Views/Onboarding/OnboardingFinalPageView.swift` — Final page
- `ios/Tradeguruelectrical/Views/Settings/SettingsView.swift` — Settings
- `ios/Tradeguruelectrical/Views/Settings/SignInView.swift` — Sign in
- `ios/Tradeguruelectrical/Views/Settings/TierBadgeView.swift` — Tier badge
- `ios/Tradeguruelectrical/Views/Legal/SafetyDisclaimerView.swift` — Disclaimer

### Existing Android (already built — foundation)
- `android/app/src/main/java/com/tradeguru/electrical/models/` — 10 files (enums, data classes, MockData)
- `android/app/src/main/java/com/tradeguru/electrical/data/db/` — 12 files (Room entities, DAOs, converters, database)
- `android/app/src/main/java/com/tradeguru/electrical/ui/theme/` — 3 files (Color, Theme, Type)
- `android/app/src/main/java/com/tradeguru/electrical/MainActivity.kt` — Entry point
- `android/app/src/main/java/com/tradeguru/electrical/TradeGuruApp.kt` — Application class

### New Files to Create

**Infrastructure (4 files)**
- `android/.../navigation/NavGraph.kt` — Route definitions + conditional root routing (onboarding/disclaimer/chat)
- `android/.../di/AppModule.kt` — Manual DI: provides Database, DAOs, Services, ViewModelFactory
- `android/.../data/PreferencesManager.kt` — DataStore Preferences wrapper (replaces @AppStorage)
- `android/.../data/DomainMappers.kt` — Room entity ↔ domain model mapping extensions

**Services (9 files)**
- `android/.../services/APIConfig.kt`
- `android/.../services/TradeGuruAPI.kt` — Uses raw OkHttp ResponseBody.source() for SSE (NOT okhttp-sse EventSource)
- `android/.../services/StreamParser.kt` — Parses raw SSE lines into Flow<StreamResult> sealed class
- `android/.../services/ApiModels.kt` — API request/response models (DeviceRegisterResponse, ApiMessage, payload types, etc.)
- `android/.../services/AuthManager.kt` — Chrome Custom Tabs OAuth PKCE flow
- `android/.../services/DeviceManager.kt`
- `android/.../services/KeychainHelper.kt`
- `android/.../services/PKCEHelper.kt`
- `android/.../services/JWTDecoder.kt`

**ViewModels (3 files)**
- `android/.../viewmodels/ChatViewModel.kt` — Thin orchestrator, receives deps via factory
- `android/.../viewmodels/ChatEngine.kt` — Consumes Flow<StreamResult> from API, emits StateFlow to UI
- `android/.../viewmodels/ConversationManager.kt` — Wraps DAOs, maps entities to domain models

**UI — Chat Screen (10 files)**
- `android/.../ui/ChatScreen.kt`
- `android/.../ui/ChatInputBar.kt`
- `android/.../ui/ModeSelector.kt`
- `android/.../ui/ModeInfoCard.kt`
- `android/.../ui/views/MessageBubble.kt`
- `android/.../ui/views/PipelineStatusView.kt`
- `android/.../ui/views/PipelineStatusDots.kt`
- `android/.../ui/views/chat/ChatMessageList.kt`
- `android/.../ui/views/chat/ChatNavBar.kt`
- `android/.../ui/views/chat/ChatErrorBanner.kt`

**UI — Block Views (12 files — one per ContentBlockType + renderer)**
- `android/.../ui/views/blocks/BlockRenderer.kt` — Switches on ContentBlockType, renders correct view
- `android/.../ui/views/blocks/TextBlockView.kt`
- `android/.../ui/views/blocks/HeadingBlockView.kt` — (was missing: h1/h2/h3 rendering)
- `android/.../ui/views/blocks/CodeBlockView.kt`
- `android/.../ui/views/blocks/StepListView.kt`
- `android/.../ui/views/blocks/PartsListView.kt`
- `android/.../ui/views/blocks/TableBlockView.kt`
- `android/.../ui/views/blocks/WarningCardView.kt`
- `android/.../ui/views/blocks/CalloutView.kt`
- `android/.../ui/views/blocks/RegulationView.kt`
- `android/.../ui/views/blocks/LinkBlockView.kt` — (was missing: clickable URL)
- `android/.../ui/views/blocks/DiagramRefBlockView.kt` — (was missing: italic reference)

**UI — Secondary Screens (9 files)**
- `android/.../ui/views/SidebarView.kt`
- `android/.../ui/views/CameraView.kt`
- `android/.../ui/views/onboarding/OnboardingScreen.kt`
- `android/.../ui/views/onboarding/OnboardingPageView.kt`
- `android/.../ui/views/onboarding/OnboardingFinalPageView.kt`
- `android/.../ui/views/settings/SettingsScreen.kt`
- `android/.../ui/views/settings/SignInView.kt`
- `android/.../ui/views/settings/TierBadgeView.kt`
- `android/.../ui/views/legal/SafetyDisclaimerScreen.kt`

### Expertise Files (from Code Reader — 98 patterns)
- `autoresearch/library/architecture.md` — ViewModel delegation, retry, entity mapping
- `autoresearch/library/compose-ui.md` — Screen composition, block rendering, bubbles
- `autoresearch/library/networking.md` — SSE streaming, API construction, file upload
- `autoresearch/library/navigation.md` — NavKey, route patterns, deep links
- `autoresearch/library/state-management.md` — UiState sealed classes, StateFlow
- `autoresearch/library/coroutines.md` — launchOrThrow, retained scope
- `autoresearch/library/screens.md` — Screen scaffolding patterns
- `autoresearch/library/viewmodel.md` — ViewModel patterns
- `autoresearch/library/theming.md` — Compose theming
- `autoresearch/library/testing.md` — Test patterns
- `autoresearch/library/sse.md` — SSE-specific patterns
- `autoresearch/library/chat-patterns.md` — Chat UI patterns
- `autoresearch/library/security.md` — Secure storage patterns
- `autoresearch/library/migration.md` — Migration patterns

## Critical Architecture Decisions (Review Round 1 Fixes)

### AD1: SSE Streaming — Use Raw OkHttp, NOT okhttp-sse EventSource

The iOS app uses `URLSession.bytes(for:)` to get a raw byte stream, then parses `event:` / `data:` lines manually via `StreamParser`. The `okhttp-sse` EventSource library does its own SSE parsing via callbacks, which is INCOMPATIBLE with our `StreamParser` architecture.

**Decision:** Use raw OkHttp `Response.body?.source()?.readUtf8Line()` inside a coroutine, pipe lines through `StreamParser`, emit `Flow<StreamResult>`. Do NOT use `EventSource`/`EventSourceListener`.

```kotlin
// TradeGuruAPI.kt — ALL 8 endpoints matching iOS exactly
object TradeGuruAPI {
    private val client = OkHttpClient()
    private val gson = Gson()

    // 1. Device registration (computes platform/locale/timezone internally like iOS)
    suspend fun registerDevice(): DeviceRegisterResponse

    // 2. Chat (SSE streaming) — returns Flow consumed on Dispatchers.IO
    fun chat(messages: List<ApiMessage>, mode: ThinkingMode, deviceId: String, jwt: String? = null): Flow<StreamResult> = flow {
        val call = client.newCall(request)
        var response: Response? = null
        try {
            response = withContext(Dispatchers.IO) { call.execute() }
            val source = response.body?.source() ?: throw IOException("Empty body")
            var currentEvent = ""; var currentData = ""
            while (!source.exhausted()) {
                ensureActive() // cooperative cancellation
                val line = withContext(Dispatchers.IO) { source.readUtf8Line() } ?: break
                if (line.startsWith("event: ")) currentEvent = line.removePrefix("event: ")
                else if (line.startsWith("data: ")) currentData = line.removePrefix("data: ")
                else if (line.isEmpty() && currentEvent.isNotEmpty()) {
                    emit(StreamParser.parse(currentEvent, currentData))
                    currentEvent = ""; currentData = ""
                }
            }
        } finally { call.cancel() } // response is auto-closed when source is exhausted
    }.flowOn(Dispatchers.IO)

    // 3. Chat with vision (image analysis, SSE streaming)
    fun chatVision(message: String, imageBase64: String, mode: ThinkingMode, deviceId: String, jwt: String? = null): Flow<StreamResult>

    // 4. Upload file (multipart)
    suspend fun uploadFile(fileData: ByteArray, fileName: String, mimeType: String, deviceId: String, jwt: String? = null): FileUploadResponse

    // 5. Transcribe audio (multipart)
    suspend fun transcribe(audioData: ByteArray, mimeType: String, deviceId: String, jwt: String? = null): String

    // 6. Text-to-speech (returns audio bytes)
    suspend fun speak(text: String, voice: String = "nova", deviceId: String, jwt: String? = null): ByteArray

    // 7. Rate response
    suspend fun rate(responseId: String, stars: Int, mode: ThinkingMode, deviceId: String, comment: String? = null, jwt: String? = null)

    // 8. Submit feedback/flag
    suspend fun feedback(responseId: String, reason: String, mode: ThinkingMode, deviceId: String, detail: String? = null, jwt: String? = null)
}

// API response models (in services/ApiModels.kt)
// NOTE: All API model data classes MUST use `@SerializedName` for any field where JSON key differs from Kotlin property name.
data class DeviceRegisterResponse(
    @SerializedName("device_id") val deviceId: String
)
data class TranscribeResponse(val text: String)
data class FileUploadResponse(val id: String, val filename: String)
data class ApiMessage(val role: String, val content: String)

// Payload types matching iOS exactly
data class StatusPayload(val stage: String, val detail: String? = null)
data class StreamDonePayload(
    @SerializedName("response_id") val responseId: String,
    val usage: TokenUsage? = null,
    val cached: Boolean? = null,
    val category: String? = null
)
data class TokenUsage(
    @SerializedName("input_tokens") val inputTokens: Int,
    @SerializedName("output_tokens") val outputTokens: Int
)
data class StreamErrorPayload(
    val code: String,
    val message: String,
    val partial: Boolean? = null
)

sealed class StreamResult {
    data class Block(val block: ContentBlock) : StreamResult()
    data class Status(val payload: StatusPayload) : StreamResult()
    data class Done(val payload: StreamDonePayload) : StreamResult()
    data class Error(val payload: StreamErrorPayload) : StreamResult()
}
```

**NOTE:** Uses `flow {}` with `flowOn(Dispatchers.IO)` instead of `callbackFlow`. This avoids the `awaitClose` requirement and provides cooperative cancellation via `ensureActive()`. The `finally` block cancels the OkHttp call on Flow cancellation.

**NOTE:** The Android StreamParser has a DIFFERENT interface than iOS. iOS StreamParser.parse(lines:) handles both SSE line accumulation and event dispatch. Android splits this: TradeGuruAPI handles line reading, StreamParser.parse(event, data) handles single-event JSON deserialization. This is an intentional simplification for Android's coroutine Flow architecture.

### AD2: Dependency Injection — Manual DI via AppModule (No Hilt)

No Hilt/Koin/Dagger. Use manual DI with a simple `AppModule` that provides singletons, matching the iOS pattern of static singletons.

```kotlin
// di/AppModule.kt — singletons for services/infra, NOT for ChatEngine
class AppModule(private val context: Context) {
    // Singletons (app-scoped, stateless or shared state)
    val database: TradeGuruDatabase by lazy { TradeGuruDatabase.getInstance(context) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(context) }
    val keychainHelper: KeychainHelper by lazy { KeychainHelper(context) }
    val deviceManager: DeviceManager by lazy { DeviceManager(keychainHelper) }
    val apiConfig: APIConfig by lazy { APIConfig() }
    val authManager: AuthManager by lazy { AuthManager(keychainHelper, apiConfig) }
    val conversationManager: ConversationManager by lazy { ConversationManager(database) }

    // NOT singletons — created per ViewModel (like iOS ChatEngine() in ChatViewModel.init)
    fun createChatEngine() = ChatEngine(deviceManager, authManager)
}

// TradeGuruApp.kt — holds AppModule, sets instance
class TradeGuruApp : Application() {
    lateinit var appModule: AppModule
    override fun onCreate() {
        super.onCreate()
        instance = this
        appModule = AppModule(this)
    }
    companion object { lateinit var instance: TradeGuruApp }
}

// ViewModel factory — creates NEW ChatEngine per ViewModel (matches iOS)
class ChatViewModelFactory(private val appModule: AppModule) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatViewModel(
            engine = appModule.createChatEngine(),
            conversationManager = appModule.conversationManager
        ) as T
}

// In Compose: val viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(app.appModule))
```

**NOTE:** `StreamParser` is an `object` with static methods (stateless, like iOS's `enum StreamParser`). `TradeGuruAPI` is an `object` (stateless, like iOS's `nonisolated enum TradeGuruAPI`). Neither needs DI injection — they're called directly.

### AD3: Room Entity ↔ Domain Model Mapping

Room entities are flat database rows. UI needs rich domain objects. Use extension functions in `DomainMappers.kt`:

```kotlin
// data/DomainMappers.kt — domain models + mapping extensions
data class Conversation(val id: String, val title: String, val mode: ThinkingMode, val messages: List<ChatMessage>, val createdAt: Long, val updatedAt: Long)
data class ChatMessage(val id: String, val role: MessageRole, val blocks: List<ContentBlock>, val timestamp: Long, val mode: ThinkingMode, val attachments: List<MessageAttachment> = emptyList())
data class ContentBlock(val id: String, val type: ContentBlockType, val content: String? = null, val title: String? = null, val steps: List<String>? = null, val items: List<PartsItem> = emptyList(), val language: String? = null, val code: String? = null, val clause: String? = null, val summary: String? = null, val url: String? = null, val rows: List<List<String>>? = null, val headers: List<String>? = null, val level: Int? = null, val style: String? = null)
data class PartsItem(val id: String, val name: String, val spec: String, val qty: Int)
data class MessageAttachment(val id: String, val type: AttachmentType, val fileName: String, val fileSize: Int? = null, val thumbnailData: ByteArray? = null)

// Extension functions
private val gson = Gson()
private val stringListType = object : TypeToken<List<String>>() {}.type
private val nestedListType = object : TypeToken<List<List<String>>>() {}.type

fun ContentBlockEntity.toDomain(parts: List<PartsItemEntity>) = ContentBlock(
    id = id, type = ContentBlockType.fromValue(type) ?: ContentBlockType.TEXT,
    content = content, title = title,
    steps = steps?.let { gson.fromJson<List<String>>(it, stringListType) },
    items = parts.map { it.toDomain() },
    language = language, code = code, clause = clause, summary = summary, url = url,
    rows = rows?.let { gson.fromJson<List<List<String>>>(it, nestedListType) },
    headers = headers?.let { gson.fromJson<List<String>>(it, stringListType) },
    level = level, style = style
)
fun PartsItemEntity.toDomain() = PartsItem(id, name, spec, qty)
fun MessageAttachmentEntity.toDomain() = MessageAttachment(id, AttachmentType.fromValue(type) ?: AttachmentType.IMAGE, fileName, fileSize, thumbnailData)

fun ChatMessageEntity.toDomain(blocks: List<ContentBlock>, attachments: List<MessageAttachment> = emptyList()) =
    ChatMessage(id, MessageRole.fromValue(role) ?: MessageRole.USER, blocks, timestamp, ThinkingMode.fromValue(mode) ?: ThinkingMode.FAULT_FINDER, attachments)

fun ConversationEntity.toDomain(messages: List<ChatMessage> = emptyList()) =
    Conversation(id, title, ThinkingMode.fromValue(mode) ?: ThinkingMode.FAULT_FINDER, messages, createdAt, updatedAt)
```

**Room Query Strategy in ConversationManager** — use `@Transaction` to avoid N+1:
```kotlin
// ConversationManager loads full conversation graph in one transaction on Dispatchers.IO
suspend fun loadConversation(conversationId: String): Conversation? = withContext(Dispatchers.IO) {
    val entity = conversationDao.getById(conversationId) ?: return@withContext null
    val messageEntities = chatMessageDao.getByConversationIdSync(conversationId)
    val messages = messageEntities.map { msg ->
        val blockEntities = contentBlockDao.getByMessageIdSync(msg.id)
        val blocks = blockEntities.map { block ->
            val parts = partsItemDao.getByContentBlockIdSync(block.id)
            block.toDomain(parts)
        }
        val attachments = messageAttachmentDao.getByMessageIdSync(msg.id)
        msg.toDomain(blocks, attachments.map { it.toDomain() })
    }
    entity.toDomain(messages)
}

// For conversation list (sidebar) — lightweight, maps to domain models
fun getAllConversations(): Flow<List<Conversation>> = conversationDao.getAllOrderedByUpdatedAt()
    .map { entities -> entities.map { it.toDomain() } }
    .flowOn(Dispatchers.IO)
```

### AD4: DataStore Preferences Keys

```kotlin
// data/PreferencesManager.kt
val Context.dataStore by preferencesDataStore(name = "tradeguru_prefs")

object PrefsKeys {
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    val HAS_ACCEPTED_DISCLAIMER = booleanPreferencesKey("has_accepted_disclaimer")
    val SELECTED_MODE = stringPreferencesKey("selected_mode")
}
```

NavGraph reads these to determine start destination (Onboarding → Disclaimer → Chat).

### AD5: OAuth — Chrome Custom Tabs + Deep Link

Add `androidx.browser:browser:1.8.0` to dependencies. Add intent filter to AndroidManifest.xml:
```xml
<activity android:name=".MainActivity" ...>
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="tradeguru" android:host="auth-callback" />
    </intent-filter>
</activity>
```

### AD6: Threading Strategy

- Room queries: always `Dispatchers.IO` (Room throws on main thread)
- OkHttp SSE reading: `Dispatchers.IO` (blocking I/O)
- StateFlow emissions: `Dispatchers.Main` (Compose observes on main)
- ViewModelScope: defaults to `Dispatchers.Main.immediate`

### AD7: Block Type Coverage — All 12 Types

| ContentBlockType | View File | Rendering |
|---|---|---|
| TEXT | TextBlockView.kt | 15sp, trade-text |
| HEADING | HeadingBlockView.kt | h1=20sp, h2=18sp, h3=16sp, bold |
| STEP_LIST | StepListView.kt | Green numbered circles |
| WARNING | WarningCardView.kt | Full amber border, 12dp radius |
| CODE | CodeBlockView.kt | Roboto Mono, copy button |
| PARTS_LIST | PartsListView.kt | 3-column grid |
| REGULATION | RegulationView.kt | Purple left border |
| TABLE | TableBlockView.kt | Horizontal scroll |
| CALLOUT | CalloutView.kt | Colored left border |
| DIAGRAM_REF | DiagramRefBlockView.kt | Italic secondary text |
| TOOL_CALL | (inline in BlockRenderer) | Italic secondary text |
| LINK | LinkBlockView.kt | Blue underline, clickable |

BlockRenderer.kt `when(type)` dispatches to the correct composable.

### AD9: ChatEngine Full State Surface (matches iOS exactly)

The ChatEngine is NOT just "consumes Flow, emits StateFlow." It matches iOS ChatEngine's full observable surface:

```kotlin
class ChatEngine(
    private val deviceManager: DeviceManager,
    private val authManager: AuthManager
) {
    private val deviceId: String = deviceManager.getOrCreateDeviceId()
    private val jwt: String? get() = authManager.currentJwt

    // Observable state (matches iOS @Observable properties)
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming
    private val _streamingBlocks = MutableStateFlow<List<ContentBlock>>(emptyList())
    val streamingBlocks: StateFlow<List<ContentBlock>> = _streamingBlocks
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _pipelineStage = MutableStateFlow(PipelineStage.IDLE)
    val pipelineStage: StateFlow<PipelineStage> = _pipelineStage
    var lastResponseId: String? = null; private set

    // Retry state capture (see architecture.md pattern)
    private var lastSendType = SendType.TEXT
    private var lastSendMode = ThinkingMode.FAULT_FINDER
    private var lastAttachments: List<MessageAttachment>? = null

    init { registerDeviceIfNeeded() }

    // ALL methods matching iOS ChatEngine:
    suspend fun fetchResponse(mode: ThinkingMode, conversation: Conversation)
    suspend fun fetchVisionResponse(mode: ThinkingMode, attachments: List<MessageAttachment>?, conversation: Conversation)
    suspend fun uploadDocumentAndChat(mode: ThinkingMode, attachments: List<MessageAttachment>?, conversation: Conversation)
    suspend fun retryLastRequest(conversation: Conversation)
    suspend fun rateLastResponse(stars: Int, comment: String? = null, mode: ThinkingMode)
    suspend fun transcribeAudio(audioFile: File): String?
    suspend fun speakText(text: String): Unit  // plays internally via MediaPlayer (like iOS AVAudioPlayer)
    fun registerDeviceIfNeeded()
    fun clearError() { _error.value = null }
    fun finalizeResponse(mode: ThinkingMode, conversation: Conversation): ChatMessage  // returns message for ViewModel to persist
    fun handleStreamError(error: StreamResult.Error, mode: ThinkingMode, conversation: Conversation): ChatMessage?  // returns partial message or null
}

// NOTE: ChatEngine does NOT take ConversationManager. iOS ChatEngine gets deviceId from DeviceManager
// and JWT from AuthManager internally. Methods that need persistence (finalizeResponse, handleStreamError)
// RETURN results that ChatViewModel then persists via ConversationManager. This matches iOS where
// ChatEngine mutates the passed-in Conversation object and SwiftData auto-persists.
```

### AD10: Auth Flow — Chrome Custom Tabs + Intent Handling

**Note:** `AuthState` and `AuthUser` already exist in `android/.../models/` (created in the boilerplate phase). No new files needed for these types.

```kotlin
// AuthManager.kt — full flow
class AuthManager(private val keychainHelper: KeychainHelper, private val apiConfig: APIConfig) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Anonymous)
    val authState: StateFlow<AuthState> = _authState
    val currentJwt: String? get() = keychainHelper.load("access_token")

    // Step 1: Generate PKCE, open Custom Tab (WorkOS authorize endpoint)
    fun startSignIn(context: Context, provider: String?) {
        val verifier = PKCEHelper.generateVerifier()
        val challenge = PKCEHelper.generateChallenge(verifier)
        keychainHelper.save("pkce_verifier", verifier)
        val state = UUID.randomUUID().toString()
        keychainHelper.save("auth_state", state)
        val url = Uri.parse("https://api.workos.com/user_management/authorize").buildUpon()
            .appendQueryParameter("client_id", apiConfig.workosClientId)
            .appendQueryParameter("redirect_uri", "tradeguru://auth-callback")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .apply { if (provider != null) appendQueryParameter("provider", provider) }
            .build()
        CustomTabsIntent.Builder().build().launchUrl(context, url.toUri())
    }

    // Step 2: Called from MainActivity.onNewIntent() when deep link fires
    suspend fun handleAuthCallback(uri: Uri) {
        val code = uri.getQueryParameter("code") ?: return
        val verifier = keychainHelper.load("pkce_verifier") ?: return
        val tokens = exchangeCode(code, verifier)  // POST to apiConfig.authProxyURL + "/user_management/authenticate"
        val user = JWTDecoder.decode(tokens.accessToken)
        keychainHelper.save("access_token", tokens.accessToken)
        keychainHelper.save("refresh_token", tokens.refreshToken)
        _authState.value = AuthState.Authenticated(user)
    }

    fun signOut() { keychainHelper.clear(); _authState.value = AuthState.Anonymous }

    suspend fun linkDevice(deviceId: String) { /* POST to /device/link, extract tier, update state */ }

    suspend fun restoreSession() {
        val accessToken = keychainHelper.load("access_token") ?: return
        val refreshToken = keychainHelper.load("refresh_token") ?: return
        val expiry = JWTDecoder.getExpiry(accessToken)
        if (expiry != null && expiry > System.currentTimeMillis()) {
            val user = JWTDecoder.decode(accessToken)
            _authState.value = AuthState.Authenticated(user)
        } else {
            // Token expired — try refresh
            try { refreshTokenIfNeeded() } catch (_: Exception) { signOut() }
        }
    }

    suspend fun refreshTokenIfNeeded() {
        val token = keychainHelper.load("access_token") ?: return
        val expiry = JWTDecoder.getExpiry(token)
        if (expiry != null && expiry - System.currentTimeMillis() < 300_000) { /* call /auth/refresh */ }
    }
}

// MainActivity.kt — handle OAuth callback
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    intent.data?.let { uri ->
        if (uri.scheme == "tradeguru" && uri.host == "auth-callback") {
            lifecycleScope.launch { appModule.authManager.handleAuthCallback(uri) }
        }
    }
}
```

### AD11: Build File Changes Required (Task 1 must do these FIRST)

**These changes have NOT been applied yet. Builder-services must execute them as the FIRST step of Task 1.**

```
build.gradle.kts:
  REMOVE: implementation(libs.okhttp.sse)           // line 85
  ADD:    implementation(libs.browser)               // Chrome Custom Tabs

settings.gradle.kts version catalog:
  REMOVE: library("okhttp-sse", ...)                 // line 66
  ADD:    library("browser", "androidx.browser", "browser").version("1.8.0")

AndroidManifest.xml:
  ADD intent filter to MainActivity for tradeguru://auth-callback (see AD5)

proguard-rules.pro:
  ADD: -keep class com.tradeguru.electrical.services.** { *; }  // API models
```

### AD8: DataMigrator.swift — Not Needed for Android

iOS `DataMigrator.swift` handles migration from legacy UserDefaults JSON data to SwiftData. Android starts fresh with Room — no legacy data to migrate. Explicitly excluded.

## Implementation Phases

### Phase 1: Foundation (Services + ViewModels)
Build the data and business logic layer. No UI yet — just the engine that powers everything.
- Services: API client, SSE parser, auth, device management, secure storage
- ViewModels: ChatViewModel, ChatEngine, ConversationManager
- Navigation graph: route definitions
- **Why first:** UI depends on these. Build bottom-up.

### Phase 2: Core UI (Chat Screen + Blocks)
Build the primary screen and all content block components.
- All 12 block types via BlockRenderer + 11 block view files (Text, Heading, Code, StepList, PartsList, Table, Warning, Callout, Regulation, Link, DiagramRef — ToolCall handled inline in BlockRenderer)
- MessageBubble (renders blocks via BlockRenderer)
- ChatScreen components (NavBar, MessageList, ErrorBanner, InputBar, ModeSelector, ModeInfoCard)
- PipelineStatus (animated dots)
- **Why second:** This is 70% of the app. Get it right.

### Phase 3: Secondary Screens
Build remaining screens.
- Onboarding carousel (3 mode pages + final page)
- Safety Disclaimer
- Settings, Sign In, Tier Badge
- Sidebar/Drawer
- Camera
- **Why third:** These are simpler, self-contained screens.

### Phase 4: Integration + Wiring
Wire everything together.
- NavGraph routes all screens
- MainActivity launches NavGraph
- ViewModels injected into screens
- Room data flows through to UI
- **Why fourth:** Everything exists, just needs connecting.

### Phase 5: Hardening + Parity Audit
Final polish.
- Structure audit (SoC, SRP, cohesion)
- Resource audit (coroutine leaks, stream cleanup)
- Code hardening (error handling, edge cases)
- Visual parity audit against iOS HTML
- **Why last:** Polish after substance.

## Team Orchestration

- You operate as the team lead and orchestrate the team to execute the plan.
- You're responsible for deploying the right team members with the right context to execute the plan.
- IMPORTANT: You NEVER operate directly on the codebase. You use `Task` and `Task*` tools to deploy team members to do the building, validating, testing, deploying, and other tasks.
- Take note of the session id of each team member. This is how you'll reference them.

### Team Members

- Builder
  - Name: builder-services
  - Role: Build all Android service files (API, streaming, auth, device, keychain) by reading Swift source + expertise files
  - Agent Type: general-purpose
  - Resume: true

- Builder
  - Name: builder-viewmodels
  - Role: Build ViewModels (ChatViewModel, ChatEngine, ConversationManager) + NavGraph by reading Swift source + expertise files
  - Agent Type: general-purpose
  - Resume: true

- Builder
  - Name: builder-blocks
  - Role: Build all 12 content block Compose views (11 views + BlockRenderer) by reading Swift block views + compose-ui expertise
  - Agent Type: general-purpose
  - Resume: true

- Builder
  - Name: builder-chat-ui
  - Role: Build ChatScreen, MessageBubble, ChatNavBar, ChatMessageList, ChatInputBar, ChatErrorBanner, ModeSelector, ModeInfoCard, PipelineStatus by reading Swift views + expertise
  - Agent Type: general-purpose
  - Resume: true

- Builder
  - Name: builder-secondary
  - Role: Build Onboarding, Disclaimer, Settings, SignIn, TierBadge, Sidebar, Camera screens by reading Swift views
  - Agent Type: general-purpose
  - Resume: true

- Builder
  - Name: builder-wiring
  - Role: Wire NavGraph, update MainActivity, connect ViewModels to screens, ensure Room flows to UI
  - Agent Type: general-purpose
  - Resume: true

- Builder
  - Name: validator-parity
  - Role: Read every Swift file, compare to Kotlin equivalent, report gaps, fix remaining issues
  - Agent Type: general-purpose
  - Resume: true

## Step by Step Tasks

### 1. Build Services Layer
- **Task ID**: build-services
- **Depends On**: none
- **Assigned To**: builder-services
- **Agent Type**: general-purpose
- **Parallel**: true (can run alongside build-viewmodels)
- Read each Swift service file in `ios/Tradeguruelectrical/Services/`
- Read `autoresearch/library/networking.md`, `autoresearch/library/sse.md`, `autoresearch/library/security.md`
- **FIRST: Apply build file changes from AD11** — remove okhttp-sse, add browser dep, update manifest
- Create 8 service files: `APIConfig.kt`, `TradeGuruAPI.kt` (all 8 endpoints per AD1), `StreamParser.kt` + `ApiModels.kt`, `AuthManager.kt` (per AD10), `DeviceManager.kt`, `KeychainHelper.kt`, `PKCEHelper.kt`, `JWTDecoder.kt`
- Create 3 infrastructure files: `di/AppModule.kt` (per AD2), `data/PreferencesManager.kt` (per AD4), `data/DomainMappers.kt` (per AD3)
- **SSE: Use raw OkHttp `flow {}` + `flowOn(Dispatchers.IO)`, NOT okhttp-sse EventSource** (see AD1)
- TradeGuruAPI must implement ALL 8 endpoints: registerDevice, chat, chatVision, uploadFile, transcribe, speak, rate, feedback
- StreamParser is an `object` with static `parse()` method — NOT an injected dependency
- TradeGuruAPI is an `object` — NOT an injected dependency (stateless, like iOS enum)
- AuthManager uses Chrome Custom Tabs + deep link callback per AD10
- Send `platform: "android"` in API calls (not "ios")

### 2. Build ViewModels + NavGraph
- **Task ID**: build-viewmodels
- **Depends On**: none
- **Assigned To**: builder-viewmodels
- **Agent Type**: general-purpose
- **Parallel**: true (can run alongside build-services)
- Read each Swift ViewModel in `ios/Tradeguruelectrical/ViewModels/`
- Read `autoresearch/library/architecture.md`, `autoresearch/library/viewmodel.md`, `autoresearch/library/state-management.md`, `autoresearch/library/coroutines.md`
- Create 3 files: `ChatViewModel.kt`, `ChatEngine.kt`, `ConversationManager.kt`
- ChatViewModel extends `ViewModel`, receives ChatEngine + ConversationManager via `ChatViewModelFactory` (see AD2)
- ChatViewModel must follow thin delegation pattern (< 300 lines)
- ChatEngine exposes full state surface: isStreaming, streamingBlocks, error, pipelineStage, plus all methods per AD9
- ConversationManager wraps Room DAOs, maps entities to domain models via `DomainMappers` (see AD3), exposes `Flow<List<Conversation>>`
- NavGraph.kt created in Task 1 (infrastructure) — defines routes with conditional start destination from DataStore prefs (see AD4)

### 3. Build Content Block Views
- **Task ID**: build-blocks
- **Depends On**: none
- **Assigned To**: builder-blocks
- **Agent Type**: general-purpose
- **Parallel**: true (can run alongside tasks 1 and 2)
- Read each Swift block view in `ios/Tradeguruelectrical/Views/Blocks/`
- Read `autoresearch/library/compose-ui.md`, `autoresearch/library/theming.md`
- Create 12 files in `android/.../ui/views/blocks/`: `BlockRenderer.kt`, `TextBlockView.kt`, `HeadingBlockView.kt`, `CodeBlockView.kt`, `StepListView.kt`, `PartsListView.kt`, `TableBlockView.kt`, `WarningCardView.kt`, `CalloutView.kt`, `RegulationView.kt`, `LinkBlockView.kt`, `DiagramRefBlockView.kt`
- **All 12 ContentBlockType values must have a renderer** (see AD7) — ToolCall handled inline in BlockRenderer
- BlockRenderer.kt: `@Composable fun RenderBlock(block: ContentBlock)` with `when(block.type)` dispatch
- Every view uses `LocalTradeGuruColors.current` for theme colors
- Font sizes, padding, border-radius must match iOS exactly (values in Swift source)
- Code block: Roboto Mono font, copy-to-clipboard button with "Copied!" state
- Warning: full amber border, 12px radius, exclamation icon
- All blocks take domain `ContentBlock` (not entity) as parameter, render purely

### 4. Build Chat Screen Components
- **Task ID**: build-chat-ui
- **Depends On**: build-blocks
- **Assigned To**: builder-chat-ui
- **Agent Type**: general-purpose
- **Parallel**: false (needs blocks first)
- Read Swift files: `ChatView.swift`, `MessageBubble.swift`, `ChatNavBar.swift`, `ChatMessageList.swift`, `ChatInputBar.swift`, `ChatErrorBanner.swift`, `ModeSelector.swift`, `ModeInfoCard.swift`, `PipelineStatusView.swift`, `PipelineStatusDots.swift`
- Read `autoresearch/library/compose-ui.md`, `autoresearch/library/chat-patterns.md`, `autoresearch/library/screens.md`
- Create 10 files in `android/.../ui/` and `android/.../ui/views/chat/`
- MessageBubble: user (green, right-aligned, 16px radius), assistant (surface, left-aligned, renders blocks)
- ChatNavBar: hamburger left, settings + new-chat right (flex spacer between)
- ChatInputBar: plus button (30px), capsule text field (20px radius), mic/send toggle
- ModeSelector: 3 pills (Fault Finder orange, Learn blue, Research purple)
- PipelineStatusDots: 3 animated pulsing dots (8px, 1.2s cycle)
- ChatScreen: composes all above into full screen

### 5. Build Secondary Screens
- **Task ID**: build-secondary
- **Depends On**: build-blocks
- **Assigned To**: builder-secondary
- **Agent Type**: general-purpose
- **Parallel**: true (can run alongside build-chat-ui)
- Read Swift files in `Views/Onboarding/`, `Views/Settings/`, `Views/Legal/`, `Views/SidebarView.swift`, `Views/CameraView.swift`
- Create 9 files: `OnboardingScreen.kt`, `OnboardingPageView.kt`, `OnboardingFinalPageView.kt`, `SafetyDisclaimerScreen.kt`, `SettingsScreen.kt`, `SignInView.kt`, `TierBadgeView.kt`, `SidebarView.kt`, `CameraView.kt`
- Onboarding: HorizontalPager with 4 pages (3 modes + final sign-in/skip)
- Disclaimer: ScrollView with 6 sections, "I Understand" button enabled after scroll-to-bottom
- Settings: List with Account, Data, About sections
- Sidebar: ModalDrawer with search, conversation list, new chat button
- Camera: CameraX preview with placeholder fallback

### 6. Wire Everything Together
- **Task ID**: wire-integration
- **Depends On**: build-services, build-viewmodels, build-chat-ui, build-secondary
- **Assigned To**: builder-wiring
- **Agent Type**: general-purpose
- **Parallel**: false (needs all components)
- Update `MainActivity.kt` to launch `NavGraph` instead of placeholder text
- Wire `ChatViewModel` into `ChatScreen` (pass via `viewModel()`)
- Wire `ConversationManager` to provide Room data as StateFlow
- Wire `ChatEngine` to call `TradeGuruAPI` for SSE streaming
- Wire `AuthManager` to Settings/SignIn screens
- Ensure `AppStorage` equivalents use DataStore Preferences (onboarding completed, disclaimer accepted)
- Test navigation flow: first launch → Onboarding → Disclaimer → Chat

### 7. Parity Validation
- **Task ID**: validate-parity
- **Depends On**: wire-integration
- **Assigned To**: validator-parity
- **Agent Type**: general-purpose
- **Parallel**: false (final gate)
- Read every Swift file, compare line-by-line to Kotlin equivalent
- Check: all 12 content block types rendered
- Check: all 3 modes with correct colors
- Check: all API endpoints called with same parameters
- Check: all navigation routes match iOS flow
- Check: all font sizes, padding, radius match iOS values
- Check: dark mode support on all screens
- Produce parity report (report #14) in `parity/` directory
- Fix any remaining gaps found

## Acceptance Criteria
- [ ] ~45 new Kotlin files created (12 blocks + 10 chat UI + 9 secondary + 8 services + 3 ViewModels + 3 infrastructure)
- [ ] Every Swift view has a Kotlin equivalent
- [ ] All 12 ContentBlockType values render correctly via BlockRenderer
- [ ] SSE streaming works (raw OkHttp source → StreamParser → Flow<StreamResult> → ChatEngine → StateFlow → UI)
- [ ] Navigation: Onboarding → Disclaimer → Chat → Settings → SignIn (conditional via DataStore prefs)
- [ ] Sidebar shows conversations from Room DB via ConversationManager domain models
- [ ] Mode selector switches between 3 modes with correct colors
- [ ] Input bar: text input, mic button, send button, attachment picker
- [ ] Dark mode works on all screens
- [ ] Visual parity report shows >= 95% match
- [ ] Manual DI via AppModule provides all dependencies to ViewModels
- [ ] Room entity → domain model mapping via DomainMappers.kt
- [ ] AndroidManifest.xml has OAuth deep link intent filter

## Validation Commands
- `find android/app/src/main/java/com/tradeguru/electrical -name "*.kt" | wc -l` — should be ~72 (27 existing + ~45 new)
- `grep -r "import androidx.compose" android/app/src/main/java/com/tradeguru/electrical/ui/ | wc -l` — confirms Compose usage
- `grep -r "class.*ViewModel" android/app/src/main/java/com/tradeguru/electrical/viewmodels/ | wc -l` — should be 3
- `grep -r "interface.*Dao" android/app/src/main/java/com/tradeguru/electrical/data/db/dao/ | wc -l` — should be 5 (existing)
- `ls android/app/src/main/java/com/tradeguru/electrical/ui/views/blocks/*.kt | wc -l` — should be 12 (11 views + BlockRenderer)
- `grep -r "AppModule" android/app/src/main/java/com/tradeguru/electrical/ | wc -l` — confirms DI wiring
- `grep -r "tradeguru.*auth-callback" android/app/src/main/AndroidManifest.xml` — confirms OAuth deep link

## Notes
- Dependencies in `build.gradle.kts`: Compose, Room, OkHttp (raw — NOT okhttp-sse EventSource), Coil, CameraX, Security Crypto, DataStore
- **Add `androidx.browser:browser:1.8.0`** to build.gradle for Chrome Custom Tabs OAuth flow
- **Remove `okhttp-sse`** from build.gradle — not needed (raw line reading instead)
- Code Reader expertise in `autoresearch/library/` (98 patterns, 220KB) should be read by every builder before writing code
- The Android HTML preview at `android/preview/chat.html` serves as the visual reference for what each screen should look like
- Swift source files are the behavioral spec — read the Swift, write the Kotlin
- CLAUDE.md rules apply: 200-line view limit, 300-line ViewModel limit
- Tasks 1, 2, 3 can run in parallel (no dependencies between services, ViewModels, and blocks)
- Tasks 4 and 5 can run in parallel (chat UI and secondary screens are independent)
- Task 6 is the integration gate — everything must exist before wiring
- Task 7 is the final validation — produces the parity report
- `DataMigrator.swift` excluded from Android port — iOS-specific legacy data migration (see AD8)
- All Room queries on `Dispatchers.IO`, all StateFlow emissions on `Dispatchers.Main` (see AD6)
- Platform string in API calls: `"android"` (not `"ios"`) — see AD1 services section
