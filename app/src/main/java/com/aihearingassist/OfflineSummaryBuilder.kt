package com.aihearingassist

class OfflineSummaryBuilder {
    private data class Candidate(val index: Int, val text: String, val score: Int)

    private val speakerLabelRegex = Regex(
        "(^|\\s)(speaker|person)\\s*\\d+\\s*:\\s*|(^|\\s)(you|saved|raw)\\s*:\\s*",
        RegexOption.IGNORE_CASE
    )
    private val sentenceBreakRegex = Regex("(?<=[.!?।])\\s+")
    private val timeOrNumberRegex = Regex("\\b\\d{1,2}(:\\d{2})?\\s?(am|pm)?\\b", RegexOption.IGNORE_CASE)

    private val englishKeywords = listOf(
        "agree", "agreed", "okay", "sure", "yes", "no", "restaurant", "meeting",
        "appointment", "time", "schedule", "class", "office", "doctor", "travel",
        "book", "train", "ticket", "coffee", "pizza", "bus", "payment", "address",
        "tomorrow", "today", "arrive", "come", "reach", "call", "need", "want",
        "plan"
    )

    private val hindiKeywords = listOf(
        "हाँ", "हा", "ठीक", "समय", "बैठक", "ट्रेन", "टिकट", "डॉक्टर", "पैसा",
        "कहाँ", "कब", "बुक", "चलो", "मिलना", "कल", "आज", "आना", "जाना",
        "बस", "पता", "भुगतान", "क्लास"
    )

    fun buildSummary(conversation: String): String {
        val ideas = extractIdeas(conversation)
        if (ideas.isEmpty()) return "No conversation available yet."

        val candidates = ideas.mapIndexed { index, idea ->
            Candidate(index = index, text = idea, score = scoreIdea(idea, index))
        }
        val usefulCandidates = candidates.filter { it.score > 0 }
        val chosen = if (usefulCandidates.isNotEmpty()) {
            usefulCandidates
                .sortedWith(compareByDescending<Candidate> { it.score }.thenBy { it.index })
                .take(4)
                .sortedBy { it.index }
        } else {
            candidates.take(3)
        }

        return chosen.joinToString("\n") { "- ${it.text}" }
    }

    private fun extractIdeas(conversation: String): List<String> {
        val normalized = conversation
            .replace('\t', ' ')
            .replace(Regex("[ ]{2,}"), " ")
            .trim()
        if (normalized.isBlank()) return emptyList()

        val withoutLabels = speakerLabelRegex.replace(normalized, "\n")
        val seen = mutableSetOf<String>()

        return withoutLabels
            .lines()
            .flatMap { it.split(sentenceBreakRegex) }
            .map { it.trim().trim('-', ' ', '\n', '\r') }
            .map { it.replace(Regex("\\s+"), " ") }
            .filter { it.isNotBlank() }
            .filter { seen.add(it.lowercase()) }
    }

    private fun scoreIdea(idea: String, index: Int): Int {
        val lower = idea.lowercase()
        var score = 0
        if (englishKeywords.any { lower.contains(it) }) score += 2
        if (hindiKeywords.any { idea.contains(it) }) score += 2
        if (timeOrNumberRegex.containsMatchIn(idea)) score += 2
        if (idea.endsWith("?") || idea.endsWith("।")) score += 1
        if (index == 0) score += 1
        return score
    }
}
