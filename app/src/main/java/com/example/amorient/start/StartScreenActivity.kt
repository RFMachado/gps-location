package com.example.amorient.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.amorient.map.MapsActivity
import com.example.amorient.R
import com.example.amorient.model.Route
import com.example.amorient.util.AmorientPreferences
import com.example.amorient.util.Consts
import com.example.amorient.util.extensions.toast
import kotlinx.android.synthetic.main.activity_start_screen.*

class StartScreenActivity: AppCompatActivity() {

    private var selectedRoute: Route? = null
    lateinit var routes: MutableList<Route>
    private var lastSelectedPosition: Int? = null

    companion object {
        fun launchIntent(context: Context) = Intent(context, StartScreenActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)

        bindListener()
    }

    private fun bindListener() {
        val preferences = AmorientPreferences(this)
       routes = preferences.get<MutableList<Route>>(Consts.ROUTE_LIST)!!

        recyclerView.adapter = RoutesAdapter(routes) { position ->
            lastSelectedPosition?.let { lastPosition ->
                routes[lastPosition].isChecked = false
                recyclerView.adapter?.notifyItemChanged(lastPosition)
            }

            routes[position].isChecked = true
            recyclerView.adapter?.notifyItemChanged(position)

            selectedRoute = routes[position]
            lastSelectedPosition = position

        }

        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))


        checkIndividual.setOnClickListener {
            checkEquipe.isChecked = false
        }

        checkEquipe.setOnClickListener {
            checkIndividual.isChecked = false
        }

        btnStart.setOnClickListener {
            if (lastSelectedPosition != null) {
                val intent = MapsActivity.launchIntent(this, lastSelectedPosition!!)
                startActivity(intent)

                finish()
            } else {
                toast("Seleciona um percurso para continuar")
            }
        }
    }
}