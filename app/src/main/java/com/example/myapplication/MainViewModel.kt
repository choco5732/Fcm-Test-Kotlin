package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// 뷰모델은 액티비티나 프래그먼트에서만 사용해야한다.
// 그 외 서비스에는 사용을 권장하지 않는다고 한다.

class MainViewModel: ViewModel() {

    private val _fcmMsg: MutableLiveData<String> = MutableLiveData()
    val fcmMsg: LiveData<String> get() = _fcmMsg


    fun setFcmMsg(msg: String) {
        _fcmMsg.value = msg
    }

}