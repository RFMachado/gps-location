package com.example.amorient.result

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.amorient.R
import com.example.amorient.model.TeamResult
import com.example.amorient.util.extensions.inflate
import kotlinx.android.synthetic.main.team_results_view_holder.view.*

class ResultTeamsAdapter(
        private val items: MutableList<TeamResult>,
        private val listener: (String) -> Unit
): RecyclerView.Adapter<ResultTeamsAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = with(holder.itemView) {
        val team = items[position]
        val teamName = "Nome da Equipe: ${team.teamName}"
        val teamMode = "Modo de jogo: ${team.mode}"
        val teamTime = "Tempo de percurso: ${team.duration}"

        txtTeamName.text = teamName
        txtMode.text = teamMode
        txtDuration.text = teamTime

        btnShare.setOnClickListener {
            val message = "$teamName $teamMode $teamTime"
            listener.invoke(message)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflate(R.layout.team_results_view_holder))

    override fun getItemCount() = items.size
}