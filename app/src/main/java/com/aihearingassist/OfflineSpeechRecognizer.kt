package com.aihearingassist

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException

enum class SpeechLanguage(
    val label: String,
    val shortLabel: String,
    val recognitionTag: String,
    val assetFolder: String,
    val storageFolder: String,
    val modelNote: String,
    val hinglishOutput: Boolean = false
) {
    ENGLISH_IN(
        label = "English (India)",
        shortLabel = "India",
        recognitionTag = "en-IN",
        assetFolder = "model-en-in",
        storageFolder = "model-en-in",
        modelNote = "Best for Indian English and rough local English accents"
    ),
    HINGLISH(
        label = "Hinglish",
        shortLabel = "Hinglish",
        recognitionTag = "hi-IN",
        assetFolder = "model-hi",
        storageFolder = "model-hi",
        modelNote = "Catches Hindi speech and converts it to English letters",
        hinglishOutput = true
    ),
    HINDI(
        label = "Hindi",
        shortLabel = "Hindi",
        recognitionTag = "hi-IN",
        assetFolder = "model-hi",
        storageFolder = "model-hi",
        modelNote = "Hindi speech in Devanagari text"
    ),
    BHOJPURI(
        label = "Bhojpuri",
        shortLabel = "Bhojpuri",
        recognitionTag = "hi-IN",
        assetFolder = "model-hi",
        storageFolder = "model-hi",
        modelNote = "Uses Hindi recognition as the closest lightweight option"
    ),
    ENGLISH_US(
        label = "English (US)",
        shortLabel = "US",
        recognitionTag = "en-US",
        assetFolder = "model-en-us",
        storageFolder = "model-en-us",
        modelNote = "US English offline model"
    ),
    ENGLISH_UK(
        label = "English (UK)",
        shortLabel = "UK",
        recognitionTag = "en-GB",
        assetFolder = "",
        storageFolder = "",
        modelNote = "High accuracy system recognizer option"
    ),
    ENGLISH_AU(
        label = "English (Australia)",
        shortLabel = "AU",
        recognitionTag = "en-AU",
        assetFolder = "",
        storageFolder = "",
        modelNote = "High accuracy system recognizer option"
    );

    val isBundled: Boolean
        get() = assetFolder.isNotBlank() && assetFolder in bundledAssetFolders

    companion object {
        private val bundledAssetFolders: Set<String> by lazy {
            BuildConfig.BUNDLED_SPEECH_MODELS
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toSet()
        }

        fun availableEntries(engine: SpeechEngine): List<SpeechLanguage> {
            if (engine == SpeechEngine.HIGH_ACCURACY_SYSTEM) return entries
            return offlineEntries()
        }

        fun offlineEntries(): List<SpeechLanguage> {
            return entries.filter { it.isBundled }.ifEmpty { listOf(ENGLISH_US) }
        }

        fun defaultLanguage(engine: SpeechEngine): SpeechLanguage {
            if (engine == SpeechEngine.HIGH_ACCURACY_SYSTEM) return ENGLISH_IN
            return listOf(ENGLISH_IN, ENGLISH_US, HINGLISH, HINDI, BHOJPURI)
                .firstOrNull { it.isBundled }
                ?: offlineEntries().first()
        }
    }
}

enum class SpeechEngine(
    val label: String,
    val shortLabel: String,
    val detail: String
) {
    HIGH_ACCURACY_SYSTEM(
        label = "High Accuracy",
        shortLabel = "Google-like",
        detail = "Uses the Android system speech recognizer for frequent, accurate captions"
    ),
    OFFLINE_VOSK(
        label = "Offline",
        shortLabel = "Offline",
        detail = "Uses bundled Vosk models without sending audio to a cloud service"
    )
}

class OfflineSpeechRecognizer {
    private var model: Model? = null
    private var modelAssetFolder: String? = null
    private var speechService: SpeechService? = null
    private var isLoading = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingStart: PendingStart? = null
    private var lastCommittedText = ""

