package com.kyjsoft.tp12quickplacebykakaosearchapi.activities

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.ListFragment
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import com.kyjsoft.tp12quickplacebykakaosearchapi.R
import com.kyjsoft.tp12quickplacebykakaosearchapi.databinding.ActivityMainBinding
import com.kyjsoft.tp12quickplacebykakaosearchapi.fragments.SearchListFragment
import com.kyjsoft.tp12quickplacebykakaosearchapi.fragments.SearchMapFragment
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.KaKaoSearchPlaceResponse
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.Place
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.PlaceMeta
import com.kyjsoft.tp12quickplacebykakaosearchapi.network.RetrofitApiService
import com.kyjsoft.tp12quickplacebykakaosearchapi.network.RetrofitHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // 카카오 지역 검색(로컬 API)
    // 1. 검색할 장소 이름(query)
    var searchQuery = "화장실" // 앱 초기 키워드 -> 내 주변 개방된 화장실 정보
    // 2. 현재 나의 위치를 기준으로 나와아하니까 내위치 정보 객체(위경도 보유)
    var myLocation : Location? = null // 처음에 없을 수도 있으니까 null로
    // 내 위치를 손쉽게 얻어 올 수 있는 구글꺼 fused API사용(라이브러리 play-services-location)
    val providerClient : FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    // 위치정보 동적 퍼미션 필요

    // 카카오 검색 결과 응답 객체 참조 변수(멤버변수)
    var searchPlaceResponse: KaKaoSearchPlaceResponse? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        setContentView(binding.root)

        // 툴바를 제목 줄로 대체 설정
        setSupportActionBar(binding.toolbar)

        // 처음 실행된 fragment를 동적으로 추가
        supportFragmentManager.beginTransaction().add(R.id.container_fragment, SearchListFragment()).commit() // commit을 안하면 rollback할 준비를 한대.
        // 여기까지만 해도 프래그먼트가 붙어 있는 채로 실행되어야함.

        // 탭레이아웃의 탭 버튼을 클릭했을 때 보여줄 fragment를 변경 -> 탭을 눌렀을 때 SearchList의 데이터를 가지고 있을 필요가 없기 때문
        binding.tabLayout.addOnTabSelectedListener( object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) { // 얘는 선택된 놈
                if(tab?.text=="LIST"){ // 내부적으로 ==을 equals로 바꿔줌
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment, SearchListFragment()).commit()
                }else if (tab?.text=="MAP"){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment, SearchMapFragment()).commit()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) { // 다른 걸 선택하면 애도 같이 선택되는 거야
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        // 소프트키보드 검색버튼 클릭했을 때
        binding.etSearch.setOnEditorActionListener { textView, i, keyEvent ->
            searchQuery = binding.etSearch.text.toString()
            searchPlaces()

            // 리턴 값이 있는 메소드임 그리고 return 키워드도 생략해야함 -> sam변환
            false //환소프트 키보드의 액션 버튼이 클릭되었을 때 여기서 모든 액션값을 소모하지 않겠다는 뜻으로 false를 리턴 안그러면 액티비티한테 이벤트가 안넘어감
        }

        // 단축 검색어 choice 버튼들에 클릭 이벤트 리스너 처리하기
        setChoiceButtonsListener()


        // 위치 동적 퍼미션
        val permissions: Array<String> = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION) // ACCESS_FINE_LOCATION 얘를 하면 coarse는 자동으로 들어감

        if( checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_DENIED){
            // 거부 되있으니까 퍼미션 요청해야지
            requestPermissions(permissions, 100)
        }else{
            // 승인 된 상태면 내 위치 값을 요구 할 거임
            requestMyLocation()
        }


    }







    // 퍼미션 요청 다이올로그의 선택이 되었을 때 자동 발동하는 콜백 메소드
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray // 허가 결과
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100 && grantResults[0]==PackageManager.PERMISSION_GRANTED ){
            requestMyLocation()
        }else{
            Toast.makeText(this, "위치정보 권한 없어서 검색기능 사용 불가", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestMyLocation(){
        // 위치 검색을 위한 기준이 되는 객체가 필요 -> LocationRequest(구글꺼)
        val request : LocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build() // 정확도나 배터리 같은 거 우선순위, 1초에 한번씩
        // 실시간 위치 정보 갱신 요청
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        providerClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper()) // ui작업을 해줄 스레드 비서 looper소환 -> 별도의 쓰레드를 만들지 않아도 계속 화면이 갱신 될수 가 있다는 거임
        // 퍼미션달라고 하는 데(썼잖아) oncreate() 밖이라 못 알아 들어서 Looper가 빨간색글씨



    }


//    위치 정보 갱신될 때마다 발동하는 Callback 객체
    private val locationCallback = object : LocationCallback(){ // 얘는 추상 메소드가 아니기때문에 필요한 메소드 골라쓰면 됨.
    override fun onLocationResult(p0: LocationResult) {
        super.onLocationResult(p0)

            myLocation = p0.lastLocation // 마지막 위치를 넣어주는 거임 -> 키워드 검색으로 한번만 전역변수에 들어가면됨 그래서 위치 업데이트를 종료해버려라
            providerClient.removeLocationUpdates(this) // 파라미터로 콜백달라고 했는데 스스로니까 this

        // 내 위치 정보있으니까 로컬 api가져와서 검색 시작하면 되는 거임
        searchPlaces()

        }
    }

    private fun searchPlaces(){
        // 검색에 필요한 요청변수들 (검색어, 내 위치 좌표)
        Toast.makeText(this, "${searchQuery} : ${myLocation?.latitude} , ${myLocation?.latitude}", Toast.LENGTH_SHORT).show()

        val retrofit = RetrofitHelper.getRetrofitInstance("dapi.kakao.com")
        retrofit.create(RetrofitApiService::class.java).searchPlaces(searchQuery,myLocation?.longitude.toString(), myLocation?.longitude.toString()).enqueue(object : Callback<KaKaoSearchPlaceResponse>{
            override fun onResponse(
                call: Call<KaKaoSearchPlaceResponse>,
                response: Response<KaKaoSearchPlaceResponse>
            ) {
                searchPlaceResponse = response.body()
                
                var meta :PlaceMeta? = searchPlaceResponse?.meta
                var documents:MutableList<Place>? = searchPlaceResponse?.documents
                
                AlertDialog.Builder(this@MainActivity).setMessage("${meta?.total_count} \n ${documents?.get(0)?.place_name}").show()

                // 무조건 검색이 완료되면 Lisr Fragment를 먼저 보여주기
                supportFragmentManager.beginTransaction().replace(R.id.container_fragment, SearchListFragment()).commit()

                // 무조건 탭버튼의 위치를 listfragment tab으로 변경하기
                binding.tabLayout.getTabAt(0)?.select()
            }

            override fun onFailure(call: Call<KaKaoSearchPlaceResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "서버오류가 있스빈다 \n 잠시 뒤에 다시 시도해주시기 바랍니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 단축 검색어 버튼에 리스너를 설정하는 작업 메소드
    private fun setChoiceButtonsListener(){
        binding.layoutChoice.choiceWc.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceMovie.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceEv.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceFood.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceGas.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceStore.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceChurch.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choicePark.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceSchool.setOnClickListener { clickChoice(it) }


    }

    var choiceID = R.id.choice_wc

    private fun clickChoice(view : View){

        // 이전에 선택되었던 뷰의 배경을 하얀 배경으로 바꿔야하잖아 -> 이전에 선택되었던 뷰의 배경을 하얀 배경으로 변경
        findViewById<ImageView>(choiceID).setBackgroundResource(R.drawable.bg_choice)

        // 선택한 단축 검색어 버튼의 배경 그림을 변경
        view.setBackgroundResource(R.drawable.bg_choice_selected)

        // 다음번 클릭 때 이전 선택 버튼의 id를 찾을 수 있도록
        choiceID = view.id

        // 선택한 뷰에 따라 검색어 값 변경
        when(view.id){
            R.id.choice_wc -> searchQuery="화장실"
            R.id.choice_ev -> searchQuery="전기차충전소"
            R.id.choice_church -> searchQuery="교회"
            R.id.choice_gas -> searchQuery="주유소"
            R.id.choice_food -> searchQuery="식당"
            R.id.choice_movie -> searchQuery="영화관"
            R.id.choice_park -> searchQuery="공원"
            R.id.choice_store -> searchQuery="편의점"
            R.id.choice_school -> searchQuery="학교"
        }

        // 새로운 검색을 요청하기
        searchPlaces()

        // 검색창에 검색어 글씨가 있다면 지우기
        binding.etSearch.text.clear()
        binding.etSearch.clearFocus() // 커서 날리기
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_main, menu) // 옵션 메뉴 붙여주기
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_help -> Toast.makeText(this, "help", Toast.LENGTH_SHORT).show()
            R.id.menu_logout -> Toast.makeText(this, "logout", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
}