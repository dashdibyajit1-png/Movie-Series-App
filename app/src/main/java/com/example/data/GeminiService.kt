package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig
import android.util.Log

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    val moshiInstance: Moshi = moshi

    suspend fun askAssistant(userQuery: String): List<ReleaseItem> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiService", "API Key is empty or placeholder!")
            return emptyList()
        }

        val prompt = if (userQuery.trim().isEmpty()) {
            "Generate a list of 6 highly anticipated recent or upcoming releases on Netflix and Prime Video for mid 2026."
        } else {
            userQuery
        }

        val sysInstruction = GeminiContent(
            parts = listOf(
                GeminiPart(
                    text = """
                        You are a streaming release intelligence assistant for Netflix and Prime Video.
                        Your task is to find and list recent, new, or upcoming Movie and TV Show releases on Netflix and Prime Video as requested by the user.
                        If the user asks a generic question, translate their request into a structured list of relevant, newly released, or upcoming movies/shows that they should track.
                        
                        You MUST respond with a valid JSON array of objects. Do not include any markdown formatting wrappers, markdown block qualifiers, other text, or explanation outside the JSON.
                        The JSON model schema is exactly:
                        [
                          {
                            "title": "Title of the movie or show",
                            "platform": "Netflix" or "Prime Video",
                            "releaseDate": "Release Month/Year or Day",
                            "genre": "Genre of the item",
                            "synopsis": "A highly punchy 1-2 sentence description of what the show is about.",
                            "durationOrSeasons": "e.g. 8 Episodes, 1 Season, or 124 Mins"
                          }
                        ]
                        Do not make up ridiculous placeholders; use highly realistic, real releases or highly anticipated additions.
                    """.trimIndent()
                )
            )
        )

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.4f
            ),
            systemInstruction = sysInstruction
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!rawText.isNullOrEmpty()) {
                val arrayAdapter = moshi.adapter<List<ReleaseItem>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, ReleaseItem::class.java)
                )
                // Clean markdown wrappers if any leaked despite instructions
                val cleanedText = rawText.trim()
                    .replace("^```json".toRegex(), "")
                    .replace("^```".toRegex(), "")
                    .replace("```$".toRegex(), "")
                    .trim()
                arrayAdapter.fromJson(cleanedText) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error asking Gemini: ${e.message}", e)
            emptyList()
        }
    }
}
