package com.example.amorient.create

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.amorient.R
import com.example.amorient.model.CheckPoint
import com.example.amorient.util.extensions.inflate
import kotlinx.android.synthetic.main.check_point_view_holder.view.*

class CreateAdapter(
        private val items: MutableList<CheckPoint>,
        private val listener: (Int) -> Unit
): RecyclerView.Adapter<CreateAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder.itemView) {
        val item = items[position]

        txtTitle.text = item.description
        txtLat.text = "Lat: ${item.lat}"
        txtLng.text = "Lng: ${item.lng}"

        imgPoint.setImageURI(Uri.parse(item.imagePath))

        btnRemove.setOnClickListener {
            listener.invoke(holder.adapterPosition)
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflate(R.layout.check_point_view_holder))

    override fun getItemCount() = items.size
}