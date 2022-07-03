package com.kushagra.smsabsorber.sms

import com.kushagra.smsabsorber.api.APIConstants.ENDPOINT_SMS
import com.kushagra.smsabsorber.api.ApiClient
import io.ktor.client.request.*

object SmsRepo {

    suspend fun getShortCodes():List<String>{
        return ApiClient.client.use {
            it.get(ENDPOINT_SMS)
        }
    }

    suspend fun postSms(){
        return ApiClient.client.use {
            it.post(ENDPOINT_SMS)
        }
    }
}