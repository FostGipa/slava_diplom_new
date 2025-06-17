package com.example.slava.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slava.R
import com.example.slava.models.Challenge
import androidx.core.graphics.toColorInt

class MyChallengesAdapter(
    private var challenges: List<Challenge>
) : RecyclerView.Adapter<MyChallengesAdapter.MyChallengeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_challenge, parent, false)
        return MyChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyChallengeViewHolder, position: Int) {
        holder.bind(challenges[position])
    }

    override fun getItemCount(): Int = challenges.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newChallenges: List<Challenge>) {
        challenges = newChallenges
        notifyDataSetChanged()
    }

    inner class MyChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.challengeNameTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.challengeStatusTextView)
        private val commentTextView: TextView = itemView.findViewById(R.id.moderatorCommentTextView)

        @SuppressLint("SetTextI18n")
        fun bind(challenge: Challenge) {
            nameTextView.text = challenge.name
            statusTextView.text = when (challenge.status) {
                "На модерации" -> "На модерации"
                "Активный" -> "Одобрен"
                "Отклонено" -> "Отклонён"
                else -> "Неизвестно"
            }

            // Цвет по статусу
            statusTextView.setTextColor(
                when (challenge.status) {
                    "Активный" -> "#4CAF50".toColorInt()
                    "Отклонено" -> "#F44336".toColorInt()
                    "На модерации" -> "#FF9800".toColorInt()
                    else -> Color.GRAY
                }
            )

            commentTextView.visibility = if (!challenge.comment.isNullOrBlank()) {
                commentTextView.text = "Комментарий: ${challenge.comment}"
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}
