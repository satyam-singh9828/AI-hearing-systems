package com.aihearingassist

import androidx.compose.runtime.mutableStateListOf

class ConversationManager {
    data class Turn(val speaker: String, val text: String)

    val entries = mutableStateListOf<Turn>()

    fun addTurn(speaker: String, text: String) {
        if (text.isBlank()) return
        entries.add(Turn(speaker.trim(), text.trim()))
    }

    fun clear() {
        entries.clear()
    }

    fun transcript(): String = entries.joinToString("\n") { "${it.speaker}: ${it.text}" }

    fun asPlainText(): String = transcript()

    fun restoreFromTranscript(text: String) {
        clear()
        text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { line ->
                val parts = line.split(": ", limit = 2)
                if (parts.size == 2) {
                    addTurn(parts[0], parts[1])
                } else {
                    addTurn("Saved", line)
                }
            }
    }
}
