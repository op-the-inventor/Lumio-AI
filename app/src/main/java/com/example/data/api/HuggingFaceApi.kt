package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class HfModelInfo(
    val id: String,
    val downloads: Int = 0
)

data class HfFile(
    val type: String,
    val path: String,
    val size: Long = 0L
)

interface HuggingFaceApi {
    @GET("api/models")
    suspend fun searchModels(
        @Query("search") query: String,
        @Query("filter") filter: String = "gguf",
        @Query("limit") limit: Int = 10
    ): List<HfModelInfo>

    @GET("api/models/{modelId}/tree/main")
    suspend fun getModelFiles(
        @Path("modelId", encoded = true) modelId: String
    ): List<HfFile>
}
