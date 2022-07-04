package com.kushagra.smsabsorber.sms

import com.kushagra.smsabsorber.api.APIConstants
import com.kushagra.smsabsorber.api.ApiClient
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import java.lang.Exception
import kotlin.io.use

class SmsServiceImpl(private val apiClient: HttpClient) : SmsService{

    override suspend fun getShortCodes(): Array<String> {
       return try {
           apiClient.get {
               url(APIConstants.ENDPOINT_SMS)
           }
       }catch (e:Exception){
           println("Error ${e.message}")
           emptyArray<String>()
       }
    }

    override suspend fun sendSmsData(data:ArrayList<HashMap<String, ArrayList<String>>>): String? {
        return try {
            apiClient.post<String?> {
                url(APIConstants.ENDPOINT_SMS)
                contentType(ContentType.Application.Json)
                body = data
            }
        }catch (e:Exception){
            println("Error ${e.message}")
           ""
        }
    }
}