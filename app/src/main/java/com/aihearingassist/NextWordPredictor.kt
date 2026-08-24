package com.aihearingassist

class NextWordPredictor {
    private val fallbackWords = listOf("go", "visit", "travel", "coffee", "book", "arrive", "come", "reach")

    fun predict(conversation: String, currentText: String): List<String> {
        val normalized = currentText.lowercase().trim()
        val tokens = tokenize(conversation + " " + normalized)
        val fromContext = buildSuggestions(tokens)
        val fromInput = buildSuggestions(tokenize(normalized))

        return (fromContext + fromInput + fallbackWords)
            .distinct()
            .filter { it.isNotBlank() }
            .take(5)
    }

    private fun tokenize(text: String): List<String> {
        return text.split(Regex("[^A-Za-z']+"))
            .map { it.lowercase().trim() }
            .filter { it.isNotBlank() }
    }

    private fun buildSuggestions(tokens: List<String>): List<String> {
        val suggestions = mutableListOf<String>()
        if (tokens.size >= 2) {
            val pair = tokens.takeLast(2)
            suggestions += when (pair.joinToString(" ")) {
                "i want" -> listOf("to", "coffee", "travel")
                "want to" -> listOf("book", "travel", "visit")
                "i will" -> listOf("arrive", "come", "reach")
                else -> emptyList()
            }
        }
        if (tokens.size >= 3) {
            val tri = tokens.takeLast(3)
            suggestions += when (tri.joinToString(" ")) {
                "i want to" -> listOf("book", "travel")
                "what time" -> listOf("is", "will")
                else -> emptyList()
            }
        }
        return suggestions
    }
}
