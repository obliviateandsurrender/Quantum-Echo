package com.raedghazal.nyuad_hackathon

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AppApi {

    @POST("test")
    suspend fun getTextFromImage(@Body textFromImageRequest: TextFromImageRequest): Response<TextFromImageResponse>

}
data class TextFromImageRequest(@SerializedName("image") val encodedImage: String)