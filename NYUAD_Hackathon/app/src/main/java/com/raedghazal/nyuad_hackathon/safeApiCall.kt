package com.raedghazal.nyuad_hackathon

import retrofit2.Response

suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): NetworkResponse<T> {
    try {
        val response = call.invoke()
        if (response.isSuccessful) {
            return NetworkResponse.Success(response)
        }
        return NetworkResponse.Error(response.message(), code = response.code())
    } catch (e: Exception) {
        return NetworkResponse.Error(NETWORK_ERROR, e)
    }
}

const val NETWORK_ERROR = "network_error"
