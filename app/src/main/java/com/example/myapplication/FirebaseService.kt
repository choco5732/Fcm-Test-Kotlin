package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.ConstantUtil.Companion.TAG
import com.example.myapplication.ConstantUtil.Companion.gluconseChannelName
import com.example.myapplication.ConstantUtil.Companion.glucoseChannelId
import com.example.myapplication.ConstantUtil.Companion.glucoseNotificationId
import com.example.myapplication.RetrofitClient.postgre
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call

class MyFirebaseMessagingService : FirebaseMessagingService() {

    object Events {
        val serviceEvent: MutableLiveData<Map<String, String>> by lazy {
            MutableLiveData<Map<String, String>>()
        }
    }

    val channel: NotificationChannel? = null
//    var count: Int = 0

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Events.serviceEvent.postValue(remoteMessage.data)
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // sharedPreference 불러오기
            val pref = getSharedPreferences("pref8", 0)
            var count = pref.getInt("glucoseCount", 0)


            // count 값으로 몇 번 울릴지 설정 예) 10시~ 12시 30분 간격이면 하루 5번, 3일 15번
            if (remoteMessage.data.containsValue("notification") &&
                remoteMessage.data.containsValue("glucose")
//                && count < 3 // 이 값으로 노티 몇번 띄울지 설정
            ) {
                Log.d(TAG, "glucose dataMsg 수신됨!")

                // sharedPreference count 증가값 저장
                count++

                val edit = pref.edit()
                edit.putInt("glucoseCount", count)
                edit.apply()

                // 빌더 .. 노티 아이콘, 제목, 내용 등등
                val builder = NotificationCompat.Builder(applicationContext, glucoseChannelId)
                builder.setSmallIcon(R.drawable.ic_glucose_notification)
                builder.setContentTitle("AGMS 혈당 입력 시간 입니다.")
                builder.setContentText("앱을 실행 하여 혈당 값을 입력해 주세요")
                builder.setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // 인텐트
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                // 펜딩 인텐트
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                builder.setContentIntent(pendingIntent)
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // 버전 구분
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    var notificationChannel =
                        notificationManager.getNotificationChannel(glucoseChannelId)
                    if (notificationChannel == null) {
                        val importance = NotificationManager.IMPORTANCE_HIGH
                        notificationChannel = NotificationChannel(glucoseChannelId, gluconseChannelName, importance)
                        notificationChannel.description
                        notificationChannel.enableVibration(true)
                        notificationManager.createNotificationChannel(notificationChannel)
                    }
                }
                notificationManager.notify(glucoseNotificationId, builder.build())

            }


        }
    }


    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "새로운 토큰 발행: $token")
        sendRegistrationToServer(token)
    }

    private fun scheduleJob() {
        // [START dispatch_job]
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .build()
        WorkManager.getInstance(this)
            .beginWith(work)
            .enqueue()
        // [END dispatch_job]
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendRegistrationToServer(token: String?) {
        postgre.updateFcmToken(1, token.toString()).enqueue(object : retrofit2.Callback<FcmPatient> {
            override fun onResponse(p0: Call<FcmPatient>, p1: retrofit2.Response<FcmPatient>) {
                Log.d("choco5732", "토큰 갱신(등록) 성공 : $token" )
                Toast.makeText(applicationContext, "토큰 갱신(등록) 성공\n$token", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: Call<FcmPatient>, p1: Throwable) {
                Log.e("choco5732", "토큰 갱신(등록) 실패 : $p1")
            }
        })

    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("FCM Message")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }


    internal class MyWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {
        override fun doWork(): Result {
            // TODO(developer): add long running task here.
            return Result.success()
        }
    }
}