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
import kotlinx.android.synthetic.main.activity_create.*


class CreateRouteActivity: AppCompatActivity() {

    private var imagePath: Uri? = null

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
        btnPhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        imgRoute.setOnClickListener {
            imagePath = null
            imgRoute.setImageURI(null)
        }
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