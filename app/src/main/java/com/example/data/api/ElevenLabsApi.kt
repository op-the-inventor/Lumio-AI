package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ElevenLabsVoiceSettings(
    @Json(name = "stability") val stability: Double = 0.5,
    @Json(name = "similarity_boost") val similarityBoost: Double = 0.75,
    @Json(name = "style") val style: Double = 0.0,
    @Json(name = "use_speaker_boost") val useSpeakerBoost: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ElevenLabsTTSRequest(
    @Json(name = "text") val text: String,
    @Json(name = "model_id") val modelId: String = "eleven_multilingual_v2",
    @Json(name = "voice_settings") val voiceSettings: ElevenLabsVoiceSettings = ElevenLabsVoiceSettings()
)

@JsonClass(generateAdapter = true)
data class ElevenLabsSTTResponse(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class ElevenLabsVoiceResponse(
    @Json(name = "voice_id") val voiceId: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class ElevenLabsVoicesListResponse(
    @Json(name = "voices") val voices: List<ElevenLabsVoiceResponse>
)

interface ElevenLabsService {
    @POST("v1/text-to-speech/{voice_id}")
    @Streaming
    suspend fun textToSpeech(
        @Header("xi-api-key") apiKey: String,
        @Path("voice_id") voiceId: String,
        @Body request: ElevenLabsTTSRequest
    ): ResponseBody

    @Multipart
    @POST("v1/speech-to-text")
    suspend fun speechToText(
        @Header("xi-api-key") apiKey: String,
        @Part file: MultipartBody.Part,
        @Part("model_id") modelId: RequestBody
    ): ElevenLabsSTTResponse

    @GET("v1/voices")
    suspend fun getVoices(
        @Header("xi-api-key") apiKey: String
    ): ElevenLabsVoicesListResponse

    @GET("v1/voices/{voice_id}")
    suspend fun getVoice(
        @Header("xi-api-key") apiKey: String,
        @Path("voice_id") voiceId: String
    ): ElevenLabsVoiceResponse

    companion object {
        private const val BASE_URL = "https://api.elevenlabs.io/"

        fun create(): ElevenLabsService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(ElevenLabsService::class.java)
        }
    }
}
