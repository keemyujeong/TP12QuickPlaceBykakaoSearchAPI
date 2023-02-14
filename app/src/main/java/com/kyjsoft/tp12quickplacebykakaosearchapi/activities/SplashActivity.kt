package com.kyjsoft.tp12quickplacebykakaosearchapi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 화면을 별도의 xml로 맨들어서 뷰를 생성할 필요가 없음. theme로 배경이미지를 보여주고 넘어가도됨.

//        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
//            override fun run() {
//
//            }
//        },1500) // 1.5초 후에 중괄호 안에가 실행

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        },1500)
    }
}