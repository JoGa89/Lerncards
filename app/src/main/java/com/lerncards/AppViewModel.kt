package com.lerncards

import androidx.lifecycle.ViewModel
import com.lerncards.data.DeckRepository
import com.lerncards.model.Deck
import com.lerncards.model.FlashCard
import kotlinx.coroutines.flow.StateFlow

class AppViewModel(
    private val repository: DeckRepository = DeckRepository()
) : ViewModel() {
    val decks: StateFlow<List<Deck>> = repository.decks

    fun importDeck(json: String): Result<Unit> = repository.importDeckFromJson(json)

    fun nextCard(deckId: String): FlashCard? = repository.nextCard(deckId)

    fun renameDeck(deckId: String, newTitle: String) {
        repository.renameDeck(deckId, newTitle)
    }

    fun mark(deckId: String, cardId: String, isCorrect: Boolean) {
        repository.submitAnswer(deckId, cardId, isCorrect)
    }
}
