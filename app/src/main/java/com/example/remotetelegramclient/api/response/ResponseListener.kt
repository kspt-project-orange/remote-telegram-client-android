package com.example.remotetelegramclient.api.response

interface ResponseListener {
    fun onResponseReceived()

    fun onErrorReceived(data: String?)
}