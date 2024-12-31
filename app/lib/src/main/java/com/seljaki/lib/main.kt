package com.seljaki.lib

import kotlinx.serialization.json.Json

fun main() {
    println("hello world")

    val blockchainJson = """
        [
            {
                "index": 0,
                "timestamp": 1735636519,
                "hash": "OCBa7XWlpDVdAlxMsXIpblVynCNcPkfV4gWJZI7T71I=",
                "previousHash": "AA==",
                "nonce": 0,
                "difficulty": 1,
                "miner": null,
                "data": {
                    "UUID": "2cf6d1a9-9f29-4348-b2c7-4d6badc7bde7",
                    "temperature": 0,
                    "longitude": 0,
                    "latitude": 0,
                    "prediction": {
                        "clear": 1,
                        "cloudy": 0,
                        "rainy": 0
                    }
                }
            },
            {
                "index": 1,
                "timestamp": 1735636568,
                "hash": "MDC5qiyB2W6TfdHq89xk2HDfzV38jh+O0GotN+TUywA=",
                "previousHash": "OCBa7XWlpDVdAlxMsXIpblVynCNcPkfV4gWJZI7T71I=",
                "nonce": 2095392869,
                "difficulty": 2,
                "miner": "Minerebcdc1b3-b42e-448b-9847-80125da75c2d",
                "data": {
                    "UUID": "9ff3622f-b7c4-432b-bc63-e09181d811fd",
                    "temperature": 13,
                    "longitude": 45.62131,
                    "latitude": 16.3454,
                    "prediction": {
                        "clear": 0.1,
                        "cloudy": 0.4,
                        "rainy": 0.5
                    }
                }
            }
        ]
    """.trimIndent()
    val blockchain = Blockchain.fromJson(blockchainJson)
    println(blockchain)
}