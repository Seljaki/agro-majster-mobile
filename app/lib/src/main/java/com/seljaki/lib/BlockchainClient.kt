package com.seljaki.lib

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos

val BLOCKCHAIN_MQTT_HOST_URL = "smd.schnapsen66.eu"
val BLOCKCHAIN_MQTT_HOST_PORT = 3335

class BlockchainClient(
    val deviceId: String
)
{
    private var mqttClient = MqttClient.builder()
        .useMqttVersion5()
        .identifier(deviceId)
        .serverHost(BLOCKCHAIN_MQTT_HOST_URL)
        .serverPort(BLOCKCHAIN_MQTT_HOST_PORT)
        .buildAsync()

    var onBlockchainReceived: ((Blockchain) -> Unit)? = null
    var onNewBlockReceived: ((Block) -> Unit)? = null
    var onError: ((Throwable) -> Unit)? = null
    var onConnect: (() -> Unit)? = null

    init {
        mqttClient.connect().whenComplete { connAck, throwable ->
            if (throwable != null) {
                // Handle connection failure
                onError?.invoke(throwable)
            } else {
                // Subscribe to the topic
                println("subscribing to blockchain")
                mqttClient.subscribeWith()
                    .topicFilter("blockchain/response/$deviceId")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .callback { publish ->
                        try {
                            val message = String(publish.payloadAsBytes)
                            //println("Received message: $message")
                            onBlockchainReceived?.invoke(Blockchain.fromJson(message))
                        } catch (e: Exception) {
                            println("Error processing message: $e")
                        }
                    }
                    .send()

                mqttClient.subscribeWith()
                    .topicFilter("blockchain/newBlock")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .callback { publish ->
                        val message = String(publish.payloadAsBytes)
                        println(message)
                        onNewBlockReceived?.invoke(Block.fromJson(message))
                    }
                    .send()
            }
            onConnect?.invoke()
            //requestBlockchain()
        }
    }

    fun requestBlockchain() {
        mqttClient.publishWith()
            .topic("blockchain/get")
            .payload(".".toByteArray())
            .qos(MqttQos.EXACTLY_ONCE)
            .send()
    }

    fun sendDataToMine(data: Data) {
        mqttClient.publishWith()
            .topic("blockchain/add")
            .payload(data.toJson().toByteArray())
            .send()
    }

    fun disconnect() {
        try {
            mqttClient.disconnect()
        } catch (e: Exception) {
            println(e)
        }
    }
}