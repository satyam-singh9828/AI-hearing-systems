import com.aihearingassist.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureLogicTest {
    @Test
    fun contextAwareNGramPredictor_prefersCurrentConversationWords() {
        val conversation = "I want to book a train ticket. I want to travel tomorrow."
        val predictor = NextWordPredictor()
        val suggestions = predictor.predict(conversation, "I want to")
        assertTrue(suggestions.contains("travel") || suggestions.contains("book"))
    }

    @Test
    fun summaryBuilder_extractsCoreIntentFromConversation() {
        val conversation = "Person 1: Would you like to go to the restaurant tonight? Person 2: Yes. What time? Person 1: Let's go at 8 PM. Person 2: Okay."
        val summary = OfflineSummaryBuilder().buildSummary(conversation)

        assertTrue(summary.contains("restaurant") || summary.contains("8 PM"))
    }

    @Test
    fun summaryBuilder_extractsHindiIntentFromConversation() {
        val conversation = "Speaker 1: क्या ट्रेन टिकट बुक हो गया? Speaker 2: हाँ टिकट बुक है।"
        val summary = OfflineSummaryBuilder().buildSummary(conversation)

        assertTrue(summary.contains("ट्रेन") || summary.contains("टिकट"))
    }

    @Test
    fun summaryBuilder_splitsSpeakerLabelsOnSameLine() {
        val conversation = "Speaker 1: Meet the doctor at 7 PM Speaker 2: Okay I will come"
        val summary = OfflineSummaryBuilder().buildSummary(conversation)

        assertTrue(summary.contains("doctor"))
        assertTrue(summary.contains("Okay") || summary.contains("come"))
    }

    @Test
    fun summaryBuilder_reportsEmptyConversation() {
        val summary = OfflineSummaryBuilder().buildSummary("   ")

        assertEquals("No conversation available yet.", summary)
    }

    @Test
    fun conversationManager_preservesTranscriptAndSupportsReset() {
        val manager = ConversationManager()
        manager.addTurn("Speaker 1", "Hello there")
        manager.addTurn("Speaker 2", "Hi")
        assertEquals(2, manager.entries.size)
        manager.clear()
        assertTrue(manager.entries.isEmpty())
    }

    @Test
    fun conversationManager_restoresSavedTranscript() {
        val manager = ConversationManager()
        manager.restoreFromTranscript("Speaker 1: Hello\nYou: I can join")

        assertEquals(2, manager.entries.size)
        assertEquals("Speaker 1", manager.entries.first().speaker)
        assertEquals("I can join", manager.entries.last().text)
    }

    @Test
    fun pseudoDiarization_alternatesSpeakerLabels() {
        val manager = ConversationManager()
        manager.addTurn("Raw", "First voice")
        manager.addTurn("Raw", "Second voice")
        manager.addTurn("Raw", "Third voice")

        val diarization = PseudoDiarization()

        assertEquals("Speaker 1", diarization.nextSpeakerLabel(0))
        assertEquals("Speaker 2", diarization.nextSpeakerLabel(1))
        assertEquals(listOf("Speaker 1", "Speaker 2", "Speaker 1"), diarization.labelTurns(manager.entries))
    }

    @Test
    fun bhojpuriUsesBundledHindiModel() {
        assertEquals(SpeechLanguage.HINDI.assetFolder, SpeechLanguage.BHOJPURI.assetFolder)
        assertEquals(SpeechLanguage.HINDI.storageFolder, SpeechLanguage.BHOJPURI.storageFolder)
    }

    @Test
    fun hinglishUsesHindiRecognizerWithRomanOutput() {
        assertEquals(SpeechLanguage.HINDI.assetFolder, SpeechLanguage.HINGLISH.assetFolder)
        assertTrue(SpeechLanguage.HINGLISH.hinglishOutput)

        val converted = SpeechTextNormalizer.normalize("नमस्ते टिकट बुक है", SpeechLanguage.HINGLISH)
        assertTrue(converted.contains("namaste"))
        assertTrue(converted.contains("tikat"))
        assertTrue(converted.contains("buk"))
    }

    @Test
    fun indianEnglishUsesIndianEnglishModel() {
        assertEquals("model-en-in", SpeechLanguage.ENGLISH_IN.assetFolder)
        assertEquals("model-en-in", SpeechLanguage.ENGLISH_IN.storageFolder)
        assertEquals("en-IN", SpeechLanguage.ENGLISH_IN.recognitionTag)
    }

    @Test
    fun accentModesCoverBundledOfflineModels() {
        val assetFolders = SpeechLanguage.entries.map { it.assetFolder }.toSet()

        assertTrue(assetFolders.contains("model-en-us"))
        assertTrue(assetFolders.contains("model-en-in"))
        assertTrue(assetFolders.contains("model-hi"))
        assertEquals(7, SpeechLanguage.entries.size)
    }

    @Test
    fun highAccuracyModeOffersExtraEnglishAccents() {
        val languages = SpeechLanguage.availableEntries(SpeechEngine.HIGH_ACCURACY_SYSTEM)

        assertTrue(languages.contains(SpeechLanguage.ENGLISH_UK))
        assertTrue(languages.contains(SpeechLanguage.ENGLISH_AU))
        assertTrue(languages.contains(SpeechLanguage.HINGLISH))
    }
}
