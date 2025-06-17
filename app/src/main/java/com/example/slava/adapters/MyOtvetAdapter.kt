package com.example.slava.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slava.R
import androidx.core.graphics.toColorInt
import com.example.slava.models.UserTask

class MyOtvetAdapter(
    private var challenges: List<UserTask>
) : RecyclerView.Adapter<MyOtvetAdapter.MyOtvetAdapterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOtvetAdapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_challenge, parent, false)
        return MyOtvetAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyOtvetAdapterViewHolder, position: Int) {
        holder.bind(challenges[position])
    }

    override fun getItemCount(): Int = challenges.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newChallenges: List<UserTask>) {
        challenges = newChallenges
        notifyDataSetChanged()
    }

    inner class MyOtvetAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.challengeNameTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.challengeStatusTextView)
        private val commentTextView: TextView = itemView.findViewById(R.id.moderatorCommentTextView)

        @SuppressLint("SetTextI18n")
        fun bind(challenge: UserTask) {
            nameTextView.text = challenge.result
            statusTextView.text = when (challenge.status) {
                "unsorted" -> "На модерации"
                "applied" -> "Одобрен"
                "rejected" -> "Отклонён"
                else -> "Неизвестно"
            }

            // Цвет по статусу
            statusTextView.setTextColor(
                when (challenge.status) {
                    "applied" -> "#4CAF50".toColorInt()
                    "rejected" -> "#F44336".toColorInt()
                    "unsorted" -> "#FF9800".toColorInt()
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
