package com.example.slava.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.slava.R
import com.example.slava.adapters.ChatAdapter
import com.example.slava.databinding.ActivityChatBinding
import com.example.slava.models.Message
import com.example.slava.utils.SharedPreferenceHelper
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val supabaseClient = SupabaseClient()
    private lateinit var binding: ActivityChatBinding
    private var currentUserId = 0
    private var chatId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.attachButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // Любой тип файлов
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*", "application/pdf"))
            startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
        }

        val sharedPref = SharedPreferenceHelper(this)
        currentUserId = sharedPref.getStringData("userId")?.toIntOrNull() ?: 0

        val recyclerView: RecyclerView = findViewById(R.id.messagesRecyclerView)
        chatAdapter = ChatAdapter(mutableListOf(), currentUserId = currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        chatId = intent.getIntExtra("chatId", 1)
        if (chatId == -1) {
            finish()
            return
        }

        lifecycleScope.launch {
            supabaseClient.listenForNewMessages(chatId, chatAdapter)
        }

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                lifecycleScope.launch {
                    supabaseClient.sendMessage(
                        Message(
                            chat_id = chatId,
                            type = "text",
                            fileUri = "",
                            sender_id = currentUserId,
                            text = messageText,
                        )
                    )
                    binding.messageEditText.text.clear()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_FILE = 100
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = getFileName(uri) ?: "file_${System.currentTimeMillis()}"
                val uniqueFileName = "${System.currentTimeMillis()}_$fileName"

                lifecycleScope.launch {
                    val result = supabaseClient.uploadChatMedia(uri, uniqueFileName)
                    result.onSuccess { fileUrl ->
                        if (fileUrl.isNotEmpty()) { // Проверяем, что загрузка удалась
                            val mimeType = contentResolver.getType(uri)
                            Log.d("123", mimeType.toString())

                            val message = when {
                                mimeType?.startsWith("image/") == true -> Message(
                                    chat_id = chatId,
                                    type = "image",
                                    fileUri = fileUrl,
                                    sender_id = currentUserId,
                                    text = "")
                                mimeType?.startsWith("video/") == true -> Message(
                                    chat_id = chatId,
                                    type = "video",
                                    fileUri = fileUrl,
                                    sender_id = currentUserId,
                                    text = "")
                                else -> Message(
                                    chat_id = chatId,
                                    type = "file",
                                    fileUri = fileUrl,
                                    sender_id = currentUserId,
                                    text = uniqueFileName)
                            }

                            supabaseClient.sendMessage(message)
//                            chatAdapter.addMessage(message)
                        } else {
                            Log.e("UploadError", "Файл не загрузился: пустой URL")
                        }
                    }.onFailure {
                        Log.e("UploadError", "Ошибка загрузки: ${it.message}")
                    }
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return result ?: uri.lastPathSegment
    }
}
