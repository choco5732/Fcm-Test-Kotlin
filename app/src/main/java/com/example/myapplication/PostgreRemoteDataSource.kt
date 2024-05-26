package com.example.myapplication

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PostgreRemoteDataSource {

    // @GET 등등 postgre에서 데이터를 가져올 코드들
    @GET("/list")
    fun list(): Call<List<FcmPatient>>

    @PUT("/patients/fcmToken")
    suspend fun updateFcmToken(@Path("id") id: Long, @Query("newFcmToken") newFcmToken: String): Call<FcmPatient>

//    @PUT("/patients/{id}/fcmToken")
//    fun updateFcmToken2(@Query("id")id: Long, @Query("newFcmToken")newFcmToken: String): Call<FcmPatient>
    @PUT("/patients/fcmToken")
    fun updateFcmToken2(@Query("id")id: Long, @Query("newFcmToken")newFcmToken: String): Call<FcmPatient>

    @PUT("/startTime")
    fun updateStartTime(@Query("id")id: Long): Call<FcmPatient>
}