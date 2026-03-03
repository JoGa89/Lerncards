package com.lerncards.data

import com.lerncards.model.CardStats
import com.lerncards.model.FlashCard
import java.time.LocalDate
import kotlin.math.max

class ReviewEngine {

    fun chooseNextCard(cards: List<FlashCard>): FlashCard? {
        if (cards.isEmpty()) return null
        val today = LocalDate.now().toEpochDay()
        val dueCards = cards.filter { it.stats.dueDay <= today }
        val pool = if (dueCards.isNotEmpty()) dueCards else cards
        return pool.maxByOrNull { priority(today, it.stats) }
    }

    fun registerAnswer(card: FlashCard, isCorrect: Boolean): FlashCard {
        val today = LocalDate.now().toEpochDay()
        val updatedStats = if (isCorrect) {
            val newStreak = card.stats.correctStreak + 1
            val nextInterval = when (newStreak) {
                1 -> 1
                2 -> 3
                else -> max(1, (card.stats.intervalDays * card.stats.easeFactor).toInt())
            }
            card.stats.copy(
                intervalDays = nextInterval,
                easeFactor = (card.stats.easeFactor + 0.08).coerceAtMost(3.0),
                correctStreak = newStreak,
                lastReviewedDay = today,
                dueDay = today + nextInterval
            )
        } else {
            val reducedInterval = max(1, card.stats.intervalDays / 2)
            card.stats.copy(
                intervalDays = reducedInterval,
                easeFactor = (card.stats.easeFactor - 0.2).coerceAtLeast(1.3),
                correctStreak = 0,
                wrongCount = card.stats.wrongCount + 1,
                lastReviewedDay = today,
                dueDay = today
            )
        }
        return card.copy(stats = updatedStats)
    }

    private fun priority(today: Long, stats: CardStats): Double {
        val overdueDays = (today - stats.dueDay).coerceAtLeast(0)
        val struggleWeight = stats.wrongCount * 2 + (3 - stats.correctStreak.coerceAtMost(3))
        return overdueDays * 2.5 + struggleWeight + (1.0 / stats.easeFactor)
    }
}
