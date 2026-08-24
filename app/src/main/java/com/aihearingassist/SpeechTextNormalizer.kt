package com.aihearingassist

object SpeechTextNormalizer {
    fun normalize(text: String, language: SpeechLanguage): String {
        val cleaned = text
            .replace(Regex("\\s+"), " ")
            .replace(" i ", " I ")
            .trim()
        if (cleaned.isBlank()) return ""

        return if (language.hinglishOutput) {
            HinglishTransliterator.toLatin(cleaned)
        } else {
            cleaned
        }
    }
}

object HinglishTransliterator {
    private val independentVowels = mapOf(
        'अ' to "a",
        'आ' to "aa",
        'इ' to "i",
        'ई' to "ee",
        'उ' to "u",
        'ऊ' to "oo",
        'ए' to "e",
        'ऐ' to "ai",
        'ओ' to "o",
        'औ' to "au",
        'ऋ' to "ri"
    )

    private val consonants = mapOf(
        'क' to "k",
        'ख' to "kh",
        'ग' to "g",
        'घ' to "gh",
        'ङ' to "n",
        'च' to "ch",
        'छ' to "chh",
        'ज' to "j",
        'झ' to "jh",
        'ञ' to "n",
        'ट' to "t",
        'ठ' to "th",
        'ड' to "d",
        'ढ' to "dh",
        'ण' to "n",
        'त' to "t",
        'थ' to "th",
        'द' to "d",
        'ध' to "dh",
        'न' to "n",
        'प' to "p",
        'फ' to "ph",
        'ब' to "b",
        'भ' to "bh",
        'म' to "m",
        'य' to "y",
        'र' to "r",
        'ल' to "l",
        'व' to "v",
        'श' to "sh",
        'ष' to "sh",
        'स' to "s",
        'ह' to "h"
    )

    private val vowelMarks = mapOf(
        'ा' to "aa",
        'ि' to "i",
        'ी' to "ee",
        'ु' to "u",
        'ू' to "oo",
        'े' to "e",
        'ै' to "ai",
        'ो' to "o",
        'ौ' to "au",
        'ृ' to "ri",
        'ं' to "n",
        'ँ' to "n",
        'ः' to "h"
    )

    fun toLatin(text: String): String {
        val raw = buildString {
            var index = 0
            while (index < text.length) {
                val current = text[index]
                val next = text.getOrNull(index + 1)
                val consonant = consonants[current]

                when {
                    current == 'क' && next == '्' && text.getOrNull(index + 2) == 'ष' -> {
                        val afterCluster = text.getOrNull(index + 3)
                        appendWithInherentVowel("ksh", afterCluster)
                        index += if (afterCluster != null && (vowelMarks.containsKey(afterCluster) || afterCluster == '्')) 4 else 3
                    }
                    current == 'त' && next == '्' && text.getOrNull(index + 2) == 'र' -> {
                        val afterCluster = text.getOrNull(index + 3)
                        appendWithInherentVowel("tr", afterCluster)
                        index += if (afterCluster != null && (vowelMarks.containsKey(afterCluster) || afterCluster == '्')) 4 else 3
                    }
                    current == 'ज' && next == '्' && text.getOrNull(index + 2) == 'ञ' -> {
                        val afterCluster = text.getOrNull(index + 3)
                        appendWithInherentVowel("gy", afterCluster)
                        index += if (afterCluster != null && (vowelMarks.containsKey(afterCluster) || afterCluster == '्')) 4 else 3
                    }
                    consonant != null -> {
                        appendWithInherentVowel(consonant, next)
                        index += if (next != null && (vowelMarks.containsKey(next) || next == '्')) 2 else 1
                    }
                    independentVowels.containsKey(current) -> {
                        append(independentVowels.getValue(current))
                        index++
                    }
                    vowelMarks.containsKey(current) -> {
                        append(vowelMarks.getValue(current))
                        index++
                    }
                    current == '।' -> {
                        append('.')
                        index++
                    }
                    else -> {
                        append(current)
                        index++
                    }
                }
            }
        }

        return raw
            .replace(Regex("\\b([a-zA-Z]{3,})a\\b")) { match -> match.groupValues[1] }
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun StringBuilder.appendWithInherentVowel(base: String, next: Char?) {
        when {
            next == '्' -> append(base)
            next != null && vowelMarks.containsKey(next) -> append(base).append(vowelMarks.getValue(next))
            else -> append(base).append('a')
        }
    }
}
