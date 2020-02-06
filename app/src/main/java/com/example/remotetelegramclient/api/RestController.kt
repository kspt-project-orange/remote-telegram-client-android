package com.example.remotetelegramclient.api

import android.util.Log
import com.example.remotetelegramclient.BuildConfig
import com.example.remotetelegramclient.api.request.AttachDriveRequest
import com.example.remotetelegramclient.api.request.CodeRequest
import com.example.remotetelegramclient.api.request.SignInRequest
import com.example.remotetelegramclient.api.response.ApiResponse
import com.example.remotetelegramclient.api.response.ResponseListener
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RestController(private val listener : ResponseListener) : Callback<ApiResponse?> {
    private var remoteTelegramClientAPI: RemoteTelegramClientAPI? = null
    private var apiServerToken: String? = null

    fun start() {
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        remoteTelegramClientAPI = retrofit.create(RemoteTelegramClientAPI::class.java)
    }

    override fun onResponse(call: Call<ApiResponse?>, response: Response<ApiResponse?>) {
        if (response.isSuccessful) {
            val result: ApiResponse? = response.body()
            apiServerToken = result?.token ?: apiServerToken
            if (result?.status == "OK")
                listener.onResponseReceived(result.toString())
            else
                listener.onErrorReceived(result?.status)
        } else {
            listener.onErrorReceived(response.errorBody().toString())
            Log.w(TAG, response.errorBody().toString())
        }
    }

    override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
        listener.onErrorReceived(t.message)
    }

    fun sendPhone(phone: String?) {
        val call: Call<ApiResponse?>? =
            remoteTelegramClientAPI?.sendPhoneToAPI(
                CodeRequest(
                    apiServerToken,
                    phone
                )
            )
        call?.enqueue(this)
    }

    fun sendCode(code: String?) {
        val call: Call<ApiResponse?>? =
            remoteTelegramClientAPI?.sendCodeToAPI(
                SignInRequest(
                    apiServerToken,
                    code
                )
            )
        call?.enqueue(this)
    }

    fun sendGDriveCredentials(idToken: String?, serverAuthCode: String?) {
        val call: Call<ApiResponse?>? =
            remoteTelegramClientAPI?.sendGDriveCredentialsToAPI(
                AttachDriveRequest(
                    apiServerToken,
                    idToken,
                    serverAuthCode
                )
            )
        call?.enqueue(this)
    }

//    fun sendSignOut() {
//        val call: Call<String?>? =
//            remoteTelegramClientAPI?.sendSignOutToAPI(remoteToken)
//        call?.enqueue(this)
//        "TODO"
//    }

    companion object {
        private const val TAG = "RestController"
    }
}