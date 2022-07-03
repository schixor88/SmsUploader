package com.kushagra.smsabsorber.api

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import io.ktor.http.*

object ApiClient {

    val client = HttpClient(Android){

        //Serializer
        install(JsonFeature){
            serializer = GsonSerializer()
        }

        //Timeouts
        install(HttpTimeout){
            requestTimeoutMillis = 15000L
            connectTimeoutMillis = 15000L
            socketTimeoutMillis = 15000L
        }

        //Logs
        install(Logging){
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("Logger Ktor=>",message)
                }
            }
            level = LogLevel.ALL
        }

        //Response in CLI
        install(ResponseObserver){
            onResponse { response ->
                Log.d("HTTP Status:","${response.status.value}")
            }
        }

        //Headers
        install(DefaultRequest){
            header("Accept", "application/json")
            header("Content-type", "application/json")
            contentType(ContentType.Application.Json)
            //Pass your token
//            header("Authorization", "Bearer ${SessionManager.userToken}")
        }
    }
//    fun getClient():HttpClient{
//            return client
//    }

}