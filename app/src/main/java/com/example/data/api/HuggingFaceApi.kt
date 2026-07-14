package com.example.data.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class HfModelInfo(
    val id: String,
    val downloads: Int = 0
)

@JsonClass(generateAdapter = true)
data class HfFile(
    val type: String,
    val path: String,
    val size: Long = 0L
)

interface HuggingFaceApi {
    @GET("api/models")
    suspend fun searchModels(
        @retrofit2.http.Header("Authorization") authHeader: String?,
        @Query("search") query: String,
        @Query("filter") filter: String = "gguf",
        @Query("sort") sort: String = "trendingScore",
        @Query("direction") direction: Int = -1,
        @Query("limit") limit: Int = 20
    ): List<HfModelInfo>

    @GET("api/models/{modelId}/tree/main")
    suspend fun getModelFiles(
        @retrofit2.http.Header("Authorization") authHeader: String?,
        @Path("modelId", encoded = true) modelId: String
    ): List<HfFile>
}
