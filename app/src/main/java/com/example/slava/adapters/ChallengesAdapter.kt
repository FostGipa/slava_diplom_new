package com.example.slava.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slava.R
import com.example.slava.models.Challenge
import com.example.slava.utils.ChallengeClickListener

class ChallengesAdapter(
    private val mList: MutableList<Challenge>,
    private val context: Context,
    private val listener: ChallengeClickListener
) : RecyclerView.Adapter<ChallengesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.challenge_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val challenge = mList[position]
        holder.nameTextView.text = challenge.name
        holder.categoryTextView.text = challenge.category?.name
        holder.categoryImageView.setImageResource(R.drawable.ic_read)

        // Устанавливаем обработчик нажатий
        holder.button.setOnClickListener {
            listener.onChallengeClick(challenge)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val categoryImageView: ImageView = itemView.findViewById(R.id.categoryImageView)
        val button : Button = itemView.findViewById(R.id.moreButton)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newChallenges: MutableList<Challenge>) {
        mList.clear()
        mList.addAll(newChallenges)
        notifyDataSetChanged()
    }
}