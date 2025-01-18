package com.seljaki.agromajtermobile

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.preference.PreferenceManager
import android.util.Log
import com.seljaki.lib.Block
import com.seljaki.lib.Blockchain
import com.seljaki.lib.BlockchainClient
import com.seljaki.lib.Data
import java.util.UUID

class MyApplication : Application() {
    lateinit var pref: SharedPreferences
    lateinit var blockchainClient: BlockchainClient
    var blockchain = Blockchain()
    var imageToPredict: Bitmap? = null;

    lateinit var deviceId: String

    override fun onCreate() {
        super.onCreate()

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        deviceId = pref.getString("deviceId", "") ?: ""
        if (deviceId.isNullOrEmpty() || deviceId == "") {
            deviceId = UUID.randomUUID().toString();
            putString("deviceId", deviceId)
        }

        // blockchain client
        blockchainClient = BlockchainClient(deviceId)
    }

    fun putString(key: String, value: String) {
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }
}
