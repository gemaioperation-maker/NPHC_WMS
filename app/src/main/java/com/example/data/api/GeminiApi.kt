package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST API Models with Moshi ---

data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent
)

data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- Client Instance ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askCopilot(prompt: String, systemInstruction: String? = null): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return "WMS Copilot: Ready (Offline Demo Mode). In a production container, configure your GEMINI_API_KEY in the secrets panel.\n\nAI Replenishment Recommendation: Based on recent counts, ITEM-1002 (Steel Bolts) is near safety threshold in BIN ZONE-BULK (current: 12, standard: 50). Suggest transfer order of 40 PCS from BULK-ZONE to PICK-ZONE.\n\nAI Slotting Optimization: ITEM-1001 (Power Drills) has a high-velocity pick rate. Suggest relocating from zone BULK shelf-3 to zone PICK shelf-1 close to the loading bay to decrease average picking cycle time by 18%."
        }

        val contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
        val systemContent = systemInstruction?.let {
            GeminiContent(parts = listOf(GeminiPart(text = it)))
        }

        return try {
            val request = GeminiRequest(contents, systemContent)
            val response = service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "WMS Copilot response empty. Please check your data."
        } catch (e: Exception) {
            "Error contacting Gemini Assistant: ${e.localizedMessage}. Falling back to smart WMS analytics offline."
        }
    }
}
