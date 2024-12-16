package com.example.slava

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slava.activities.ChallengeDetailActivity
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
            openChallengeDetail(challenge)
        }
        binding.activeChallengeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.activeChallengeRecyclerView.adapter = adapter

        try {
            lifecycleScope.launch {
                val userId = supabaseClient.getUserById(supabaseClient.getToken(requireContext()).toString())
                binding.scoreTextView.text = supabaseClient.getTotalScore(userId!!.id!!).toString()
            }
        } catch (e: Exception) {
            Log.e("123", e.message.toString())
        }

        binding.parthnersButton.setOnClickListener{
            startActivity(Intent(requireContext(), PartnersActivity::class.java))
        }

        lifecycleScope.launch {
            val userId = getUserIdFromPreferences()
            Log.d("HomeFragment", "UserID: $userId")
            supabaseClient.fetchAcceptedChallenges(userId)
            observeChallenges()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeChallenges() {
        lifecycleScope.launch {
            supabaseClient.activeChallenges.collect { challengeList ->
                Log.d("HomeFragment", "Challenges получены: ${challengeList.size}")
                adapter.updateData(challengeList.toMutableList())
            }
        }
    }

    private fun openChallengeDetail(challenge: Challenge) {
        val intent = Intent(requireContext(), ChallengeDetailActivity::class.java).apply {
            putExtra("challenge", challenge)
        }
        startActivity(intent)
    }

    suspend fun getUserIdFromPreferences(): Int {
        val token = supabaseClient.getToken(requireActivity().baseContext)
        var userId: Int = 0

        if (token != null) {
            try {
                val user = supabaseClient.getUserById(token)
                userId = user?.id ?: 0
            } catch (e: Exception) {
                Log.e("123", e.message.toString())
            }
        }
        return userId
    }

}