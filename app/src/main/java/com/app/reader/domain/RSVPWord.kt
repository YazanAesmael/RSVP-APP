package com.app.reader.domain

data class RSVPWord(
    val fullWord: String,
    val leftPart: String,
    val pivotChar: String,
    val rightPart: String
)

object WordProcessor {
    fun processWord(word: String): RSVPWord {
        if (word.isEmpty()) return RSVPWord("", "", "", "")

        val pivotIndex = calculateSmartPivot(word)
        val safeIndex = pivotIndex.coerceIn(0, word.lastIndex)

        return RSVPWord(
            fullWord = word,
            leftPart = word.take(safeIndex),
            pivotChar = word[safeIndex].toString(),
            rightPart = if (safeIndex + 1 < word.length) word.substring(safeIndex + 1) else ""
        )
    }

    private fun calculateSmartPivot(word: String): Int {
        // 1. Strip punctuation to find the "visual" center of the letters
        val content = word.dropLastWhile { !it.isLetterOrDigit() }
        val len = content.length
        if (len == 0) return word.length / 2

        // 2. THE FIX:
        // Previously, we skewed left for words > 5 chars.
        // Now, we use TRUE CENTER for words up to 13 chars.
        // This makes "waiting" highlight 't', "running" highlight 'n', etc.
        val pivot = when {
            len <= 13 -> (len - 1) / 2

            // Only skew left for massive words where your eye actually needs help
            // finding the start (e.g., "uncharacteristically")
            else -> {
                val spritzIndex = (len * 0.35).toInt()
                val centerIndex = (len - 1) / 2
                (spritzIndex + centerIndex) / 2
            }
        }

        return pivot
    }
}