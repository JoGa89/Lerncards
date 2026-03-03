package com.lerncards.data

import com.lerncards.model.Deck
import com.lerncards.model.DeckImport
import com.lerncards.model.FlashCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

class DeckRepository(
    private val reviewEngine: ReviewEngine = ReviewEngine()
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val _decks = MutableStateFlow<List<Deck>>(emptyList())
    val decks: StateFlow<List<Deck>> = _decks.asStateFlow()

    fun importDeckFromJson(rawJson: String): Result<Unit> {
        return runCatching {
            val importModel = json.decodeFromString<DeckImport>(rawJson)
            require(importModel.cards.isNotEmpty()) { "Der Kartensatz enthält keine Karten." }

            val deck = Deck(
                title = importModel.title,
                description = importModel.description,
                cards = importModel.cards.map {
                    FlashCard(
                        question = it.question,
                        answer = it.answer,
                        useCase = it.useCase
                    )
                }
            )

            _decks.value = _decks.value + deck
        }
    }


    fun renameDeck(deckId: String, newTitle: String) {
        val sanitized = newTitle.trim()
        if (sanitized.isEmpty()) return

        _decks.value = _decks.value.map { deck ->
            if (deck.id == deckId) deck.copy(title = sanitized) else deck
        }
    }

    fun submitAnswer(deckId: String, cardId: String, isCorrect: Boolean) {
        _decks.value = _decks.value.map { deck ->
            if (deck.id != deckId) return@map deck
            deck.copy(cards = deck.cards.map { card ->
                if (card.id != cardId) card else reviewEngine.registerAnswer(card, isCorrect)
            })
        }
    }

    fun nextCard(deckId: String): FlashCard? {
        val deck = _decks.value.firstOrNull { it.id == deckId } ?: return null
        return reviewEngine.chooseNextCard(deck.cards)
    }
}
