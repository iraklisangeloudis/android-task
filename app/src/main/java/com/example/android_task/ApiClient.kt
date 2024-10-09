package com.example.android_task

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ApiClient(private val context: Context) {
    private val client = OkHttpClient()

    suspend fun login(username: String, password: String): String? {
        return withContext(Dispatchers.IO) {
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

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string())
                    val accessToken = json.getJSONObject("oauth").getString("access_token")
                    saveAccessToken(context, accessToken)
                    accessToken
                } else {
                    null
                }
            } catch (e: IOException) {
                null
            }
        }
    }

    suspend fun getTasks(accessToken: String): String? {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://api.baubuddy.de/dev/index.php/v1/tasks/select")
                .get()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    null
                }
            } catch (e: IOException) {
                null
            }
        }
    }

    private fun saveAccessToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("access_token", token)
            apply() // Save access token asynchronously in SharedPreferences
        }
    }
}
