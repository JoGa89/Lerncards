package com.lerncards

import com.lerncards.data.ReviewEngine
import com.lerncards.model.CardStats
import com.lerncards.model.FlashCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReviewEngineTest {
    private val engine = ReviewEngine()

    @Test
    fun wrongAnswerResetsDueDateToToday() {
        val card = FlashCard(
            question = "Q",
            answer = "A",
            useCase = "U",
            stats = CardStats(intervalDays = 4, correctStreak = 2)
        )

        val updated = engine.registerAnswer(card, isCorrect = false)
        val today = LocalDate.now().toEpochDay()

        assertEquals(today, updated.stats.dueDay)
        assertEquals(0, updated.stats.correctStreak)
    }

    @Test
    fun dueCardsGetPreferred() {
        val today = LocalDate.now().toEpochDay()
        val due = FlashCard(
            question = "Q1",
            answer = "A1",
            useCase = "U1",
            stats = CardStats(dueDay = today - 2, wrongCount = 1)
        )
        val notDue = FlashCard(
            question = "Q2",
            answer = "A2",
            useCase = "U2",
            stats = CardStats(dueDay = today + 5, wrongCount = 6)
        )

        val selected = engine.chooseNextCard(listOf(notDue, due))

        assertTrue(selected?.id == due.id)
    }
}
