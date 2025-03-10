package com.example.slava

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slava.activities.ChallengeDetailActivity
import com.example.slava.activities.LoginActivity
import com.example.slava.activities.PartnersActivity
import com.example.slava.adapters.ActiveChallengeAdapter
import com.example.slava.databinding.FragmentHomeBinding
import com.example.slava.models.Challenge
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val supabaseClient : SupabaseClient = SupabaseClient()
    private val challenges = mutableListOf<Challenge>()
    private lateinit var adapter: ActiveChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ActiveChallengeAdapter(challenges) { challenge ->

        }
        binding.activeChallengeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.activeChallengeRecyclerView.adapter = adapter
        binding.readingCategory.setOnClickListener{
            Toast.makeText(requireContext(), "В разработке.", Toast.LENGTH_SHORT).show()
        }
        binding.programmingCategory.setOnClickListener{
            Toast.makeText(requireContext(), "В разработке.", Toast.LENGTH_SHORT).show()
        }
        binding.sportsCategory.setOnClickListener{
            Toast.makeText(requireContext(), "В разработке.", Toast.LENGTH_SHORT).show()
        }
        binding.languageCategory.setOnClickListener{
            Toast.makeText(requireContext(), "В разработке.", Toast.LENGTH_SHORT).show()
        }

        try {
            lifecycleScope.launch {
                val response = supabaseClient.getUserById(supabaseClient.getToken(requireContext()).toString())
                response.onSuccess { user ->
                    binding.nameTextView.text = "Привет, ${user.name}"
                    binding.scoreTextView.text = supabaseClient.getTotalScore(user.id_user).toString()
                }.onFailure { error ->
                    Toast.makeText(requireContext(), "Ошибка получения пользователя: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("123", e.message.toString())
        }

        binding.parthnersButton.setOnClickListener{
            startActivity(Intent(requireContext(), PartnersActivity::class.java))
        }
    }
}