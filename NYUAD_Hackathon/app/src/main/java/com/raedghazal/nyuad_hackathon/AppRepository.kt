package com.raedghazal.nyuad_hackathon

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit


object AppRepository {

    private val okHttpClient = OkHttpClient.Builder().also {
        it.connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        it.addInterceptor(logging)
    }

    private val api = Retrofit.Builder()
        .baseUrl("http://10.42.0.1:5000")
        .client(okHttpClient.build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AppApi::class.java)


    suspend fun getTextFromImage(
        imageUri: Uri,
        context: Context
    ): NetworkResponse<TextFromImageResponse>? {
        val imageStream = context.contentResolver.openInputStream(imageUri)
        val selectedImage = BitmapFactory.decodeStream(imageStream)
        val encodedImage = encodeImage(selectedImage) ?: return null
        return safeApiCall { api.getTextFromImage(TextFromImageRequest(encodedImage)) }
    }

    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

}

data class TextFromImageResponse(@SerializedName("result") val result: String)