    private data class PendingStart(
        val context: Context,
        val language: SpeechLanguage,
        val onReady: () -> Unit,
        val onPartial: (String) -> Unit,
        val onResult: (String) -> Unit,
        val onError: (String) -> Unit
    )

    fun start(
        context: Context,
        language: SpeechLanguage,
        onReady: () -> Unit,
        onPartial: (String) -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!language.isBundled) {
            onError("${language.label} is only available in High Accuracy mode.")
            return
        }

        val loadedModel = model
        if (loadedModel == null || modelAssetFolder != language.assetFolder) {
            stopSpeechService()
            pendingStart = PendingStart(
                context = context.applicationContext,
                language = language,
                onReady = onReady,
                onPartial = onPartial,
                onResult = onResult,
                onError = onError
            )
            loadPendingModel()
            return
        }

        startListening(onReady, onPartial, onResult, onError)
    }

    fun pause() {
        speechService?.setPause(true)
    }

    fun resume() {
        speechService?.setPause(false)
    }

    fun stop() {
        pendingStart = null
        stopSpeechService()
    }

    private fun stopSpeechService() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
    }

    fun shutdown() {
        stop()
        model?.close()
        model = null
        modelAssetFolder = null
    }

    private fun loadPendingModel() {
        if (isLoading) {
            return
        }

        val start = pendingStart ?: return
        val language = start.language
        isLoading = true
        LibVosk.setLogLevel(LogLevel.INFO)
        StorageService.unpack(
            start.context,
            language.assetFolder,
            language.storageFolder,
            { loaded ->
                val pending = pendingStart
                isLoading = false
                if (pending == null) {
                    loaded.close()
                    return@unpack
                }
                if (pending.language.assetFolder != language.assetFolder) {
                    loaded.close()
                    loadPendingModel()
                    return@unpack
                }

                model?.close()
                model = loaded
                modelAssetFolder = language.assetFolder
                pendingStart = null
                post {
                    startListening(
                        pending.onReady,
                        pending.onPartial,
                        pending.onResult,
                        pending.onError
                    )
                }
            },
            { exception ->
                val pending = pendingStart
                isLoading = false
                if (pending == null) {
                    return@unpack
                }
                if (pending.language.assetFolder != language.assetFolder) {
                    loadPendingModel()
                    return@unpack
                }

                pendingStart = null
                post { pending.onError("${language.label} speech model failed to load: ${exception.message}") }
            }
        )
    }

    private fun startListening(
        onReady: () -> Unit,
        onPartial: (String) -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val loadedModel = model ?: run {
            onError("Offline speech model is not ready yet.")
            return
        }

        stop()
        lastCommittedText = ""
        try {
            val recognizer = Recognizer(loadedModel, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening(
                object : RecognitionListener {
                    override fun onPartialResult(hypothesis: String?) {
                        extractText(hypothesis)?.let { partial ->
                            post { onPartial(partial) }
                        }
                    }

                    override fun onResult(hypothesis: String?) {
                        commitResult(hypothesis, onResult)
                    }

                    override fun onFinalResult(hypothesis: String?) {
                        commitResult(hypothesis, onResult)
                    }

                    override fun onError(exception: Exception?) {
                        post { onError("Offline recognizer error: ${exception?.message ?: "unknown error"}") }
                    }

                    override fun onTimeout() {
                        post { onError("Listening timed out. Press Start again.") }
                    }
                }
            )
            post(onReady)
        } catch (exception: IOException) {
            post { onError("Could not start offline recognizer: ${exception.message}") }
        } catch (exception: RuntimeException) {
            post { onError("Could not start offline recognizer: ${exception.message ?: "initialization failed"}") }
        }
    }

    private fun commitResult(hypothesis: String?, onResult: (String) -> Unit) {
        val text = extractText(hypothesis) ?: return
        if (text.equals(lastCommittedText, ignoreCase = true)) return
        lastCommittedText = text
        post { onResult(text) }
    }

    private fun extractText(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return runCatching {
            val parsed = JSONObject(json)
            val text = parsed.optString("text").ifBlank { parsed.optString("partial") }
            text.trim().takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    private fun post(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }
}
