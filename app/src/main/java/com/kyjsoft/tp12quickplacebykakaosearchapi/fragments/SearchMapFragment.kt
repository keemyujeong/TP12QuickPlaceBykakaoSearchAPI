package com.kyjsoft.tp12quickplacebykakaosearchapi.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kyjsoft.tp12quickplacebykakaosearchapi.activities.MainActivity
import com.kyjsoft.tp12quickplacebykakaosearchapi.activities.PlaceUrlActivity
import com.kyjsoft.tp12quickplacebykakaosearchapi.databinding.FragmentSearchMapBinding
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.Place
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class SearchMapFragment : Fragment() {

    val binding : FragmentSearchMapBinding by lazy { FragmentSearchMapBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    val mapview : MapView by lazy { MapView(context) }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // mapview를 viewgroup에 추가하여 화면에 배치하도록
        binding.containerMapview.addView(mapview)

        // 마커나 말풍선을 클릭했을 때 반응하는 거 -> 반드시 마커 설정보다 mapview에 설정해놓아야 동작함
        mapview.setPOIItemEventListener(markerEventListener)

        // 지도 관련 설정들(지도 위치, 마커 추가 등)
        setMapAndMarkers()


    }

    private fun setMapAndMarkers(){
        // 맵의 중심점을 내 위치로 변경
        // 현재 내 위치 정보는 Mainactivity의 멤버 변수로 저장되어 있음.
        var latitude : Double = (activity as MainActivity).myLocation?.latitude ?: 37.566805
        var longitude : Double = (activity as MainActivity).myLocation?.longitude ?: 126.978417

        var myMapPoint : MapPoint = MapPoint.mapPointWithGeoCoord(latitude,longitude)
        mapview.setMapCenterPointAndZoomLevel(myMapPoint, 5, true) // 숫자가 가까울수록 줌 인
        mapview.zoomIn(true) // 줌할 때 +모양
        mapview.zoomOut(true)

        // 내 위치에다가 마커 표시하기
        var marker = MapPOIItem()
        marker.apply {
            itemName = "여기"
            mapPoint = myMapPoint
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.YellowPin
        }

        mapview.addPOIItem(marker)

        // 검색결과 장소들의 마커들을 추가하기
        val document : MutableList<Place>? = (activity as MainActivity).searchPlaceResponse?.documents
        document?.forEach {
            val point = MapPoint.mapPointWithGeoCoord(it.y.toDouble(), it.x.toDouble())

            // 마커 객체를 만들어서 기본 설정하기
            val marker = MapPOIItem().apply {
                itemName = it.place_name
                mapPoint = point
                markerType = MapPOIItem.MarkerType.RedPin
                selectedMarkerType = MapPOIItem.MarkerType.BluePin

                // 마커에 표시되지는 않지만 저장하고 싶은 사용자 정보가 있다면
                userObject = it.place_name
            }

            mapview.addPOIItem(marker)

        }

    }

    // 마커나 말풍선(콜아웃)이 클릭되면 이벤트에 반응하는 리스너
    private val markerEventListener: MapView.POIItemEventListener = object : MapView.POIItemEventListener{
        override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {

        }

        override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {

        }

        override fun onCalloutBalloonOfPOIItemTouched(
            p0: MapView?,
            p1: MapPOIItem?,
            p2: MapPOIItem.CalloutBalloonButtonType?
        ) {
            // 말풍선을 클릭했을 때 반응하는 콜백 메소드
            // 두번째 파라미터가 클릭한 객체의 marker객체
            if(p1?.userObject==null) return

            var place = p1?.userObject as Place

            // 장소의 상세 정보 url을 가지고 상세정보 웹페이지를 보여주는 화면으로 전환
            val intent: Intent = Intent(context, PlaceUrlActivity::class.java)
            intent.putExtra("placeUrl", place.place_url)
            startActivity(intent)

        }

        override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {

        }

    }
}