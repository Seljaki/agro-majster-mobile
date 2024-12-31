package com.seljaki.lib
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Blockchain (
    var blocks: MutableList<Block> = mutableListOf()
) {
    companion object {
        fun fromJson(json: String): Blockchain {
            return Blockchain(
                Json.decodeFromString<MutableList<Block>>(json)
            )
        }
    }
}