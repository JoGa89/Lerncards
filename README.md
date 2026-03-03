# Lerncards

Eine moderne Android-Lernkarten-App (Jetpack Compose) mit adaptivem Wiederholungsalgorithmus.

## Features
- Import von Kartensätzen via JSON-Datei.
- Hauptmenü mit allen importierten Kartensätzen.
- Kartensatznamen direkt im Hauptmenü umbenennen.
- Karten-Session mit Flip-Karte (Frage -> Antwort + Anwendungsfall).
- Selbstkontrolle via **Richtig/Falsch** Buttons.
- Adaptives Scheduling:
  - bekannte Karten werden seltener gezeigt,
  - schwierige Karten häufiger und früher.

## JSON-Format
```json
{
  "title": "Englisch Basics",
  "description": "Wichtige Vokabeln",
  "cards": [
    {
      "question": "apple",
      "answer": "Apfel",
      "use_case": "I eat an apple every morning."
    }
  ]
}
```

## Browser-Demo (zum schnellen Testen)
Falls du ohne Android-Emulator testen willst:
1. `cd web-demo`
2. `python -m http.server 4173`
3. Browser öffnen: `http://localhost:4173`

Die Demo bildet den gleichen Lernfluss ab (Import, Umbenennen, Flip, Richtig/Falsch, adaptive Auswahl).

## Architektur
- `DeckRepository` verwaltet Decks und den Lernfortschritt.
- `ReviewEngine` berechnet Priorität und nächste Fälligkeit pro Karte.
- `MainActivity` + Compose Screens für Home und Review.
