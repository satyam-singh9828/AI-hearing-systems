package com.aihearingassist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

private val Cyan = Color(0xFF4DEBFF)
private val CyanDeep = Color(0xFF0E7490)
private val Lavender = Color(0xFF9B8CFF)
private val Mint = Color(0xFF2DD4BF)
private val Warm = Color(0xFFFFB86B)
private val Danger = Color(0xFFFF5C7A)

private data class AppPalette(
    val isDark: Boolean,
    val background: Color,
    val backgroundGradient: List<Color>,
    val panel: Color,
    val panelSoft: Color,
    val panelSunken: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val field: Color,
    val visualGradient: List<Color>,
    val captionGradient: List<Color>
)

private val DarkPalette = AppPalette(
    isDark = true,
    background = Color(0xFF111520),
    backgroundGradient = listOf(Color(0xFF0B1020), Color(0xFF182032), Color(0xFF111520)),
    panel = Color(0xFF1A2030),
    panelSoft = Color(0xFF232B3F),
    panelSunken = Color(0xFF101827),
    textPrimary = Color(0xFFF6FAFF),
    textSecondary = Color(0xFFC7D2E5),
    textMuted = Color(0xFF8D99AE),
    border = Color.White.copy(alpha = 0.08f),
    field = Color(0xFF101827),
    visualGradient = listOf(Color(0xFF10182A), Color(0xFF20314B), Color(0xFF111827)),
    captionGradient = listOf(Color(0xFF111827), Color(0xFF17233A))
)

private val LightPalette = AppPalette(
    isDark = false,
    background = Color(0xFFEAF0FF),
    backgroundGradient = listOf(Color(0xFFF7F9FF), Color(0xFFE5ECFB), Color(0xFFD8E1F4)),
    panel = Color(0xFFF9FBFF),
    panelSoft = Color(0xFFE8EEFA),
    panelSunken = Color(0xFFF1F5FE),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF64748B),
    border = Color(0xFFD1DAEA),
    field = Color(0xFFF4F7FE),
    visualGradient = listOf(Color(0xFFE8EEFF), Color(0xFFF9FBFF), Color(0xFFDCE6F8)),
    captionGradient = listOf(Color(0xFFF9FBFF), Color(0xFFE9F0FD))
)

private val LocalAppPalette = staticCompositionLocalOf { DarkPalette }

private data class FeatureHealth(
    val title: String,
    val state: String,
    val detail: String,
    val color: Color
)

class MainActivity : ComponentActivity() {
    private lateinit var offlineSpeechRecognizer: OfflineSpeechRecognizer
    private lateinit var systemSpeechRecognizer: SystemSpeechRecognizer
    private lateinit var ttsHelper: TextToSpeechHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val conversationManager = ConversationManager()
        val summaryBuilder = OfflineSummaryBuilder()
        val predictor = NextWordPredictor()
        offlineSpeechRecognizer = OfflineSpeechRecognizer()
        systemSpeechRecognizer = SystemSpeechRecognizer()
        val localConversationStore = LocalConversationStore(this)
        ttsHelper = TextToSpeechHelper(this)

        conversationManager.restoreFromTranscript(localConversationStore.loadTranscript())

        setContent {
            AIHearingAssistApp(
                conversationManager = conversationManager,
                summaryBuilder = summaryBuilder,
                predictor = predictor,
                offlineSpeechRecognizer = offlineSpeechRecognizer,
                systemSpeechRecognizer = systemSpeechRecognizer,
                localConversationStore = localConversationStore,
                ttsHelper = ttsHelper
            )
        }
    }

    override fun onDestroy() {
        offlineSpeechRecognizer.shutdown()
        systemSpeechRecognizer.shutdown()
        ttsHelper.shutdown()
        super.onDestroy()
    }
}

