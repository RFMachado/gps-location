package com.example.amorient.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.amorient.R
import com.example.amorient.map.MapsActivity
import com.example.amorient.model.CheckPoint
import com.example.amorient.util.toast
import kotlinx.android.synthetic.main.activity_create.*


class CreateRouteActivity: AppCompatActivity() {

    private var adapter: CreateAdapter? = null
    private var imagePath: Uri? = null
    private var items = mutableListOf<CheckPoint>()

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
        adapter = CreateAdapter(items) { position ->
            items.removeAt(position)
            adapter?.notifyItemRemoved(position)
        }

        recyclerView.adapter = adapter

        btnPhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btnCreate.setOnClickListener {
            inserItemOnList()
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

        if (name.isNotBlank() && lat.isNotBlank() && lng.isNotBlank() && imagePath != null) {
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

            clearAllFields()
        } else {
            toast("Preencha todos os campos")
        }
    }

    private fun clearAllFields() {
        edtname.text?.clear()
        edtLat.text?.clear()
        edtLng.text?.clear()
        imagePath = null
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