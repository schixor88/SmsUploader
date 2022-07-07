package com.kushagra.smsabsorber.sms

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*

interface SmsService {

    suspend fun getShortCodes():Array<String>

    suspend fun sendSmsData(data:HashMap<String, ArrayList<String>>):String?

    companion object{
        fun create():SmsService{
            return  SmsServiceImpl(
                apiClient = HttpClient(Android){
                    install(Logging){
                        level = LogLevel.ALL
                    }
                    install(JsonFeature){
                        serializer = GsonSerializer()
                    }
                }
            )
        }
    }
}