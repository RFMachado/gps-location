package com.example.amorient.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.amorient.R
import com.example.amorient.map.MapsActivity
import com.example.amorient.model.CheckPoint
import com.example.amorient.model.Route
import com.example.amorient.util.AmorientPreferences
import com.example.amorient.util.Consts
import com.example.amorient.util.extensions.toast
import kotlinx.android.synthetic.main.activity_create.*

class CreateRouteActivity: AppCompatActivity() {

    private var items = mutableListOf<CheckPoint>()
    private var imagePath: Uri? = null
    private var adapter: CreateAdapter? = null
    private val preferences = AmorientPreferences(this)

    private val regexLat =
            "^([+\\-])?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,6})?))\$".toRegex()

    private val regexLng =
            "^([+\\-])?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,6})?))\$".toRegex()

    companion object {
        fun launchIntent(context: Context): Intent {
            return Intent(context, CreateRouteActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        bindListener()
    }

    private fun bindListener() {
        checkSaveButtonEnable()

        adapter = CreateAdapter(items) { position ->
            items.removeAt(position)
            adapter?.notifyItemRemoved(position)

            checkSaveButtonEnable()
        }

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))

        btnPhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btnCreate.setOnClickListener {
            inserItemOnList()
        }

        btnSave.setOnClickListener {
            val routes = preferences.get<MutableList<Route>>(Consts.ROUTE_LIST) ?: mutableListOf()
            routes.add(Route(checkpoints = items))

            preferences.set(Consts.ROUTE_LIST, routes)

            toast("Percurso salvo com sucesso!", Toast.LENGTH_LONG)
            finish()
        }

        imgRoute.setOnClickListener {
            imagePath = null
            imgRoute.setImageURI(null)
        }
    }

    private fun inserItemOnList() {
        val name = edtname.text.toString()
        val lat = edtLat.text.toString()
        val lng = edtLng.text.toString()

        if (validateFields(name, lat, lng)) {
            items.add(
                    CheckPoint(
                            description = name,
                            lat = lat.toDouble(),
                            lng = lng.toDouble(),
                            imagePath = imagePath,
                            key = generateKey()
                    )
            )

            adapter?.notifyItemInserted(items.lastIndex)

            checkSaveButtonEnable()

            clearAllFields()
        }
    }

    private fun validateFields(name: String, lat: String, lng: String): Boolean {
        if (name.isNotBlank() && lat.isNotBlank() && lng.isNotBlank() && imagePath != null) {
            return isValidCoordenates(lat, lng)
        } else {
            toast("Preencha todos os campos")
            return false
        }
    }

    private fun isValidCoordenates(lat: String, lng: String): Boolean {
        val isValid = regexLat.matches(lat) && regexLng.matches(lng)

        if(!isValid)
            toast("Verifique as coordenadas")

        return isValid
    }


    private fun checkSaveButtonEnable() {
        btnSave.isEnabled = items.size >= 5
    }

    private fun clearAllFields() {
        edtname.text?.clear()
        edtLat.text?.clear()
        edtLng.text?.clear()
        imagePath = null
        imgRoute.setImageURI(null)
    }

    private fun generateKey(): Int {
        return if (items.size == 0)
            1
        else
            items[items.lastIndex].key + 1
    }

    private fun dispatchTakePictureIntent() {
        val photoPickerIntent = Intent(ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(photoPickerIntent, MapsActivity.REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MapsActivity.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            imagePath = data?.data
            imgRoute.setImageURI(imagePath)
        }
    }

}