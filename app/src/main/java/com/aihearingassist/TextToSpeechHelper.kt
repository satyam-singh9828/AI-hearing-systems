package com.aihearingassist

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            ready = result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE
        }
    }

    fun speak(text: String, language: SpeechLanguage = SpeechLanguage.ENGLISH_US): Boolean {
        if (!ready) return false
        val locale = when (language) {
            SpeechLanguage.ENGLISH_US -> Locale.US
            SpeechLanguage.ENGLISH_IN -> Locale("en", "IN")
            SpeechLanguage.ENGLISH_UK -> Locale.UK
            SpeechLanguage.ENGLISH_AU -> Locale("en", "AU")
            SpeechLanguage.HINGLISH,
            SpeechLanguage.HINDI,
            SpeechLanguage.BHOJPURI -> Locale("hi", "IN")
        }
        val result = tts.setLanguage(locale)
        val languageAvailable = result == TextToSpeech.LANG_AVAILABLE ||
            result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
            result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
        if (!languageAvailable) return false
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AIHearingAssist")
        return true
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
