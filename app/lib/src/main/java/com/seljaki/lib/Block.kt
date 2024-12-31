package com.seljaki.lib

import kotlinx.serialization.Serializable

@Serializable
data class Block (
    val index: Int,
    val timestamp: Long,
    val hash: String,
    val previousHash: String,
    val nonce: Int,
    val difficulty: Int,
    val miner: String?,
    val data: Data
)