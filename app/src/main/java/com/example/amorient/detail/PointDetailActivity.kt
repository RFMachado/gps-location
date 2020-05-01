package com.example.amorient.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.amorient.R
import com.example.amorient.model.CheckPoint
import kotlinx.android.synthetic.main.activity_detail.*

class PointDetailActivity: AppCompatActivity() {

    private val checkPoint by lazy {
        intent.getParcelableExtra(EXTRA_POINT) as? CheckPoint
    }

    companion object {
        private const val EXTRA_POINT = "recommendData"

        fun launchIntent(context: Context, checkPoint: CheckPoint): Intent {
            return Intent(context, PointDetailActivity::class.java).apply {
                putExtra(EXTRA_POINT, checkPoint)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        checkPoint?.let {
            if (it.image == null) {
                imgPoint.setImageURI(it.imagePath)
            } else {
                imgPoint.setImageResource(it.image)
            }

            txtTitle.text = it.description
            txtDistance.text = it.distance
        }
    }

}