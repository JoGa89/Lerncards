package com.lerncards

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lerncards.model.Deck
import com.lerncards.model.FlashCard
import com.lerncards.ui.theme.LerncardsTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LerncardsTheme {
                LerncardsApp(
                    viewModel = viewModel,
                    loadJson = { uri -> readTextFromUri(uri) }
                )
            }
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        return contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader().readText()
        }.orEmpty()
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LerncardsApp(
    viewModel: AppViewModel,
    loadJson: (Uri) -> String
) {
    val navController = rememberNavController()
    val decks by viewModel.decks.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                decks = decks,
                onOpenDeck = { navController.navigate("review/$it") },
                onRenameDeck = { deckId, title -> viewModel.renameDeck(deckId, title) },
                onJsonImported = { raw -> viewModel.importDeck(raw) },
                loadJson = loadJson
            )
        }
        composable(
            route = "review/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId").orEmpty()
            val deck = decks.firstOrNull { it.id == deckId }
            if (deck == null) {
                navController.popBackStack()
            } else {
                ReviewScreen(
                    deck = deck,
                    nextCard = { viewModel.nextCard(deckId) },
                    onAnswer = { cardId, correct -> viewModel.mark(deckId, cardId, correct) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    decks: List<Deck>,
    onOpenDeck: (String) -> Unit,
    onRenameDeck: (String, String) -> Unit,
    onJsonImported: (String) -> Result<Unit>,
    loadJson: (Uri) -> String
) {
    var status by remember { mutableStateOf("Importiere einen JSON-Kartensatz, um zu starten.") }
    var renameDeckId by remember { mutableStateOf<String?>(null) }
    var renameTitle by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val result = onJsonImported(loadJson(uri))
        status = result.fold(
            onSuccess = { "Kartensatz erfolgreich importiert." },
            onFailure = { "Import fehlgeschlagen: ${it.message}" }
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Lerncards") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { launcher.launch(arrayOf("application/json")) }) {
                Text("JSON Kartensatz hochladen")
            }
            Text(status, style = MaterialTheme.typography.bodyMedium)

            if (decks.isEmpty()) {
                Text("Noch keine Kartensätze vorhanden.")
            } else {
                decks.forEach { deck ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                deck.title,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { onOpenDeck(deck.id) }
                            )
                            Text(deck.description, modifier = Modifier.clickable { onOpenDeck(deck.id) })
                            Text("Karten: ${deck.cards.size}", modifier = Modifier.clickable { onOpenDeck(deck.id) })
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onOpenDeck(deck.id) }) {
                                    Text("Lernen")
                                }
                                TextButton(onClick = {
                                    renameDeckId = deck.id
                                    renameTitle = deck.title
                                }) {
                                    Text("Umbenennen")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (renameDeckId != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { renameDeckId = null },
            title = { Text("Kartensatz umbenennen") },
            text = {
                TextField(
                    value = renameTitle,
                    onValueChange = { renameTitle = it },
                    label = { Text("Neuer Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRenameDeck(renameDeckId!!, renameTitle)
                    renameDeckId = null
                }) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDeckId = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun ReviewScreen(
    deck: Deck,
    nextCard: () -> FlashCard?,
    onAnswer: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    var card by remember(deck.id, deck.cards) { mutableStateOf(nextCard()) }
    var revealed by remember(card?.id) { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(deck.title) },
            navigationIcon = { Button(onClick = onBack) { Text("Zurück") } }
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (card == null) {
                Text("Keine Karte verfügbar")
                return@Column
            }

            val current = card!!
            FlipCard(
                question = current.question,
                answer = current.answer,
                useCase = current.useCase,
                revealed = revealed,
                onToggle = { revealed = !revealed }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    onAnswer(current.id, false)
                    card = nextCard()
                    revealed = false
                }) { Text("Falsch") }
                Button(onClick = {
                    onAnswer(current.id, true)
                    card = nextCard()
                    revealed = false
                }) { Text("Richtig") }
            }
        }
    }
}

@Composable
private fun FlipCard(
    question: String,
    answer: String,
    useCase: String,
    revealed: Boolean,
    onToggle: () -> Unit
) {
    val rotation = if (revealed) 180f else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onToggle() }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .padding(20.dp)
    ) {
        if (!revealed) {
            Column {
                Text("Frage", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Text(question, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(24.dp))
                Text("Tippe, um die Antwort zu sehen.")
            }
        } else {
            Column(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                Text("Antwort", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Text(answer, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(24.dp))
                Text("Anwendungsfall", style = MaterialTheme.typography.titleSmall)
                Text(useCase)
            }
        }
    }
}