@Composable
fun AIHearingAssistApp(
    conversationManager: ConversationManager,
    summaryBuilder: OfflineSummaryBuilder,
    predictor: NextWordPredictor,
    offlineSpeechRecognizer: OfflineSpeechRecognizer,
    systemSpeechRecognizer: SystemSpeechRecognizer,
    localConversationStore: LocalConversationStore,
    ttsHelper: TextToSpeechHelper
) {
    val context = LocalContext.current
    val entries = conversationManager.entries
    val systemDarkTheme = isSystemInDarkTheme()
    val diarization = remember { PseudoDiarization() }
    var useDarkTheme by remember { mutableStateOf(systemDarkTheme) }
    var heardText by remember { mutableStateOf(TextFieldValue()) }
    var currentText by remember { mutableStateOf(TextFieldValue()) }
    var liveCaption by remember { mutableStateOf("Speak after pressing Start. Captions appear here.") }
    var summary by remember(entries.size) {
        mutableStateOf(summaryBuilder.buildSummary(conversationManager.asPlainText()))
    }
    var suggestions by remember { mutableStateOf(predictor.predict(conversationManager.asPlainText(), "")) }
    var captureState by remember { mutableStateOf("Ready") }
    var selectedEngine by remember { mutableStateOf(SpeechEngine.HIGH_ACCURACY_SYSTEM) }
    var statusMessage by remember { mutableStateOf("High Accuracy mode catches rough English, Hindi, and Hinglish when the phone supports it.") }
    var selectedLanguage by remember { mutableStateOf(SpeechLanguage.defaultLanguage(selectedEngine)) }
    val availableLanguages = remember(selectedEngine) { SpeechLanguage.availableEntries(selectedEngine) }
    var pendingCaptionText by remember { mutableStateOf("") }
    var lastSavedSpeechCaption by remember { mutableStateOf("") }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var activityLevel by remember { mutableDoubleStateOf(0.0) }

    fun nextSpeakerLabel(): String = diarization.nextSpeakerLabel(entries.size)

    fun isCaptureActive(): Boolean = captureState == "Listening" || captureState == "Paused" || captureState == "Loading"

    fun stopActiveRecognizer() {
        offlineSpeechRecognizer.stop()
        systemSpeechRecognizer.stop()
    }

    fun pauseActiveRecognizer() {
        if (selectedEngine == SpeechEngine.HIGH_ACCURACY_SYSTEM) {
            systemSpeechRecognizer.pause()
        } else {
            offlineSpeechRecognizer.pause()
        }
    }

    fun resumeActiveRecognizer() {
        if (selectedEngine == SpeechEngine.HIGH_ACCURACY_SYSTEM) {
            systemSpeechRecognizer.resume()
        } else {
            offlineSpeechRecognizer.resume()
        }
    }

    fun refreshSuggestions(text: String = currentText.text) {
        suggestions = predictor.predict(
            conversation = conversationManager.asPlainText() + " " + summary,
            currentText = text
        )
    }

    fun rebuildConversationState(message: String) {
        summary = summaryBuilder.buildSummary(conversationManager.asPlainText())
        localConversationStore.saveTranscript(conversationManager.asPlainText())
        refreshSuggestions()
        statusMessage = message
    }

    fun saveSpeechCaption(
        text: String,
        message: String,
        language: SpeechLanguage = selectedLanguage
    ): Boolean {
        val caption = SpeechTextNormalizer.normalize(text, language)
        if (caption.isBlank()) return false
        if (caption.equals(lastSavedSpeechCaption, ignoreCase = true)) return false

        conversationManager.addTurn(nextSpeakerLabel(), caption)
        lastSavedSpeechCaption = caption
        pendingCaptionText = ""
        liveCaption = caption
        rebuildConversationState(message)
        return true
    }

    fun changeSpeechEngine(engine: SpeechEngine) {
        if (engine == selectedEngine) return
        if (isCaptureActive()) {
            saveSpeechCaption(pendingCaptionText, "Latest caption saved before changing recognizer.")
            stopActiveRecognizer()
            captureState = "Ready"
            liveCaption = "Stopped. Press Start for ${engine.label} captions."
        }

        val nextLanguages = SpeechLanguage.availableEntries(engine)
        selectedEngine = engine
        if (selectedLanguage !in nextLanguages) {
            selectedLanguage = SpeechLanguage.defaultLanguage(engine)
        }
        statusMessage = "${engine.label} selected. ${engine.detail}."
    }

    fun changeSpeechLanguage(language: SpeechLanguage) {
        if (language == selectedLanguage) return
        if (isCaptureActive()) {
            saveSpeechCaption(pendingCaptionText, "Latest caption saved before changing language.")
            stopActiveRecognizer()
            captureState = "Ready"
            liveCaption = "Stopped. Press Start for ${language.label} captions."
        }

        selectedLanguage = language
        statusMessage = "${language.label} selected. ${language.modelNote}."
    }

    fun beginListening() {
        if (captureState != "Ready") {
            statusMessage = "Stop the current capture before starting again."
            return
        }

        val engine = selectedEngine
        val language = selectedLanguage
        captureState = "Loading"
        statusMessage = if (engine == SpeechEngine.HIGH_ACCURACY_SYSTEM) {
            "Starting High Accuracy catcher for ${language.label}."
        } else {
            "Loading ${language.label} offline speech model from this phone."
        }
        liveCaption = "Preparing recognizer..."
        pendingCaptionText = ""

        val onReady = {
            captureState = "Listening"
            statusMessage = if (engine == SpeechEngine.HIGH_ACCURACY_SYSTEM) {
                "Listening with High Accuracy in ${language.label}. Speak naturally near the phone."
            } else {
                "Listening offline in ${language.label}. Speak near the phone."
            }
            liveCaption = "Listening..."
        }
        val onPartial: (String) -> Unit = { partial ->
            val caption = SpeechTextNormalizer.normalize(partial, language)
            pendingCaptionText = caption
            liveCaption = caption.ifBlank { "Listening..." }
            activityLevel = 0.34
        }
        val onResult: (String) -> Unit = { result ->
            saveSpeechCaption(result, "Caption added and summary refreshed.", language)
        }
        val onError: (String) -> Unit = { error ->
            captureState = "Ready"
            statusMessage = error
            liveCaption = "Recognizer stopped."
        }

        if (engine == SpeechEngine.HIGH_ACCURACY_SYSTEM) {
            systemSpeechRecognizer.start(
                context = context,
                language = language,
                onReady = onReady,
                onPartial = onPartial,
                onResult = onResult,
                onError = onError
            )
        } else {
            offlineSpeechRecognizer.start(
                context = context,
                language = language,
                onReady = onReady,
                onPartial = onPartial,
                onResult = onResult,
                onError = onError
            )
        }
    }

    fun startListening() {
        if (!hasAudioPermission) {
            statusMessage = "Microphone permission is needed for live captions."
            return
        }
        beginListening()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) beginListening() else statusMessage = "Microphone permission was denied."
    }

    LaunchedEffect(captureState) {
        while (captureState == "Listening") {
            activityLevel = if (activityLevel > 0.1) 0.12 else 0.26
            delay(350)
        }
        activityLevel = 0.0
    }

    val palette = if (useDarkTheme) DarkPalette else LightPalette
    val colors = if (useDarkTheme) darkColorScheme(
        primary = Cyan,
        secondary = Lavender,
        tertiary = Warm,
        background = palette.background,
        surface = palette.panel,
        surfaceVariant = palette.panelSoft,
        onSurface = palette.textPrimary
    ) else lightColorScheme(
        primary = CyanDeep,
        secondary = Lavender,
        tertiary = Warm,
        background = palette.background,
        surface = palette.panel,
        surfaceVariant = palette.panelSoft,
        onSurface = palette.textPrimary
    )

    val diarizedLabels = diarization.labelTurns(entries.toList())
    val featureHealth = listOf(
        FeatureHealth(
            title = "Voice Capture",
            state = when {
                !hasAudioPermission -> "Permission"
                captureState == "Listening" -> "Active"
                captureState == "Loading" -> "Loading"
                captureState == "Paused" -> "Paused"
                else -> "Ready"
            },
            detail = if (hasAudioPermission) {
                "${selectedEngine.label} catcher is ready for ${selectedLanguage.label}."
            } else {
                "Grant mic permission to test real voice capture."
            },
            color = if (hasAudioPermission) Mint else Warm
        ),
        FeatureHealth(
            title = "Summarisation",
            state = "Local",
            detail = if (entries.isEmpty()) {
                "Waiting for transcript text."
            } else {
                "Summary refreshed from ${entries.size} turn(s)."
            },
            color = Warm
        ),
        FeatureHealth(
            title = "Diarisation",
            state = "Pseudo",
            detail = if (entries.isEmpty()) {
                "Alternating speaker labels are ready."
            } else {
                "${diarizedLabels.distinct().size} visible speaker label(s) across ${entries.size} turn(s)."
            },
            color = Lavender
        ),
        FeatureHealth(
            title = "Text Catcher",
            state = selectedEngine.shortLabel,
            detail = selectedEngine.detail,
            color = Cyan
        )
    )

    MaterialTheme(colorScheme = colors) {
        CompositionLocalProvider(LocalAppPalette provides palette) {
            Surface(modifier = Modifier.fillMaxSize(), color = palette.background) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(palette.backgroundGradient))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .imePadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Header(
                            status = captureState,
                            useDarkTheme = useDarkTheme,
                            onThemeChange = { useDarkTheme = it }
                        )

                        FeatureHealthPanel(features = featureHealth)

                        LiveCaptionPanel(
                            statusMessage = statusMessage,
                            liveCaption = liveCaption,
                            activityLevel = activityLevel,
                            isSpeaking = captureState == "Listening",
                            selectedEngine = selectedEngine,
                            selectedLanguage = selectedLanguage,
                            availableLanguages = availableLanguages,
                            onEngineChange = { changeSpeechEngine(it) },
                            onLanguageSelect = { changeSpeechLanguage(it) },
                            heardText = heardText,
                            onHeardTextChange = { heardText = it },
                            onAddHeardSpeech = {
                                val heard = heardText.text.trim()
                                if (heard.isNotBlank()) {
                                    conversationManager.addTurn(nextSpeakerLabel(), heard)
                                    lastSavedSpeechCaption = heard
                                    heardText = TextFieldValue()
                                    rebuildConversationState("Caption added and summary refreshed.")
                                }
                            },
                            onStart = {
                                if (hasAudioPermission) {
                                    startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            onPause = {
                                pauseActiveRecognizer()
                                val saved = saveSpeechCaption(pendingCaptionText, "Partial caption saved and summary refreshed.")
                                captureState = "Paused"
                                if (!saved) liveCaption = "Paused"
                                statusMessage = if (saved) {
                                    "Paused and saved the latest caption."
                                } else {
                                    "Paused. Resume when the speaker continues."
                                }
                            },
                            onResume = {
                                resumeActiveRecognizer()
                                captureState = "Listening"
                                liveCaption = "Listening..."
                                statusMessage = "Listening with ${selectedEngine.label} in ${selectedLanguage.label}."
                            },
                            onStop = {
                                val saved = saveSpeechCaption(pendingCaptionText, "Latest caption saved and summary refreshed.")
                                stopActiveRecognizer()
                                captureState = "Ready"
                                if (!saved) liveCaption = "Stopped. Press Start for live captions."
                                statusMessage = if (saved) {
                                    "Microphone stopped and latest caption saved."
                                } else {
                                    "Microphone stopped."
                                }
                            },
                            canStart = captureState == "Ready",
                            canPause = captureState == "Listening",
                            canResume = captureState == "Paused",
                            canStop = captureState == "Listening" || captureState == "Paused" || captureState == "Loading"
                        )

                        GeneratedTextPanel(generatedText = conversationManager.asPlainText())

                        SummaryPanel(
                            summary = summary,
                            onCreateSummary = {
                                summary = summaryBuilder.buildSummary(conversationManager.asPlainText())
                                localConversationStore.saveTranscript(conversationManager.asPlainText())
                                statusMessage = "Summary generated from the text block."
                            }
                        )

                        ResponsePanel(
                            text = currentText,
                            suggestions = suggestions,
                            onTextChange = {
                                currentText = it
                                refreshSuggestions(it.text)
                            },
                            onSuggestion = { suggestion ->
                                val separator = if (currentText.text.isBlank()) "" else " "
                                val updated = currentText.text + separator + suggestion
                                currentText = currentText.copy(text = updated)
                                refreshSuggestions(updated)
                            },
                            onSpeak = {
                                val spoken = currentText.text.trim()
                                if (spoken.isNotBlank()) {
                                    conversationManager.addTurn("You", spoken)
                                    val spoke = ttsHelper.speak(spoken, selectedLanguage)
                                    currentText = TextFieldValue()
                                    rebuildConversationState(
                                        if (spoke) {
                                            "Response spoken in ${selectedLanguage.label}, saved, and summary refreshed."
                                        } else {
                                            "${selectedLanguage.label} offline TTS voice is not installed. Response saved."
                                        }
                                    )
                                }
                            }
                        )

                        TranscriptPanel(
                            modifier = Modifier.fillMaxWidth(),
                            entries = entries.toList(),
                            onSave = {
                                localConversationStore.saveTranscript(conversationManager.asPlainText())
                                summary = summaryBuilder.buildSummary(conversationManager.asPlainText())
                                statusMessage = "Transcript saved on this device."
                            },
                            onShare = {
                                val transcript = conversationManager.asPlainText()
                                if (transcript.isBlank()) {
                                    statusMessage = "Nothing to share yet."
                                } else {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "AI Hearing Assist Transcript")
                                        putExtra(Intent.EXTRA_TEXT, transcript)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share transcript"))
                                }
                            },
                            onClear = {
                                conversationManager.clear()
                                heardText = TextFieldValue()
                                currentText = TextFieldValue()
                                pendingCaptionText = ""
                                lastSavedSpeechCaption = ""
                                suggestions = predictor.predict("", "")
                                summary = summaryBuilder.buildSummary("")
                                localConversationStore.saveTranscript("")
                                stopActiveRecognizer()
                                captureState = "Ready"
                                liveCaption = "Speak after pressing Start. Captions appear here."
                                statusMessage = "Conversation cleared."
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    status: String,
    useDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AI Hearing Assist",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Text(
                text = "Offline Communication Assistant",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeToggle(useDarkTheme = useDarkTheme, onThemeChange = onThemeChange)
            StatusPill(status)
        }
    }
}

@Composable
private fun ThemeToggle(useDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.panel.copy(alpha = if (palette.isDark) 0.82f else 0.9f))
            .border(1.dp, palette.border, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (useDarkTheme) "Dark" else "Light",
            color = palette.textSecondary,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
        Switch(checked = useDarkTheme, onCheckedChange = onThemeChange)
    }
}

