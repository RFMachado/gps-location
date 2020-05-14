package com.example.amorient.result

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.amorient.R
import com.example.amorient.model.TeamResult
import com.example.amorient.util.AmorientPreferences
import com.example.amorient.util.Consts
import kotlinx.android.synthetic.main.activity_results.*

class ResultsActivity : AppCompatActivity() {

    companion object {
        fun launchIntent(context: Context) = Intent(context, ResultsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        setRecyclerView()
    }

    private fun setRecyclerView() = with(recyclerView) {
        val preferences = AmorientPreferences(context)
        val teams =
                preferences.get<MutableList<TeamResult>>(Consts.TEAMS_RESULT_LIST) ?: mutableListOf()

        layoutEmpty.visibility = if (teams.isEmpty()) View.VISIBLE else View.GONE

        adapter = ResultTeamsAdapter(teams) { message ->
            share(message)
        }
    }

    private fun share(message: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        startActivity(Intent.createChooser(shareIntent, "Compartilhar"))
    }
}