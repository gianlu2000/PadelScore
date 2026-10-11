package com.gianlucanatoli.padelscore

import android.content.Context
import com.gianlucanatoli.padelscore.core.MatchState
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object Sync {
    private val json = Json { ignoreUnknownKeys = true }
    private const val PREF = "padel"
    private const val STATE = "state"
    private const val HISTORY = "history"

    fun save(context: Context, state: MatchState) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(STATE, json.encodeToString(MatchState.serializer(), state)).apply()
        if (state.isFinished) appendHistory(context, state)
        val req = PutDataMapRequest.create("/match/state")
        req.dataMap.putString("json", json.encodeToString(MatchState.serializer(), state))
        req.dataMap.putLong("updated", System.currentTimeMillis()); req.setUrgent()
        Wearable.getDataClient(context).putDataItem(req.asPutDataRequest())
    }
    fun load(context: Context): MatchState? = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(STATE, null)?.let { json.decodeFromString(MatchState.serializer(), it) }
    fun history(context: Context): List<MatchState> = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(HISTORY, null)?.let { json.decodeFromString(ListSerializer(MatchState.serializer()), it) } ?: emptyList()
    fun appendHistory(context: Context, state: MatchState) {
        val old = history(context).filterNot { it.startedAtEpochMs == state.startedAtEpochMs }
        val next = (listOf(state) + old).take(100)
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(HISTORY, json.encodeToString(ListSerializer(MatchState.serializer()), next)).apply()
    }
}