@Composable
private fun FeatureHealthPanel(features: List<FeatureHealth>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        features.forEach { feature ->
            FeatureHealthCard(feature)
        }
    }
}

@Composable
private fun FeatureHealthCard(feature: FeatureHealth) {
    val palette = LocalAppPalette.current
    Column(
        modifier = Modifier
            .width(176.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.panel.copy(alpha = if (palette.isDark) 0.9f else 0.94f))
            .border(1.dp, feature.color.copy(alpha = 0.26f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(feature.color)
            )
            Text(
                text = feature.state,
                color = feature.color,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = feature.title,
            color = palette.textPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = feature.detail,
            color = palette.textMuted,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusPill(status: String) {
    val palette = LocalAppPalette.current
    val color = when (status) {
        "Listening" -> Mint
        "Paused" -> Warm
        "Loading" -> Lavender
        else -> palette.textSecondary
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.panelSoft)
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(status, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

@Composable
private fun LiveCaptionPanel(
    statusMessage: String,
    liveCaption: String,
    activityLevel: Double,
    isSpeaking: Boolean,
    selectedEngine: SpeechEngine,
    selectedLanguage: SpeechLanguage,
    availableLanguages: List<SpeechLanguage>,
    onEngineChange: (SpeechEngine) -> Unit,
    onLanguageSelect: (SpeechLanguage) -> Unit,
    heardText: TextFieldValue,
    onHeardTextChange: (TextFieldValue) -> Unit,
    onAddHeardSpeech: () -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    canStart: Boolean,
    canPause: Boolean,
    canResume: Boolean,
    canStop: Boolean
) {
    val palette = LocalAppPalette.current
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panel),
        border = BorderStroke(1.dp, Cyan.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("Live Caption Console", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                }
                SpeakingWobble(isSpeaking = isSpeaking, activityLevel = activityLevel)
            }
            CaptionEngineVisual(isSpeaking = isSpeaking, selectedLanguage = selectedLanguage)
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary
            )
            SpeechOptionsPicker(
                selectedEngine = selectedEngine,
                selectedLanguage = selectedLanguage,
                availableLanguages = availableLanguages,
                onEngineChange = onEngineChange,
                onLanguageSelect = onLanguageSelect
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.verticalGradient(palette.captionGradient))
                    .border(1.dp, Cyan.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = liveCaption,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary
                )
            }
            LinearProgressIndicator(
                progress = { activityLevel.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = Cyan,
                trackColor = palette.panelSoft
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        text = "Start",
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        color = Mint,
                        onClick = onStart,
                        enabled = canStart,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Pause",
                        icon = { Icon(Icons.Default.Pause, contentDescription = null) },
                        color = Warm,
                        enabled = canPause,
                        onClick = onPause,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        text = "Resume",
                        icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null) },
                        color = Lavender,
                        enabled = canResume,
                        onClick = onResume,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Stop",
                        icon = { Icon(Icons.Default.Stop, contentDescription = null) },
                        color = Danger,
                        enabled = canStop,
                        onClick = onStop,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            OutlinedTextField(
                value = heardText,
                onValueChange = onHeardTextChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("Manual caption correction") },
                colors = appTextFieldColors()
            )
            Button(
                onClick = onAddHeardSpeech,
                enabled = heardText.text.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Heard Speech and Summarize")
            }
        }
    }
}

