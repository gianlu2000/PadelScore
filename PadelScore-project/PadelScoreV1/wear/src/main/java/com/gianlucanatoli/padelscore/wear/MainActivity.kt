package com.gianlucanatoli.padelscore.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ScreenScaffold
import com.gianlucanatoli.padelscore.core.*

private val Orange = Color(0xFFFF9800)
private val Blue = Color(0xFF42A5F5)
private val Dark = Color(0xFF0B0B0D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { WearApp(this) } }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable fun WearApp(activity: MainActivity) {
    var state by remember { mutableStateOf(WearSync.load(activity)) }
    val history = remember { mutableStateListOf<MatchState>() }
    MaterialTheme(colorScheme = darkColorScheme(primary = Orange, background = Dark, surface = Color(0xFF17171A))) {
        if (state == null || state!!.isFinished) {
            Box(Modifier.fillMaxSize().background(Dark), contentAlignment = Alignment.Center) {
                Button(onClick = { state = PadelScoring.newMatch(); WearSync.save(activity, state!!) }) { Text("NUOVA PARTITA") }
            }
        } else {
            val pager = rememberPagerState(pageCount = { 3 })
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> ScorePage(state!!, onPoint = { team -> history.add(state!!); state = PadelScoring.point(state!!, team); WearSync.save(activity, state!!) }, onUndo = { if (history.isNotEmpty()) { state = history.removeAt(history.lastIndex); WearSync.save(activity, state!!) } }, canUndo = history.isNotEmpty())
                    1 -> DetailPage(state!!)
                    2 -> EndPage(state!!, onFinish = { state = PadelScoring.finish(state!!); WearSync.save(activity, state!!) })
                }
            }
        }
    }
}

@Composable private fun ScorePage(s: MatchState, onPoint: (Int)->Unit, onUndo: ()->Unit, canUndo: Boolean) {
    Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (s.inTieBreak) "TIE-BREAK" else "SET ${s.team1Sets + s.team2Sets + 1} · ${s.team1Games}-${s.team2Games}", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TeamBox(Modifier.weight(1f), "1", Orange, PadelScoring.displayPoint(s,1)) { onPoint(1) }
            TeamBox(Modifier.weight(1f), "2", Blue, PadelScoring.displayPoint(s,2)) { onPoint(2) }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.fillMaxWidth()) { Text("↶  ANNULLA") }
        Spacer(Modifier.height(4.dp)); Text("Swipe →", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable private fun TeamBox(modifier: Modifier, name: String, color: Color, score: String, onClick: ()->Unit) {
    Column(modifier.aspectRatio(0.9f).border(2.dp, color, RoundedCornerShape(14.dp)).clickable { onClick() }.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("SQUADRA $name", color = color, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(3.dp)); Text(score, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun DetailPage(s: MatchState) {
    Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("RISULTATO", fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { Text("🟧", color = Orange); Text("🟦", color = Blue) }
        s.completedSets.forEachIndexed { i, set -> Text("SET ${i+1}     ${set.team1}     ${set.team2}") }
        if (s.completedSets.isEmpty()) Text("Nessun set concluso")
        Spacer(Modifier.height(8.dp)); Text("GAME     ${s.team1Games}     ${s.team2Games}")
        Text("PUNTI    ${PadelScoring.displayPoint(s,1)}     ${PadelScoring.displayPoint(s,2)}")
    }
}

@Composable private fun EndPage(s: MatchState, onFinish: ()->Unit) {
    var confirm by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) } }
    val sec = ((now - s.startedAtEpochMs).coerceAtLeast(0))/1000
    Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("DURATA", fontWeight = FontWeight.Bold); Spacer(Modifier.height(4.dp)); Text(formatDuration(sec), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(14.dp)); Button(onClick = { confirm = true }) { Text("FINE PARTITA") }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm=false }, title = { Text("Terminare la partita?") }, text = { Text("Il risultato verrà salvato nello storico.") }, confirmButton = { TextButton(onClick = { confirm=false; onFinish() }) { Text("TERMINA") } }, dismissButton = { TextButton(onClick={confirm=false}) { Text("ANNULLA") } })
}

private fun formatDuration(sec: Long) = String.format("%02d:%02d:%02d", sec/3600, (sec%3600)/60, sec%60)
