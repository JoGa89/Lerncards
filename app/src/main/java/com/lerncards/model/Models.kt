package com.lerncards.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

@Serializable
data class DeckImport(
    val title: String,
    val description: String = "",
    val cards: List<CardImport>
)

@Serializable
data class CardImport(
    val question: String,
    val answer: String,
    @SerialName("use_case")
    val useCase: String = ""
)

data class Deck(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val cards: List<FlashCard>
)

data class FlashCard(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val useCase: String,
    val stats: CardStats = CardStats()
)

data class CardStats(
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val correctStreak: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewedDay: Long? = null,
    val dueDay: Long = LocalDate.now().toEpochDay()
)
