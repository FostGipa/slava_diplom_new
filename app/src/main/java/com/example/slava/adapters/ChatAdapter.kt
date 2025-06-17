package com.example.slava.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.slava.R
import com.example.slava.models.Message
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.core.net.toUri

class ChatAdapter(
    private var messages: MutableList<Message>,
    private val currentUserId: Int
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender_id == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == TYPE_SENT) {
            R.layout.item_message_sent
        } else {
            R.layout.item_message_received
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.messageContent.visibility = View.GONE
        holder.messageImage.visibility = View.GONE
        holder.messageVideo.visibility = View.GONE
        holder.fileContainer.visibility = View.GONE
        holder.messageTime.text = formatDateTime(message.created_at.toString())

        when (message.type) {
            "text" -> {
                holder.messageContent.visibility = View.VISIBLE
                holder.messageContent.text = message.text
            }
            "image" -> {
                holder.messageImage.visibility = View.VISIBLE
                Glide.with(holder.messageImage.context)
                    .load(message.fileUri)
                    .into(holder.messageImage)
            }
            "video" -> {
                holder.messageVideo.visibility = View.VISIBLE
                holder.messageVideo.setVideoURI(message.fileUri?.toUri())
                holder.messageVideo.setOnPreparedListener { it.start() }
            }
            "file" -> {
                holder.fileContainer.visibility = View.VISIBLE
                holder.fileName.text = message.text
                holder.fileContainer.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, message.fileUri?.toUri())
                    it.context.startActivity(intent)
                }
            }
        }
    }

    fun getMessages(): List<Message> {
        return messages
    }

    override fun getItemCount(): Int = messages.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageContent: TextView = itemView.findViewById(R.id.messageContent)
        val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        val messageImage: ImageView = itemView.findViewById(R.id.messageImage)
        val messageVideo: VideoView = itemView.findViewById(R.id.messageVideo)
        val fileContainer: LinearLayout = itemView.findViewById(R.id.fileContainer)
        val fileName: TextView = itemView.findViewById(R.id.fileName)
    }

    private fun formatDateTime(dateTime: String): String {
        return try {
            val parsedDate = OffsetDateTime.parse(dateTime) // Разбираем дату
            val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Europe/Moscow"))
            parsedDate.format(formatter)
        } catch (_: Exception) {
            dateTime
        }
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
