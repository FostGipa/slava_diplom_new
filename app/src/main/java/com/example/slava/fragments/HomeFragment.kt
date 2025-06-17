package com.example.slava.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slava.R
import com.example.slava.activities.PartnersActivity
import com.example.slava.activities.UserChallengeDetailActivity
import com.example.slava.adapters.ActiveChallengeAdapter
import com.example.slava.adapters.MyChallengesAdapter
import com.example.slava.databinding.FragmentHomeBinding
import com.example.slava.models.UserChallenge
import com.example.slava.models.Challenge
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val supabaseClient: SupabaseClient = SupabaseClient()
    private var challenges = mutableListOf<UserChallenge>()
    private lateinit var adapter: ActiveChallengeAdapter
    private lateinit var myChallengesAdapter: MyChallengesAdapter
    private var isMyChallengesExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ActiveChallengeAdapter(mutableListOf()) { challenge ->
            startActivity(
                Intent(requireContext(), UserChallengeDetailActivity::class.java)
                    .putExtra("challengeId", challenge.challenge?.id_challenge)
            )
        }

        binding.activeChallengeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.activeChallengeRecyclerView.adapter = adapter

        myChallengesAdapter = MyChallengesAdapter(emptyList())
        binding.myChallengesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.myChallengesRecyclerView.adapter = myChallengesAdapter

        binding.myChallengesHeader.setOnClickListener {
            toggleMyChallengesVisibility()
        }

        binding.readingCategory.setOnClickListener {
            categoryChallengeTransaction("Чтение")
        }
        binding.programmingCategory.setOnClickListener {
            categoryChallengeTransaction("Программирование")
        }
        binding.sportsCategory.setOnClickListener {
            categoryChallengeTransaction("Спорт")
        }
        binding.languageCategory.setOnClickListener {
            categoryChallengeTransaction("Языки")
        }

        binding.parthnersButton.setOnClickListener {
            startActivity(Intent(requireContext(), PartnersActivity::class.java))
        }

        loadUserDataAndChallenges()
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserDataAndChallenges() {
        binding.linearLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val token = supabaseClient.getToken(requireContext()).toString()
            val response = supabaseClient.getUserById(token)

            response.onSuccess { user ->
                binding.nameTextView.text = "Привет, ${user.name}"
                binding.scoreTextView.text = user.user_pts.toString()

                // Заявки, в которых участвует пользователь
                val responseChallenge = supabaseClient.fetchAllUserAcceptedChallenges(user.id_user!!)
                responseChallenge.onSuccess { challengeList ->
                    challenges = challengeList.toMutableList()
                    adapter.updateData(challenges)
                }

                // Челленджи, которые создал сам пользователь
                val createdChallengeResult = supabaseClient.fetchUserCreatedChallenge(user.id_user)
                createdChallengeResult.onSuccess { myChallenge ->
                    myChallengesAdapter.updateData(myChallenge)
                }.onFailure {
                    // не показываем ошибку, если ничего не создано
                }

                binding.progressBar.visibility = View.GONE
                binding.linearLayout.visibility = View.VISIBLE
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Ошибка получения пользователя: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun toggleMyChallengesVisibility() {
        isMyChallengesExpanded = !isMyChallengesExpanded
        binding.myChallengesRecyclerView.visibility = if (isMyChallengesExpanded) View.VISIBLE else View.GONE
        val iconRes = if (isMyChallengesExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
        binding.arrowIcon.setImageResource(iconRes)
        binding.nestedScrollView.post {
            binding.nestedScrollView.smoothScrollTo(0, binding.myChallengesRecyclerView.bottom)
        }
    }

    private fun categoryChallengeTransaction(category: String) {
        val bundle = Bundle().apply {
            putString("category", category)
        }
        findNavController().navigate(R.id.nav_challenge, bundle)
    }

    override fun onResume() {
        super.onResume()
        loadUserDataAndChallenges()
    }
}
