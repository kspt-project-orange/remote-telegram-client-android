package com.example.remotetelegramclient.api.response

interface ResponseListener {
    fun onResponseReceived(data: String?)

    fun onErrorReceived(data: String?)
}