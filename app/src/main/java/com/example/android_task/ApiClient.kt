package com.example.android_task

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ApiClient {
    private val client = OkHttpClient()

    fun login(username: String, password: String, context: Context, callback: (String?) -> Unit) {
        val mediaType = "application/json".toMediaType()
        val requestBody = JSONObject().apply {
            put("username", username)
            put("password", password)
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.baubuddy.de/index.php/login")
            .post(requestBody)
            .addHeader("Authorization", "Basic QVBJX0V4cGxvcmVyOjEyMzQ1NmlzQUxhbWVQYXNz")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body
                responseBody?.let { body ->
                    val json = JSONObject(body.string())
                    val accessToken = json.getJSONObject("oauth").getString("access_token")
                    saveAccessToken(context, accessToken)
                    callback(accessToken)
                } ?: callback(null)
            }
        })
    }

    fun getTasks(accessToken: String, callback: (String?) -> Unit) {
        val request = Request.Builder()
            .url("https://api.baubuddy.de/dev/index.php/v1/tasks/select")
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.body?.string())
            }
        })
    }

    private fun saveAccessToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("access_token", token)
            apply() // Save access token asynchronously in SharedPreferences
        }
    }
}