@Composable
private fun CaptionEngineVisual(isSpeaking: Boolean, selectedLanguage: SpeechLanguage) {
    val palette = LocalAppPalette.current
    val transition = rememberInfiniteTransition(label = "engine-glow")
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "engine-pulse"
    )
    val scale = if (isSpeaking) pulse else 0.92f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(palette.visualGradient))
            .border(1.dp, Cyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(106.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Cyan.copy(alpha = 0.95f), Lavender.copy(alpha = 0.8f), Color(0xFF26324C))
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.42f), RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(66.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.panelSunken.copy(alpha = 0.8f))
                    .border(1.dp, Cyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = palette.textPrimary, modifier = Modifier.size(32.dp))
            }
        }
        Text(
            text = selectedLanguage.label,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(50))
                .background(palette.panelSunken.copy(alpha = 0.86f))
                .border(1.dp, Cyan.copy(alpha = 0.24f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            color = palette.textPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SpeechOptionsPicker(
    selectedEngine: SpeechEngine,
    selectedLanguage: SpeechLanguage,
    availableLanguages: List<SpeechLanguage>,
    onEngineChange: (SpeechEngine) -> Unit,
    onLanguageSelect: (SpeechLanguage) -> Unit
) {
    val palette = LocalAppPalette.current
    var languageMenuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.panelSunken)
            .border(1.dp, Cyan.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Catcher Mode", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeechModeButton(
                engine = SpeechEngine.HIGH_ACCURACY_SYSTEM,
                selectedEngine = selectedEngine,
                onEngineChange = onEngineChange,
                modifier = Modifier.weight(1f)
            )
            SpeechModeButton(
                engine = SpeechEngine.OFFLINE_VOSK,
                selectedEngine = selectedEngine,
                onEngineChange = onEngineChange,
                modifier = Modifier.weight(1f)
            )
        }

        Text("Language / Accent", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { languageMenuOpen = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Cyan.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.textPrimary),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Cyan)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = selectedLanguage.label,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Cyan)
            }
            DropdownMenu(
                expanded = languageMenuOpen,
                onDismissRequest = { languageMenuOpen = false },
                modifier = Modifier
                    .background(palette.panel)
                    .border(1.dp, Cyan.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
            ) {
                availableLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(language.label, color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        language.modelNote,
                                        color = palette.textMuted,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (language == selectedLanguage) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Mint)
                                }
                            }
                        },
                        onClick = {
                            languageMenuOpen = false
                            onLanguageSelect(language)
                        }
                    )
                }
            }
        }
        Text(
            text = "${selectedEngine.detail}. ${selectedLanguage.modelNote}.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary
        )
    }
}

