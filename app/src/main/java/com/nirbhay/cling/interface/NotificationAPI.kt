package com.nirbhay.cling.`interface`

import com.nirbhay.cling.Constants.Companion.CONTENT_TYPE
import com.nirbhay.cling.Constants.Companion.SERVER_KEY
import com.nirbhay.cling.data.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}