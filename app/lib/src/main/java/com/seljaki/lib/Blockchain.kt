package com.seljaki.lib
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Blockchain (
    val blocks: MutableList<Block>
) {
    companion object {
        fun fromJson(json: String): Blockchain {
            return Blockchain(
                Json.decodeFromString<MutableList<Block>>(json)
            )
        }
    }
}