@Composable
private fun SpeechModeButton(
    engine: SpeechEngine,
    selectedEngine: SpeechEngine,
    onEngineChange: (SpeechEngine) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = engine == selectedEngine
    val palette = LocalAppPalette.current
    val color = if (selected) Cyan else palette.border

    OutlinedButton(
        onClick = { onEngineChange(engine) },
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = if (selected) 0.8f else 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Cyan.copy(alpha = 0.14f) else palette.panelSunken,
            contentColor = if (selected) Cyan else palette.textSecondary
        )
    ) {
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(engine.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = LocalAppPalette.current.textPrimary,
    unfocusedTextColor = LocalAppPalette.current.textPrimary,
    focusedContainerColor = LocalAppPalette.current.field,
    unfocusedContainerColor = LocalAppPalette.current.field,
    focusedBorderColor = Cyan,
    unfocusedBorderColor = LocalAppPalette.current.border,
    focusedLabelColor = Cyan,
    unfocusedLabelColor = LocalAppPalette.current.textSecondary,
    cursorColor = Cyan
)

@Composable
private fun SpeakingWobble(isSpeaking: Boolean, activityLevel: Double) {
    val transition = rememberInfiniteTransition(label = "speaking-wave")
    val wobble by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 280),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )
    val scale = if (isSpeaking) 1f + activityLevel.toFloat().coerceIn(0.05f, 0.35f) else 1f
    val color = if (isSpeaking) Color(0xFF16A34A) else MaterialTheme.colorScheme.surfaceVariant

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(7.dp)
                    .height((18 + index * 5).dp)
                    .graphicsLayer {
                        scaleY = if (isSpeaking) scale + (index * 0.05f) else 0.55f
                        translationY = if (isSpeaking) wobble * (if (index % 2 == 0) 1 else -1) else 0f
                    }
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: @Composable () -> Unit,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .defaultMinSize(minWidth = 76.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color(0xFF07111F),
            disabledContainerColor = color.copy(alpha = 0.22f),
            disabledContentColor = Color.White.copy(alpha = 0.42f)
        ),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
    ) {
        icon()
        Spacer(Modifier.width(5.dp))
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ResponsePanel(
    text: TextFieldValue,
    suggestions: List<String>,
    onTextChange: (TextFieldValue) -> Unit,
    onSuggestion: (String) -> Unit,
    onSpeak: () -> Unit
) {
    val palette = LocalAppPalette.current
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panel),
        border = BorderStroke(1.dp, Lavender.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Your Response", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("Type message for speech") },
                colors = appTextFieldColors()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { suggestion ->
                    OutlinedButton(
                        onClick = { onSuggestion(suggestion) },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                        border = BorderStroke(1.dp, Cyan.copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan)
                    ) {
                        Text(suggestion)
                    }
                }
            }
            Button(
                onClick = onSpeak,
                modifier = Modifier.fillMaxWidth(),
                enabled = text.text.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Color(0xFF0E1020))
            ) {
                Icon(Icons.Default.RecordVoiceOver, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Speak and Add to Transcript")
            }
        }
    }
}

