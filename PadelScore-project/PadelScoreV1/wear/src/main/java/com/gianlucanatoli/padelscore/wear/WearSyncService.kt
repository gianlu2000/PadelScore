package com.gianlucanatoli.padelscore.wear

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class WearSyncService : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer) {
        for (event in events) if (event.dataItem.uri.path == "/match/state") {
            val data = DataMapItem.fromDataItem(event.dataItem).dataMap.getString("json") ?: continue
            getSharedPreferences("padel", MODE_PRIVATE).edit().putString("state", data).apply()
        }
    }
}
