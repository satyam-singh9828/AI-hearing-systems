package com.aihearingassist

class PseudoDiarization {
    fun nextSpeakerLabel(turnCount: Int): String {
        val normalizedCount = turnCount.coerceAtLeast(0)
        return if (normalizedCount % 2 == 0) "Speaker 1" else "Speaker 2"
    }

    fun labelTurns(conversation: List<ConversationManager.Turn>): List<String> {
        return conversation.mapIndexed { index, _ ->
            nextSpeakerLabel(index)
        }
    }
}