@Composable
private fun GeneratedTextPanel(generatedText: String) {
    val palette = LocalAppPalette.current
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panel),
        border = BorderStroke(1.dp, Cyan.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Generated Text", fontWeight = FontWeight.Bold, color = palette.textPrimary)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 118.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.panelSunken)
                    .border(1.dp, Cyan.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = generatedText.ifBlank { "Live captions and manual captions will collect here." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (generatedText.isBlank()) palette.textMuted else palette.textPrimary
                )
            }
        }
    }
}

@Composable
private fun SummaryPanel(summary: String, onCreateSummary: () -> Unit) {
    val palette = LocalAppPalette.current
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panel),
        border = BorderStroke(1.dp, Warm.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Summarisation Tool", fontWeight = FontWeight.Bold, color = palette.textPrimary)
                Text("Local", color = Warm, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 86.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.panelSunken)
                    .border(1.dp, Warm.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Text(summary, style = MaterialTheme.typography.bodyLarge, color = palette.textPrimary)
            }
            Button(
                onClick = onCreateSummary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Warm, contentColor = Color(0xFF1B1308))
            ) {
                Text("Summarise Generated Text")
            }
        }
    }
}

@Composable
private fun TranscriptPanel(
    modifier: Modifier = Modifier,
    entries: List<ConversationManager.Turn>,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onClear: () -> Unit
) {
    val palette = LocalAppPalette.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panel),
        border = BorderStroke(1.dp, palette.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Complete Transcript", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ColorIconButton(
                        color = Color(0xFF0F766E),
                        onClick = onSave,
                        icon = { Icon(Icons.Default.Save, contentDescription = "Save transcript", tint = Color.White) }
                    )
                    ColorIconButton(
                        color = Color(0xFF2563EB),
                        onClick = onShare,
                        icon = { Icon(Icons.Default.Share, contentDescription = "Share transcript", tint = Color.White) }
                    )
                    ColorIconButton(
                        color = Color(0xFFDC2626),
                        onClick = onClear,
                        icon = { Icon(Icons.Default.Delete, contentDescription = "Clear transcript", tint = Color.White) }
                    )
                }
            }
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.panelSunken)
                        .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        "Transcript appears here as the conversation grows.",
                        color = palette.textMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    entries.takeLast(12).forEachIndexed { index, turn ->
                        val visibleIndex = entries.size - entries.takeLast(12).size + index
                        TranscriptRow(turn = turn, index = visibleIndex)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorIconButton(
    color: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
        content = icon
    )
}

@Composable
private fun TranscriptRow(turn: ConversationManager.Turn, index: Int) {
    val palette = LocalAppPalette.current
    val isUser = turn.speaker.equals("You", ignoreCase = true)
    val accent = if (isUser) Lavender else Cyan
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.panelSunken)
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${index + 1}",
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f))
                .padding(top = 5.dp),
            color = accent,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(turn.speaker, color = accent, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(turn.text, style = MaterialTheme.typography.bodyLarge, color = palette.textPrimary)
        }
    }
}
