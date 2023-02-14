package com.kyjsoft.tp12quickplacebykakaosearchapi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kyjsoft.tp12quickplacebykakaosearchapi.activities.MainActivity
import com.kyjsoft.tp12quickplacebykakaosearchapi.adapters.PlaceListRecyclerAdapter
import com.kyjsoft.tp12quickplacebykakaosearchapi.databinding.FragmentSearchListBinding
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.KaKaoSearchPlaceResponse

class SearchListFragment : Fragment() {

    val binding : FragmentSearchListBinding by lazy { FragmentSearchListBinding.inflate(layoutInflater) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // mainActivity를 참조하기
        val ma : MainActivity = activity as MainActivity

        // 아직 메인 액티비티에서 파싱작업이 완료되지 않았을 수도 있음 -> 데이터가 없을 수도 있다는 거임
        if( ma.searchPlaceResponse == null) return

        binding.recyclerView.adapter = PlaceListRecyclerAdapter(ma.searchPlaceResponse!!.documents, requireContext())

        
    }

}