package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface PostgreRemoteDataSource {

    // @GET 등등 postgre에서 데이터를 가져올 코드들
    @GET("/list")
    fun list(): Call<List<FcmPatient>>

//    @PUT("/patients/fcmToken")
//    fun updateFcmToken(@Query("id")id: Long, @Query("newFcmToken")newFcmToken: String): Call<FcmPatient>
//
//    @PUT("/startTime")
//    fun updateStartTime(@Query("id")id: Long): Call<FcmPatient>

    @PUT("/patients/fcmToken")
    fun updateFcmToken(@Query("id")id: Long, @Query("newFcmToken")newFcmToken: String): Call<FcmPatient>

    @PUT("/startTime")
    fun updateStartTime(@Query("id")id: Long): Call<FcmPatient>

    @PUT("/isEnd")
    fun updateIsEnd(@Query("id")id: Long, @Query("isEnd")isEnd: Boolean): Call<FcmPatient>

    @PUT("/isEndFalse")
    fun updateIsEndFalse(@Query("id")id: Long): Call<FcmPatient>
}