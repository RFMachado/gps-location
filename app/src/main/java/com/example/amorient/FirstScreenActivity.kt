package com.example.amorient

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_first_screen.*

class FirstScreenActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_screen)

        btnPlay.setOnClickListener {
            MapsActivity.launchIntent(this).also { intent ->
                startActivity(intent)
            }
        }

        btnExit.setOnClickListener {
            finish()
        }
    }
}