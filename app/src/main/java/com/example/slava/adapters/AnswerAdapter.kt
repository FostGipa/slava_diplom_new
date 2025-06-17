package com.example.slava.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.slava.models.Answer
import com.example.slava.R

class AnswerAdapter(
    private val answers: MutableList<Answer>,
    private val onAnswerClick: (Answer) -> Unit,
) : RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private var isAnswerLocked = false

    class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val answerLetter: TextView = itemView.findViewById(R.id.answerLetter)
        val answerText: TextView = itemView.findViewById(R.id.answerText)
        val answerLayout : CardView = itemView.findViewById(R.id.answerLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.test_answer_item, parent, false)
        return AnswerViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: AnswerViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val answer = answers[position]
        val letters = listOf("A", "B", "C", "D")
        holder.answerLetter.text = letters.getOrElse(position) { "" }
        holder.answerText.text = answer.text

        // Устанавливаем цвет карточки
        if (selectedPosition == position) {
            if (answer.isCorrect) {
                holder.answerLayout.setCardBackgroundColor(holder.itemView.context.getColor(R.color.green))
            } else {
                holder.answerLayout.setCardBackgroundColor(holder.itemView.context.getColor(R.color.red))
            }
        } else {
            holder.answerLayout.setCardBackgroundColor(holder.itemView.context.getColor(R.color.light_gray))
        }

        // Только один клик разрешен
        holder.itemView.setOnClickListener {
            if (!isAnswerLocked) {
                isAnswerLocked = true
                selectedPosition = position
                notifyDataSetChanged()
                onAnswerClick(answer)
            }
        }
    }

    override fun getItemCount(): Int = answers.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateAnswers(newAnswers: List<Answer>) {
        answers.clear()
        answers.addAll(newAnswers)
        selectedPosition = RecyclerView.NO_POSITION
        isAnswerLocked = false // разрешаем выбор снова
        notifyDataSetChanged()
    }
}
