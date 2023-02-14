package com.kyjsoft.tp12quickplacebykakaosearchapi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.kyjsoft.tp12quickplacebykakaosearchapi.G
import com.kyjsoft.tp12quickplacebykakaosearchapi.R
import com.kyjsoft.tp12quickplacebykakaosearchapi.databinding.ActivityEmailLoginBinding
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.UserAccount

class EmailLoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityEmailLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

        binding.btnSignIn.setOnClickListener {
            clickSignIn()
        }


    }

    private fun clickSignIn(){
        var email = binding.etEmail.text.toString()
        var password = binding.etPassword.text.toString()

        //Firebase FireStore에서 이메일 로그인 여부 확인
        val db = FirebaseFirestore.getInstance()

        db.collection("emailUsers")
            .whereEqualTo("email",email)
            .whereEqualTo("passoword",password)
            .get().addOnSuccessListener {
                if(it.documents.size > 0){
                    // where조건에 맞는 데이터가 있다는 거임
                    // 로그인 성공
                    // 회원정보를 다른 액티비티에 사용할 가능성이 있으므로 전역변수(클래스이름만으로 사용가능한)에 저장
                    var id = it.documents[0].id // 랜덤한 식별자
                    G.userAccount = UserAccount(id, email)

                    val intent = Intent(this, MainActivity::class.java)

                    // 다른 액티비티로 넘어가면서 task에 있는 모든 액티비티를 제거하고 새로운 task로 시작
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // 스택이 다 날라가고
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 지금 있는걸 새로운 스택으로 남겨놔

                    startActivity(intent)


                }else {
                    // 로그인 실패
                    AlertDialog.Builder(this).setMessage("이메일 혹은 비밀번호가 일치하지 않습니다.").show()
                    binding.etEmail.requestFocus()
                    binding.etEmail.selectAll()
                }
            }.addOnFailureListener { Toast.makeText(this, "서버 오류 : ${it}", Toast.LENGTH_SHORT).show() }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}