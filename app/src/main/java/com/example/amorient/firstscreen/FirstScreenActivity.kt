package com.example.amorient.firstscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.amorient.R
import com.example.amorient.start.StartScreenActivity
import kotlinx.android.synthetic.main.activity_first_screen.*

class FirstScreenActivity: AppCompatActivity() {

    companion object {
        fun launchIntent(context: Context) = Intent(context, FirstScreenActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_screen)

        btnPlay.setOnClickListener {
            val intent = StartScreenActivity.launchIntent(this)
            startActivity(intent)

            finish()
        }

        btnExit.setOnClickListener {
            finish()
        }
    }
}