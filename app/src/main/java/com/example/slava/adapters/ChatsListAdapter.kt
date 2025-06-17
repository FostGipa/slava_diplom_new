package com.example.slava.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.example.slava.R
import com.example.slava.models.Chat
import kotlinx.coroutines.launch

class ChatListAdapter(
    private var chatList: MutableList<Chat>,
    private val currentUserId: Int,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val getUserName: suspend (Int) -> String?,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.chatUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chats, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        lifecycleScope.launch {
            val userName = getUserName(chat.getOtherUserId(currentUserId)) ?: "Неизвестный"
            holder.nameTextView.text = userName
        }

        holder.itemView.setOnClickListener { onChatClick(chat) }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newChatList: List<Chat>) {
        chatList = newChatList.toMutableList() // Создаем новый список, чтобы избежать ошибок
        notifyDataSetChanged()
    }

}
