package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.myapplication.ConstantUtil.Companion.TAG
import com.example.myapplication.RetrofitClient.postgre
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import retrofit2.Call
import java.util.Calendar


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()
    private var count = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 노티피케이션 권한 for 13버전
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }


        Firebase.messaging.subscribeToTopic("glucose")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

            }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            
            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, token)
            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        })

        // TextViw count 갱신 코드
        MyFirebaseMessagingService.Events.serviceEvent.observe(
            this,
            Observer<Map<String, String>>
            { fcmMsg ->
                count++
                binding.tvReceivedMsg.text = "count : $count \n$fcmMsg"
            }
        )

        binding.btnTokenUpdate.setOnClickListener {
            postgre.updateFcmToken(1, "I'm not Android.").enqueue(object : retrofit2.Callback<FcmPatient> {
                override fun onResponse(p0: Call<FcmPatient>, p1: retrofit2.Response<FcmPatient>) {
                    Log.d("choco5732", "버튼 눌러서 전송 성공 : " + p1.body().toString())
                }

                override fun onFailure(p0: Call<FcmPatient>, p1: Throwable) {
                    Log.e("choco5732", "실패 : $p1")
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        val cal = Calendar.getInstance()
        val time = cal.time

        Log.d(TAG, "$time")

        postgre.updateStartTime(1).enqueue(object: retrofit2.Callback<FcmPatient> {
            override fun onResponse(p0: Call<FcmPatient>, p1: retrofit2.Response<FcmPatient>) {
                Log.d("choco5732", "시작시간 업데이트 성공 : " + p1.body().toString())
            }

            override fun onFailure(p0: Call<FcmPatient>, p1: Throwable) {
                Log.e("choco5732", "시작시간 업데이트 실패 : $p1")
            }
        })

    }
}