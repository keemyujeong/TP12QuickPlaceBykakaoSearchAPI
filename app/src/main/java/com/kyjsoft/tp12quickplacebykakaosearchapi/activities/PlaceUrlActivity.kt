package com.kyjsoft.tp12quickplacebykakaosearchapi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.kyjsoft.tp12quickplacebykakaosearchapi.databinding.ActivityPlaceUrlBinding

class PlaceUrlActivity : AppCompatActivity() {

    val binding by lazy { ActivityPlaceUrlBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.wv.webViewClient = WebViewClient() // 현재 웹 뷰 아나에 웹 문서를 열도록
        binding.wv.webChromeClient = WebChromeClient() // 웹 페이지의 다이얼로그 같은 것들이 발동하도록
        binding.wv.settings.javaScriptEnabled = true // 웹 뷰에서 JS를 실행하도록 설정

        var placeUrl: String = intent.getStringExtra("placeUrl") ?: "" // null일 수도 있으니까 null이면 빈글씨
        binding.wv.loadUrl(placeUrl)


    }

    override fun onBackPressed() {
        if(binding.wv.canGoBack()) binding.wv.goBack() // 뒤로 갈 수 있냐? 그러면 웹뷰를 꺼라라는 거야 왜냐하면 액티비티가 꺼질 수도 있거든
        super.onBackPressed()
    }
}