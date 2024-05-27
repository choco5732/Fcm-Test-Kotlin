package com.example.myapplication

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
import java.util.Objects


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private val viewModel: MainViewModel by viewModels()
    private var count = 0
    private var alertDialog: AlertDialog? = null
    private val mMainThreadHandler = Handler(Looper.getMainLooper())


    private fun showDialogOff(message: String) {
        try {
            val inflater = LayoutInflater.from(this)
            val dialogView: View = inflater.inflate(R.layout.dialog_message, null)

            // 텍스트 뷰의 참조를 얻습니다.
            val textViewMessage = dialogView.findViewById<TextView>(R.id.text_message)
            // 텍스트 뷰에 메시지를 설정합니다.
            // message : 연속혈당측정이 종료되었습니다.\n\n분석 결과를 들으러 병원에 가볼까요?\n기기는 꼭 지금 제거하지 않아도 돼요.
            textViewMessage.text = message

            // AlertDialog를 생성합니다.
            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)
                .setOnDismissListener { dialog: DialogInterface? ->
                    // 마지막 AlertDialog가 닫힐 때 변수 초기화
                    alertDialog = null
                }
            alertDialog = builder.create()
            alertDialog!!.setCancelable(false)
            val buttonOk = dialogView.findViewById<Button>(R.id.buttonOk)

            buttonOk.setOnClickListener { v: View? ->
                // 확인 버튼 클릭 시 처리
                if (alertDialog != null) {
                    alertDialog!!.dismiss()
                    Log.d("choco5732" , "AlertDialog 확인 버튼 눌림!")
                    // db에 종료버튼 눌림 여부 attribute를 true로 바꿔주는 로직 작성
                    postgre.updateIsEnd(1, true).enqueue(object: retrofit2.Callback<FcmPatient> {
                        override fun onResponse(p0: Call<FcmPatient>, p1: retrofit2.Response<FcmPatient>) {
                            Log.d("choco5732", "isEnd값 true로 업데이트 성공")
                            Toast.makeText(this@MainActivity, "isEnd값 true로 업데이트 성공", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(p0: Call<FcmPatient>, p1: Throwable) {
                            Log.e("choco5732", "isEnd 업데이트 실패 : $p1")
                        }
                    })
                }
            }
            // AlertDialog 표시
            mMainThreadHandler.postDelayed(Runnable {
                if (Objects.nonNull(alertDialog)) alertDialog!!.show()
            }, 500)
        } catch (e: Exception) {

        }
    }



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


        // 혈당 구독.. 성공시 토스트 메시지
        Firebase.messaging.subscribeToTopic("glucose")
            .addOnCompleteListener { task ->
                var msg = "혈당 노티 구독 성공!"
                if (!task.isSuccessful) {
                    msg = "혈당 노티 구독 실패.."
                }
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

            }

        // 토큰 불러오기.. 성공시 토스트 메시지(토큰)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            
            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, token)
            Toast.makeText(baseContext, "토큰 : $token", Toast.LENGTH_SHORT).show()
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

        // db 업데이트 테스트
        binding.btnTokenUpdate.setOnClickListener {
            postgre.updateFcmToken(1, "I'm not Android.").enqueue(object : retrofit2.Callback<FcmPatient> {
                override fun onResponse(p0: Call<FcmPatient>, p1: retrofit2.Response<FcmPatient>) {
                    Log.d("choco5732", "버튼 눌러서 전송 성공 : " + p1.body().toString())
                }

                override fun onFailure(p0: Call<FcmPatient>, p1: Throwable) {
                    Log.e("choco5732", "실패 : $p1")
                }
            })
            // select All
//            postgre.list().enqueue(object : retrofit2.Callback<List<FcmPatient>> {
//                override fun onResponse(p0: Call<List<FcmPatient>>, p1: retrofit2.Response<List<FcmPatient>>) {
//                    Log.d("choco5732", "버튼 눌러서 리스트 보기 성공 : " + p1.body().toString())
//                }
//
//                override fun onFailure(p0: Call<List<FcmPatient>>, p1: Throwable) {
//                    Log.e("choco5732", "실패 : $p1")
//                }
//            })
        }

        // 종료 다이얼로그 띄우기
        binding.btnShowDialog.setOnClickListener {
            showDialogOff("연속혈당측정이 종료되었습니다.\n분석 결과를 들으러 병원에 가볼까요?\n기기는 꼭 지금 제거하지 않아도 돼요.")
        }

        // 종료 다이얼로그 초기화 버튼
        binding.btnIsEndFalse.setOnClickListener {
            Log.d("choco5732", "isEnd 초기화 버튼 눌렀습니다.")

            // db에 종료버튼 눌림 여부 attribute를 false로 바꿔주는 로직 작성
            postgre.updateIsEndFalse(1).enqueue(object: retrofit2.Callback<FcmPatient> {
                override fun onResponse(p0: Call<FcmPatient>, p1: retrofit2.Response<FcmPatient>) {
                    Toast.makeText(this@MainActivity, "isEnd값 false로 업데이트 성공", Toast.LENGTH_SHORT).show()
                    Log.d("choco5732", "isEnd값 false로 업데이트 성공")
                }

                override fun onFailure(p0: Call<FcmPatient>, p1: Throwable) {
                    Log.e("choco5732", "isEnd 업데이트 실패 : $p1")
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
                Log.d("choco5732", "시작 시간 업데이트 성공")
            }

            override fun onFailure(p0: Call<FcmPatient>, p1: Throwable) {
                Log.e("choco5732", "시작 시간 업데이트 실패 : $p1")
            }
        })
    }
}