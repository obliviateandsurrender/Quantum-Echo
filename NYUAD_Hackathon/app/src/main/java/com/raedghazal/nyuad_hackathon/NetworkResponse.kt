package com.raedghazal.nyuad_hackathon

import retrofit2.Response

sealed class NetworkResponse<T>(
    private val response: Response<T>? = null,
    val errorMessage: String = "",
    val error: Throwable? = null,
    val code: Int? = null
) {
    class Success<T>(response: Response<T>) : NetworkResponse<T>(response)
    class Error<T>(errorMessage: String?, error: Throwable? = null, code: Int? = null) :
        NetworkResponse<T>(errorMessage = errorMessage ?: "", error = error, code = code)

    fun subscribe(
        onSuccess: (Response<T>) -> Unit,
        onError: (Pair<Int?, String>) -> Unit = { }
    ) {
        if (response != null && response.isSuccessful) {
            onSuccess(response)
        } else {
            onError(code to errorMessage)
        }
    }

}