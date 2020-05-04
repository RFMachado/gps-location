package com.example.amorient.menu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.amorient.R
import com.example.amorient.create.CreateRouteActivity
import com.example.amorient.map.MapsActivity
import com.example.amorient.model.Route
import com.example.amorient.start.StartScreenActivity
import com.example.amorient.util.AmorientPreferences
import com.example.amorient.util.Consts
import com.example.amorient.util.Utils
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity: AppCompatActivity() {

    companion object {
        fun launchIntent(context: Context) = Intent(context, MenuActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        bindListener()
        checkNeedAddDefaultList()
    }

    private fun bindListener() {
        btnPlay.setOnClickListener {
            if (checkPermissionExernalStorage()) {
                val intent = StartScreenActivity.launchIntent(this)
                startActivity(intent)

                finish()
            }
        }

        btnExit.setOnClickListener {
            finish()
        }

        btnCreate.setOnClickListener {
            val intent = CreateRouteActivity.launchIntent(this)
            startActivity(intent)
        }
    }

    private fun checkNeedAddDefaultList() {
        val preferences = AmorientPreferences(this)
        val routes = preferences.get<MutableList<Route>>(Consts.ROUTE_LIST) ?: mutableListOf()

        if(routes.isEmpty()) {
            val route = Route(name = "CAIC", checkpoints = Utils.getPoints())

            routes.add(route)
            preferences.set(Consts.ROUTE_LIST, routes)
        }
    }

    fun checkPermissionExernalStorage(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialogPermission("External storage", Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    ActivityCompat
                            .requestPermissions(
                                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    MapsActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                }

                return false
            } else {
                return true
            }
        }
        return true
    }

    private fun showDialogPermission(msg: String, permission: String) {
        val alertBuilder = AlertDialog.Builder(this).apply {
            setCancelable(true)
            setTitle("Permissão necessária")
            setMessage("$msg permissão é necessária")
        }

        alertBuilder.setPositiveButton(android.R.string.yes) { _, _ ->
            ActivityCompat.requestPermissions(this@MenuActivity, arrayOf(permission),
                    MapsActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
        }

        alertBuilder.create().show()
    }
}