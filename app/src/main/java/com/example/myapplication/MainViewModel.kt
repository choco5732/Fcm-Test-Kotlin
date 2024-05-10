package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    private val _fcmMsg: MutableLiveData<String> = MutableLiveData()
    val fcmMsg: LiveData<String> get() = _fcmMsg


    fun setFcmMsg(msg: String) {
        _fcmMsg.value = msg
    }

}