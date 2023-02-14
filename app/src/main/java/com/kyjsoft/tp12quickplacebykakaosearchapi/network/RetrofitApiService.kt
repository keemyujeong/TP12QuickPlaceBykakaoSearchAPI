package com.kyjsoft.tp12quickplacebykakaosearchapi.network

import com.kyjsoft.tp12quickplacebykakaosearchapi.model.KaKaoSearchPlaceResponse
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.NidUserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitApiService {
    @GET("/v1/nid/me")
    fun getNaverUserInfo(@Header("Authorization") authorization : String ): Call<NidUserInfoResponse>

    @Headers("Authorization: KakaoAK 60f91ea60305a572d7d3bee82209b4d3")
    @GET("/v2/local/search/address.json")
    fun searchPlaces(@Query("query") query:String, @Query("x") longitude:String, @Query("y") latitude:String) : Call<KaKaoSearchPlaceResponse>

}