package com.mediafetch.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MediaFetchApiService {
    @POST("api/v1/analyze")
    suspend fun analyzeUrl(@Body request: AnalyzeRequest): Response<AnalyzeResponse>

    @POST("api/v1/download")
    suspend fun requestDownloadStream(@Body request: DownloadRequest): Response<DownloadResponse>

    @GET("api/v1/health")
    suspend fun checkHealth(): Response<HealthResponse>
}
