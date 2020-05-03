package com.example.amorient.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.amorient.map.MapsActivity
import com.example.amorient.R
import kotlinx.android.synthetic.main.activity_start_screen.*

class StartScreenActivity: AppCompatActivity() {

    companion object {
        fun launchIntent(context: Context) = Intent(context, StartScreenActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)

        checkIndividual.setOnClickListener {
            checkEquipe.isChecked = false
        }

        checkEquipe.setOnClickListener {
            checkIndividual.isChecked = false
        }

        btnStart.setOnClickListener {
            val intent = MapsActivity.launchIntent(this)
            startActivity(intent)

            finish()
        }
    }
}