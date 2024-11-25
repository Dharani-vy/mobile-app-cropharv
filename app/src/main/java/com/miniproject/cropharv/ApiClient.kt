package com.miniproject.cropharv

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent"
    private const val API_KEY = "AIzaSyB9wq_b0Rr1md2qYd1KNzwHpSiKtdQFPFE"

    private val client = OkHttpClient()

    fun sendRequest(content: String, callback: Callback) {
        // Create the JSON request body that matches the successful cURL request
        val json = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", content)
                        })
                    })
                })
            })
        }

        // Log the JSON payload to debug the request
        println("JSON Request: $json")

        // Create the request body with content type JSON
        val body = json.toString().toRequestBody("application/json".toMediaType())

        // Build the request URL with the API key in the query parameters
        val request = Request.Builder()
            .url("$BASE_URL?key=$API_KEY")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        // Execute the request and handle possible IOExceptions
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
                callback.onFailure(call, e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback.onResponse(call, response)
                } else {
                    println("Request unsuccessful: ${response.code}")
                    callback.onFailure(call, IOException("Request unsuccessful: ${response.code}"))
                }
            }
        })
    }
}
