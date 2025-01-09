package com.seljaki.agromajtermobile

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.preference.PreferenceManager
import android.util.Log
import com.seljaki.lib.Block
import com.seljaki.lib.Blockchain
import com.seljaki.lib.BlockchainClient
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
        blockchainClient.onConnect = {
            blockchainClient.onBlockchainReceived = { blockchain: Blockchain ->
                Log.d("blockchain", blockchain.toString())
                this.blockchain.blocks = blockchain.blocks
            }
            blockchainClient.onNewBlockReceived = { block: Block ->
                Log.d("blockchain", "New block: $block")
                blockchain.blocks.add(block)
            }
            blockchainClient.requestBlockchain()
            /*blockchainClient.sendDataToMine(
                Data(
                    4.21,
                    46.56435,
                    16.43213,
                    WeatherPrediction(
                        0.8,
                        0.1,
                        0.1
                    )
                )
            )*/
        }
    }

    fun putString(key: String, value: String) {
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }
}