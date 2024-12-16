package com.example.slava.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slava.models.Challenge
import com.example.slava.R

class ActiveChallengeAdapter(var mList : MutableList<Challenge>, private val onChallengeClick: (Challenge) -> Unit) : RecyclerView.Adapter<ActiveChallengeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.active_challenge_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = mList[position].name
        if (mList[position].id_category == 1) {
            holder.category.text = "Чтение"
        }
        holder.itemView.setOnClickListener{
            onChallengeClick(mList[position])
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val name : TextView = itemView.findViewById<TextView>(R.id.challengeTitle)
        val category : TextView = itemView.findViewById<TextView>(R.id.challengeCategory)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newChallenges: MutableList<Challenge>) {
        mList = newChallenges
        notifyDataSetChanged()
    }
}