package com.gianlucanatoli.padelscore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gianlucanatoli.padelscore.core.*
import java.text.SimpleDateFormat
import java.util.*

private val Orange = Color(0xFFFF9800)
private val Blue = Color(0xFF42A5F5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { App(this) } }
}

@Composable fun App(activity: MainActivity) {
    var state by remember { mutableStateOf(Sync.load(activity)) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Orange)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Text("PadelScore", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))
                if (state != null && !state!!.isFinished) {
                    Text("PARTITA IN CORSO", color = Orange, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp)); Score(state!!)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = { state = PadelScoring.finish(state!!); Sync.save(activity, state!!) }) { Text("Fine partita") }
                } else {
                    Button(onClick = { state = PadelScoring.newMatch(); Sync.save(activity, state!!) }) { Text("Nuova partita") }
                    Spacer(Modifier.height(24.dp)); Text("STORICO PARTITE", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    if (state?.isFinished == true) Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Partita"); Score(state!!); Text("Durata: ${duration(state!!)}") } }
                    else Text("Nessuna partita salvata")
                }
            }
        }
    }
}

@Composable private fun Score(s: MatchState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.weight(1f)) { Column(Modifier.padding(16.dp)) { Text("SQUADRA 1", color = Orange); Text("Set ${s.team1Sets}"); Text("Game ${s.team1Games}"); Text(PadelScoring.displayPoint(s,1), style = MaterialTheme.typography.headlineLarge) } }
        Card(Modifier.weight(1f)) { Column(Modifier.padding(16.dp)) { Text("SQUADRA 2", color = Blue); Text("Set ${s.team2Sets}"); Text("Game ${s.team2Games}"); Text(PadelScoring.displayPoint(s,2), style = MaterialTheme.typography.headlineLarge) } }
    }
}

private fun duration(s: MatchState): String {
    val end = s.finishedAtEpochMs ?: System.currentTimeMillis(); val sec = (end-s.startedAtEpochMs)/1000
    return String.format("%02d:%02d:%02d", sec/3600, (sec%3600)/60, sec%60)
}
