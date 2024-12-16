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
import com.example.slava.adapters.ChallengesAdapter
import com.example.slava.utils.ChallengeClickListener
import com.example.slava.databinding.FragmentChallengeBinding
import com.example.slava.models.Challenge
import com.example.slava.utils.SupabaseClient
import com.example.slava.utils.UserState
import kotlinx.coroutines.launch

class ChallengeFragment : Fragment(), ChallengeClickListener {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChallengesAdapter
    private val supabaseClient: SupabaseClient = SupabaseClient()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Инициализируем адаптер с пустым списком и передаем интерфейс обратного вызова
        adapter = ChallengesAdapter(mutableListOf(), requireActivity().baseContext, this)
        binding.challengeRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.challengeRecyclerView.adapter = adapter

        // Запускаем загрузку вызовов
        supabaseClient.fetchChallenges()

        lifecycleScope.launch {
            supabaseClient.challenges.collect { challenges ->
                adapter.updateData(challenges)
                adapter.notifyDataSetChanged()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Реализация метода интерфейса
    override fun onChallengeClick(challenge: Challenge) {
        val intent = Intent(requireContext(), ChallengeDetailActivity::class.java)
        intent.putExtra("challenge", challenge)
        startActivity(intent)
    }
}