package com.example.slava.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slava.models.Rating
import com.example.slava.R
import com.example.slava.models.Challenge
import com.example.slava.models.RatingItem
import com.example.slava.models.UserChallenge
import com.example.slava.utils.SupabaseClient

class RatingAdapter(var mList: MutableList<UserChallenge>) : RecyclerView.Adapter<RatingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RatingAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rating_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RatingAdapter.ViewHolder, position: Int) {
        val rating = mList[position]
        holder.scoreTextView.text = "Количество очков: ${rating.pts}"
        holder.nameTextView.text = rating.user?.name
        holder.placeTextView.text = "${position + 1}"

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeTextView : TextView = itemView.findViewById(R.id.placeTextView)
        val nameTextView : TextView = itemView.findViewById(R.id.nameTextView)
        val scoreTextView : TextView = itemView.findViewById(R.id.scoreTextView)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRating: MutableList<UserChallenge>) {
        mList = newRating
        notifyDataSetChanged()
    }
}