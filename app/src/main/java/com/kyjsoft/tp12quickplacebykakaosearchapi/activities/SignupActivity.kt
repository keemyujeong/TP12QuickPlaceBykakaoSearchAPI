package com.kyjsoft.tp12quickplacebykakaosearchapi.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.kyjsoft.tp12quickplacebykakaosearchapi.R
import com.kyjsoft.tp12quickplacebykakaosearchapi.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    val binding by lazy { ActivitySignupBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

        binding.btnSignup.setOnClickListener { clickSignUp() }


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignUp(){
        // Firebase Firestore DB에 사용자 정보 저장하기[앱과 firebase 플랫폼 연동]

        var email = binding.etEmail.text.toString()
//        if 로 email.match(정규 표현식) 하셈
        var password = binding.etPassword.text.toString()
        var passwordcomfirm = binding.etPasswordConfirm.text.toString()


        // password가 올바른지 확인하기 (코틀린은 ==은 값비교)
        if(password != passwordcomfirm){
            AlertDialog.Builder(this).setMessage("비밀번호가 같지 않습니다. 다시 확인하고 입력해주세요.").show()
            binding.etPasswordConfirm.selectAll() // 써있는 글씨들 모두 선택 상태로 하여서 손쉽게 새로 입력이 가능해짐
            return
        }

        // Firestore에 DB에 저장하기위해
        var db = FirebaseFirestore.getInstance()

        // 이미 가입한 적이 있는 이메일 인지 검사를 먼저 하라
        // 필드 값 중에 'email'의 값이 Editext에 입력한 email과 같은지 찾아달라고 요청
        db.collection("emailUsers").whereEqualTo("email", email).get().addOnSuccessListener {
            // 같은 값을 가진 document가 있다면 -> 기존에 같은 emeil이 있다는 거임
            if(it.documents.size>0){
                AlertDialog.Builder(this).setMessage("중복된 이메일 있으니 다시 확인 후 입력 바랍니다.").show()
                binding.etEmail.requestFocus() // 포커스가 다른 곳에 있을 수도 있으니
                binding.etEmail.selectAll() // 포커스를 이메일로 가져와
            }else{

                // 저장할 데이터들을 하나로 묶기위해 HashMap
                var user: MutableMap<String, String> = mutableMapOf()
                user.put("email", email)
                user.put("password", password)

                // DB안에 COLLECTION 명은 "emailUsers"로 지정[RDBMS의 테이블 같은 역할]
                // 별도의 document를 주지 않으면 random값으로 설정됨. -> 이 랜덤값을 회원번호의 역할로 사용함
                // 얘네는 비동기이기 때문에 잘되었는지 확인하는 것이 어려움
                db.collection("emailUsers").add(user).addOnSuccessListener {

                    AlertDialog.Builder(this)
                        .setMessage("회원가입이 완료되었습니다. \n 환영합니다 고객님")
                        .setPositiveButton("확인", object : DialogInterface.OnClickListener{
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                finish()
                            }
                        }).show()
                }.addOnFailureListener {
                    AlertDialog.Builder(this).setMessage("회원가입 오류실패 :  ${it}").show()
                }

            }
        }








    }
}