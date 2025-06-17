package com.example.slava.fragments

import com.example.slava.adapters.ChatListAdapter
import android.content.Intent
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.slava.R
import com.example.slava.activities.ChatActivity
import com.example.slava.databinding.FragmentChatBinding
import com.example.slava.models.Chat
import com.example.slava.models.User
import com.example.slava.utils.SharedPreferenceHelper
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val supabaseClient: SupabaseClient = SupabaseClient()
    private lateinit var chatAdapter: ChatListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private var chatList = mutableListOf<Chat>()
    private var searchResults = mutableListOf<User>()
    private var currentUserId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root

        recyclerView = view.findViewById(R.id.chatsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = SharedPreferenceHelper(requireContext())
        currentUserId = sharedPref.getStringData("userId")?.toIntOrNull() ?: 0

        searchView = view.findViewById(R.id.searchView)

        loadChats()
        setupSearch()

        return view
    }

    private fun loadChats() {
        binding.linearLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val newChatList = supabaseClient.getUserChats(currentUserId).toMutableList()

            if (::chatAdapter.isInitialized) {
                chatAdapter.updateList(newChatList)
            } else {
                chatAdapter = ChatListAdapter(
                    newChatList,
                    currentUserId,
                    lifecycleScope,
                    { userId -> supabaseClient.getUserName(userId)?.name }
                ) { chat ->
                    val intent = Intent(requireContext(), ChatActivity::class.java)
                    intent.putExtra("chatId", chat.id)
                    startActivity(intent)
                }
                recyclerView.adapter = chatAdapter
            }
            binding.progressBar.visibility = View.GONE
            binding.linearLayout.visibility = View.VISIBLE
        }
    }


    private lateinit var suggestionsAdapter: SimpleCursorAdapter

    private fun setupSearch() {
        val from = arrayOf("name") // Колонка, которая будет отображаться
        val to = intArrayOf(android.R.id.text1) // ID элемента в layout
        suggestionsAdapter = SimpleCursorAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            null,
            from,
            to,
            0
        )

        searchView.suggestionsAdapter = suggestionsAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterChats(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterChats(newText)
                return true
            }
        })

        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = suggestionsAdapter.getItem(position) as MatrixCursor
                val userId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
                createChatIfNotExists(userId)
                return true
            }
        })
    }

    private fun createChatIfNotExists(userId: Int) {
        lifecycleScope.launch {
            val existingChat = chatList.find { (it.user1_id == userId && it.user2_id == currentUserId) ||
                    (it.user2_id == userId && it.user1_id == currentUserId) }
            if (existingChat == null) {
                val newChat = supabaseClient.createChat(currentUserId, userId)
                if (newChat != null) {
                    loadChats()
                    val intent = Intent(requireContext(), ChatActivity::class.java)
                    intent.putExtra("chatId", newChat.id)
                    startActivity(intent)
                }
            } else {
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("chatId", existingChat.id)
                startActivity(intent)
            }
        }
    }

    private fun filterChats(query: String?) {
        lifecycleScope.launch {
            searchResults.clear()

            if (!query.isNullOrEmpty()) {
                val result = supabaseClient.searchUsersByName(query)
                if (result.isSuccess) {
                    searchResults.addAll(result.getOrNull() ?: emptyList())

                    val cursor = MatrixCursor(arrayOf(BaseColumns._ID, "name"))
                    searchResults.forEach { user ->
                        cursor.addRow(arrayOf(user.id_user, user.name))
                    }
                    suggestionsAdapter.changeCursor(cursor)
                }
            } else {
                suggestionsAdapter.changeCursor(null)
            }
        }
    }
}