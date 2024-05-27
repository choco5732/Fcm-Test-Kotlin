package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class FcmPatient(
    @SerializedName("patient_id")
    val patient_id: Int? = null,
    @SerializedName("patient_name")
    val patient_name: String? = null,
    @SerializedName("start_time")
    val start_time: String? = null,
    @SerializedName("fcm_token")
    val fcm_token: String? = null,
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("isend")
    val isEnd: Boolean? = null
)
