package com.example.amorient.start

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.amorient.R
import com.example.amorient.model.Route
import com.example.amorient.util.extensions.inflate
import kotlinx.android.synthetic.main.route_view_holder.view.*

class RoutesAdapter(
        private val items: MutableList<Route>,
        private val listener: (Int) -> Unit
): RecyclerView.Adapter<RoutesAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder.itemView) {
        val route = items[position]

        txtName.text = route.name
        btnRadio.isChecked = route.isChecked

        btnRadio.setOnClickListener {
            listener.invoke(holder.adapterPosition)
        }

        setOnClickListener {
            btnRadio.performClick()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflate(R.layout.route_view_holder))

    override fun getItemCount() = items.size
}