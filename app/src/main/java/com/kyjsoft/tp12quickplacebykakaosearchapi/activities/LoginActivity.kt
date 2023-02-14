package com.kyjsoft.tp12quickplacebykakaosearchapi.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.kyjsoft.tp12quickplacebykakaosearchapi.G
import com.kyjsoft.tp12quickplacebykakaosearchapi.databinding.ActivityLoginBinding
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.NidUserInfoResponse
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.UserAccount
import com.kyjsoft.tp12quickplacebykakaosearchapi.network.RetrofitApiService
import com.kyjsoft.tp12quickplacebykakaosearchapi.network.RetrofitHelper
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
        setContentView(binding.root)

        // 둘러보기 글씨 클릭으로 로그인 없이 main 화면으로 이동
        binding.tvGo.setOnClickListener {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        // 회원가입
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }

        binding.layoutEmail.setOnClickListener {
            startActivity((Intent(this@LoginActivity, EmailLoginActivity::class.java)))
        }

        // 간편 로그인 버튼
        binding.btnLoginGoogle.setOnClickListener { clickedLoginGoogle() }
        binding.btnLoginKakao.setOnClickListener { clickedLoginKakao() }
        binding.btnLoginNaver.setOnClickListener { clickedLoginNaver() }

    }

    fun clickedLoginNaver(){
        // 네이버 개발자 센터 가이드 문서 참고 -> 애플리케이션 등록도 완료해라
        // 네이버 아이디 로그인 SDK를 초기화
        NaverIdLoginSDK.initialize(this, "lVd1gGYBP65oKGe9y9_R","oiwF7SgNML", "장소찾기")
        // 네이버 전용 버튼 뷰를 사용안하고 authenticate메소드 사용
        NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback{
            override fun onError(errorCode: Int, message: String) {
                Toast.makeText(this@LoginActivity, "error : ${message}", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Toast.makeText(this@LoginActivity, "실패 : ${message}", Toast.LENGTH_SHORT).show()
            }

            override fun onSuccess() {
                Toast.makeText(this@LoginActivity, "성공~!", Toast.LENGTH_SHORT).show()
                // 회원정보를 받아오기

                // 사용자정보를 가져오려면 서버와 HTTP REST API통신을 해야함
                // 단 필요한 요청 파라미터 -> 사용자 정보에 접속할 수 있는 인증 키 같은 값 -> 토큰 -> 토큰 값은 계속 갱신되는 값임
                val accessToken:String? = NaverIdLoginSDK.getAccessToken()

                // 토큰 값 확인하기 -> 즉, 1회용 접속 키
                Log.i("token", accessToken.toString()) // nullable이니까 toString()

                // json 형태 -> {response : "", msg : "", response:{id: ""}}

                // retrofit을 이용해서 사용자의 정보를 가져오기
                val retrofit = RetrofitHelper.getRetrofitInstance("https://openapi.naver.com")
                val authorization = "Bearer ${accessToken}"
                retrofit.create(RetrofitApiService::class.java).getNaverUserInfo(authorization).enqueue( object : Callback<NidUserInfoResponse>{
                    override fun onResponse(
                        call: Call<NidUserInfoResponse>,
                        response: Response<NidUserInfoResponse>
                    ) {
                        Toast.makeText(this@LoginActivity, "회원정보 불러오기 설공", Toast.LENGTH_SHORT).show()
                        val userInfo: NidUserInfoResponse? = response.body()
                        val id = userInfo?.response?.id ?: ""
                        val email = userInfo?.response?.email ?: ""

                        G.userAccount = UserAccount(id, email)

                        startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                        finish()

                    }

                    override fun onFailure(call: Call<NidUserInfoResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "회원정보 불러오기 실패 ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })





            }
        })

    }
    fun clickedLoginKakao(){
        // 구글 로그인 화면(액티비티)를 실행하여 결과를 받아와서 사용자 정보 취득
        // 구글developer사이트에 google cloud에 api 서비스 -> credentials에 firebase때문에 이미 등록 되어있음.
        // firebase하고 연동하면서 Authentication에서 구글 로그인 on 해야함. 토큰을 주지 않아도 됨.


    }
    fun clickedLoginGoogle(){
        // 구글 로그인 옵션 객체 생성 - Builder 이용
        val signInOptions: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // 이메일 정보를 받을 수 있는 로그인 옵션값
            .build()

        // 구글 로그인 화면(Activity)가 이미 라이브러리에 만들어져 있음.
        // 그러니 그 액티비티를 실행시켜주는 Intent객체를 소환
        val intent = GoogleSignIn.getClient(this, signInOptions).signInIntent
//      로그인 결과를 받기 위한 액티비티 실행
        googleResultLauncher.launch(intent)


    }



    // 구글 로그인 화면 액티비티를 실행시키고 그 결과를 되돌려 받는 작업을 관리하는 객체 생성
    var googleResultLauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 로그인 결과를 가져온 객체를 먼저 소환
            val intent: Intent? = result?.data
            // 돌아온 인텐트 객체에게 구글 계정 정보를 빼오기
            val task : Task<GoogleSignInAccount> =  GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account: GoogleSignInAccount = task.result
            var id: String = account.id.toString()
            var email : String = account.email ?: "" // 혹시 null이면 "" 이메일 기본값은 빈 문자열

            G.userAccount = UserAccount(id, email)
            startActivity(Intent(this@LoginActivity,MainActivity::class.java))
            finish()


        }

//    val googleResultLauncher = ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult, object : ActivityResultCallback<ActivityResult>{
//        override fun onActivityResult(result: ActivityResult?) {
//            // 로그인 결과를 가져온 객체를 먼저 소환
//            val intent: Intent? = result?.data
//            // 돌아온 인텐트 객체에게 구글 계정 정보를 빼오기
//            val task : Task<GoogleSignInAccount> =  GoogleSignIn.getSignedInAccountFromIntent(intent)
//            val account: GoogleSignInAccount = task.result
//            var id: String = account.id.toString()
//            var email : String = account.email ? : "" // 혹시 null이면 "" 이메일 기본값은 빈 문자열
//
//            G.userAccount = UserAccount(id, email)
//            startActivity(Intent(this@LoginAcitivity ,MainActivity::class.java)
//            finish()
//
//        }
//    })


}