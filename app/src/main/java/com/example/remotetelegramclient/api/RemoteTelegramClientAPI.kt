package com.example.remotetelegramclient.api

import com.example.remotetelegramclient.api.request.AttachDriveRequest
import com.example.remotetelegramclient.api.request.CodeRequest
import com.example.remotetelegramclient.api.request.SignInRequest
import com.example.remotetelegramclient.api.response.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RemoteTelegramClientAPI {

    @POST("/v0/auth/requestCode")
    fun sendPhoneToAPI(@Body codeRequest: CodeRequest): Call<ApiResponse?>

    @POST("/v0/auth/signIn")
    fun sendCodeToAPI(@Body signInRequest: SignInRequest): Call<ApiResponse?>

    @POST("/v0/auth/attachDrive")
    fun sendGDriveCredentialsToAPI(@Body attachDriveRequest: AttachDriveRequest): Call<ApiResponse?>

//    @DELETE("signOut")
//    fun sendSignOutToAPI(@Query("q") token: String?): Call<ApiResponse?>
}