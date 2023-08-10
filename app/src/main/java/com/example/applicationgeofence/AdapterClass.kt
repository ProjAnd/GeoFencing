package com.example.applicationgeofence

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class AdapterClass(options: FirebaseRecyclerOptions<UserInfo>) : FirebaseRecyclerAdapter<UserInfo, AdapterClass.ViewHolder>(
    options
) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var email: TextView
        var lat: TextView
        var long: TextView

        init {
            email = itemView.findViewById(R.id.tvEmail)
            lat =  itemView.findViewById(R.id.tvlat)
            long = itemView.findViewById(R.id.tvlong)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: UserInfo) {
            holder.email.text ="User: ${model.useremail}"
            holder.lat.text = "Latitude : ${model.latitude}"
            holder.long.text = "Longitude: ${model.longitude}"
    }

}