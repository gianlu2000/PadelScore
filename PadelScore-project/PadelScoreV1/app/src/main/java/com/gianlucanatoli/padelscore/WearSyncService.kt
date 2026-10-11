package com.gianlucanatoli.padelscore

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.gianlucanatoli.padelscore.core.MatchState
import kotlinx.serialization.json.Json

class WearSyncService : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer) {
        val json = Json { ignoreUnknownKeys = true }
        for (event in events) if (event.dataItem.uri.path == "/match/state") {
            val data = DataMapItem.fromDataItem(event.dataItem).dataMap.getString("json") ?: continue
            val state = json.decodeFromString(MatchState.serializer(), data)
            getSharedPreferences("padel", MODE_PRIVATE).edit().putString("state", data).apply()
            if (state.isFinished) Sync.appendHistory(this, state)
        }
    }
}
