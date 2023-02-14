package com.kyjsoft.tp12quickplacebykakaosearchapi.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kyjsoft.tp12quickplacebykakaosearchapi.R
import com.kyjsoft.tp12quickplacebykakaosearchapi.activities.PlaceUrlActivity
import com.kyjsoft.tp12quickplacebykakaosearchapi.databinding.RecyclerItemListFragmentBinding
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.KaKaoSearchPlaceResponse
import com.kyjsoft.tp12quickplacebykakaosearchapi.model.Place

class PlaceListRecyclerAdapter(var documents: MutableList<Place>, val context : Context) : RecyclerView.Adapter<PlaceListRecyclerAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding : RecyclerItemListFragmentBinding by lazy {  RecyclerItemListFragmentBinding.bind(itemView) }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(context).inflate(R.layout.recycler_item_list_fragment, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val place = documents[position]

        holder.binding.tvPlaceName.text = place.place_name
        holder.binding.tvAddressName.text = if(place.road_address_name=="") place.address_name else place.road_address_name
        holder.binding.tvDistance.text = "${place.distance}m"

        // 아이템뷰를 클릭했을 때 장소에 대한 세부정보 웹사이트를 보여주는 화면으로 이동
        // 클릭한거 번호주는 인텐트 만들기
        holder.binding.root.setOnClickListener{
            val intent = Intent(context, PlaceUrlActivity::class.java)
            intent.putExtra("placeUrl", place.place_url)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = documents.size

}