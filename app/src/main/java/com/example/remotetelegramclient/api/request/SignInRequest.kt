package com.example.remotetelegramclient.api.request

class SignInRequest(token: String?, val code: String?) : ApiRequest(token)