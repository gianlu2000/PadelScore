# PadelScore V1

Prima versione del contatore punteggio padel per Android + Wear OS.

## Funzioni V1
- Tema scuro Material 3.
- Squadra 1 arancione, Squadra 2 blu.
- Punteggio 0/15/30/40, vantaggi, game, set e tie-break 6-6.
- Match al meglio dei 3 set.
- Annullamento dell'ultimo punto tramite snapshot dello stato.
- Timer partita.
- Fine partita con conferma.
- Salvataggio locale.
- Sincronizzazione telefono ↔ Wear OS tramite Wearable Data Layer; lo stato del watch resta utilizzabile offline.

## Moduli
- `:core`: motore del punteggio e modelli condivisi.
- `:wear`: applicazione Wear OS.
- `:app`: applicazione Android.

## Build
Aprire la cartella in Android Studio recente con Android SDK installato.

```bash
./gradlew :app:assembleDebug :wear:assembleDebug
```

Gli APK vengono prodotti in:
- `app/build/outputs/apk/debug/`
- `wear/build/outputs/apk/debug/`

Per installazione manuale via ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb -s SERIAL_WATCH install -r wear/build/outputs/apk/debug/wear-debug.apk
```

Nota: questa V1 è una base tecnica iniziale; prima della release definitiva andrebbero aggiunti test automatici, firma release e una verifica su un modello reale di Wear OS.
