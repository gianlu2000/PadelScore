package com.gianlucanatoli.padelscore.wear

import android.content.Context
import com.gianlucanatoli.padelscore.core.MatchState
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.serialization.json.Json

object WearSync {
    private val json = Json { ignoreUnknownKeys = true }
    fun save(context: Context, state: MatchState) {
        context.getSharedPreferences("padel", Context.MODE_PRIVATE).edit().putString("state", json.encodeToString(MatchState.serializer(), state)).apply()
        val req = PutDataMapRequest.create("/match/state")
        req.dataMap.putString("json", json.encodeToString(MatchState.serializer(), state)); req.dataMap.putLong("updated", System.currentTimeMillis()); req.setUrgent()
        Wearable.getDataClient(context).putDataItem(req.asPutDataRequest())
    }
    fun load(context: Context): MatchState? = context.getSharedPreferences("padel", Context.MODE_PRIVATE).getString("state", null)?.let { json.decodeFromString(MatchState.serializer(), it) }
}
