package com.example.remotetelegramclient.api.request

class AttachDriveRequest(token: String?, val driveIdToken: String?, val driveServerAuthCode: String?) : ApiRequest(token)