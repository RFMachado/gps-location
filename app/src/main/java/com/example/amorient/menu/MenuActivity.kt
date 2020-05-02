package com.example.amorient.menu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.amorient.R
import com.example.amorient.create.CreateRouteActivity
import com.example.amorient.start.StartScreenActivity
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity: AppCompatActivity() {

    companion object {
        fun launchIntent(context: Context) = Intent(context, MenuActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        btnPlay.setOnClickListener {
            val intent = StartScreenActivity.launchIntent(this)
            startActivity(intent)

            finish()
        }

        btnExit.setOnClickListener {
            finish()
        }

        btnCreate.setOnClickListener {
            val intent = CreateRouteActivity.launchIntent(this)
            startActivity(intent)
        }
    }
}