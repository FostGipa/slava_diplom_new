package com.example.slava.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slava.models.Answer
import com.example.slava.R

class AnswerAdapter(
    private val answers: MutableList<Answer>,
    private val onAnswerClick: (Answer) -> Unit
) : RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION // Хранит индекс выбранного ответа

    class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val answerText: TextView = itemView.findViewById(R.id.answerText)
        val answerLayout : LinearLayout = itemView.findViewById(R.id.answerLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.test_answer_item, parent, false)
        return AnswerViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val answer = answers[position]
        holder.answerText.text = answer.text

        // Изменяем цвет фона в зависимости от выбранного состояния
        if (position == selectedPosition) {
            holder.answerLayout.setBackgroundResource(R.color.selected_answer_background) // Цвет для выбранного ответа
        } else {
            holder.answerLayout.setBackgroundResource(R.color.default_answer_background) // Цвет по умолчанию
        }

        // Обработка нажатия на ответ
        holder.itemView.setOnClickListener {
            // Уведомляем об изменении предыдущего и текущего выбора
            val previousPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousPosition) // Перерисовываем предыдущий выбор
            notifyItemChanged(selectedPosition) // Перерисовываем текущий выбор

            // Сообщаем о выборе через callback
            onAnswerClick(answer)
        }
    }

    override fun getItemCount(): Int = answers.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateAnswers(newAnswers: List<Answer>) {
        answers.clear()
        answers.addAll(newAnswers)
        selectedPosition = RecyclerView.NO_POSITION // Сбрасываем выбор при обновлении данных
        notifyDataSetChanged()
    }
}
