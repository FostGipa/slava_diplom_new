package com.example.slava.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slava.R
import com.example.slava.activities.ChallengeDetailActivity
import com.example.slava.activities.UserChallengeDetailActivity
import com.example.slava.adapters.ChallengesAdapter
import com.example.slava.databinding.FragmentChallengeBinding
import com.example.slava.models.Challenge
import com.example.slava.utils.ChallengeClickListener
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChallengeFragment : Fragment(), ChallengeClickListener {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!
    private val supabaseClient: SupabaseClient = SupabaseClient()
    private lateinit var activeAdapter: ChallengesAdapter
    private lateinit var inactiveAdapter: ChallengesAdapter
    private var isShowingActive = false

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        val view = binding.root

        val categories = mutableListOf("Выберите категорию", "Чтение", "Программирование", "Спорт", "Языки")

        val adapter = object : ArrayAdapter<String>(requireContext(), R.layout.spinner_item, categories) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, R.color.black))
                view.textSize = 16f
                view.setPadding(20, 20, 20, 20)
                return view
            }
        }

        binding.categorySpinner.adapter = adapter

        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = if (position == 0) null else categories[position]
                if (position == 0) {
                    binding.categorySpinner.setBackgroundResource(R.drawable.category_unselected)
                } else {
                    binding.categorySpinner.setBackgroundResource(R.drawable.button_bg)
                }

                (view as? TextView)?.setTextColor(
                    ContextCompat.getColor(requireContext(), if (position == 0) R.color.unselected_button else R.color.white)
                )

                loadChallenges(false, selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.categorySpinner.setBackgroundResource(R.drawable.category_unselected)
            }
        }

        val category = arguments?.getString("category")

        if (category != null && categories.contains(category)) {
            val position = categories.indexOf(category)
            binding.categorySpinner.setSelection(position)
        }

        setupRecyclerViews()
        setupButtons()

        return view
    }


    private fun setupRecyclerViews() {
        activeAdapter = ChallengesAdapter(mutableListOf(), requireContext(), this)
        inactiveAdapter = ChallengesAdapter(mutableListOf(), requireContext(), this)

        binding.challengeRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.challengeRecyclerView.adapter = activeAdapter
    }

    override fun onChallengeClick(challenge: Challenge) {
        lifecycleScope.launch {
            val response = supabaseClient.getUserById(supabaseClient.getToken(requireContext()).toString())
            response.onSuccess { user ->
                val activeChallenges = supabaseClient.fetchAllUserAcceptedChallenges(user.id_user!!).getOrElse {
                    Log.e("123", "Ошибка загрузки активных челленджей: ${it.message}")
                    return@launch
                }

                val activeChallengeIds = activeChallenges.map { it.challenge?.id_challenge }
                val isActive = challenge.id_challenge in activeChallengeIds

                val intent = if (isActive) {
                    Intent(requireContext(), UserChallengeDetailActivity::class.java)
                } else {
                    Intent(requireContext(), ChallengeDetailActivity::class.java)
                }
                intent.putExtra("challengeId", challenge.id_challenge)
                startActivity(intent)
            }.onFailure {
                Toast.makeText(requireContext(), "Ошибка получения пользователя!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {
        binding.availableButton.setOnClickListener {
            isShowingActive = false
            updateUI(false)
        }
        binding.activeButton.setOnClickListener {
            isShowingActive = true
            updateUI(true)
        }
        updateUIButtonState(isShowingActive)
    }

    private fun updateUI(isActive: Boolean) {
        updateUIButtonState(isActive)
        loadChallenges(isActive)
    }

    private fun updateUIButtonState(isActive: Boolean) {
        binding.activeButton.setBackgroundResource(if (isActive) R.drawable.button_bg else R.drawable.light_button_bg)
        binding.activeButton.setTextColor(ContextCompat.getColor(requireContext(), if (isActive) android.R.color.white else R.color.unselected_button))
        binding.availableButton.setBackgroundResource(if (isActive) R.drawable.light_button_bg else R.drawable.button_bg)
        binding.availableButton.setTextColor(ContextCompat.getColor(requireContext(), if (isActive) R.color.unselected_button else android.R.color.white))
    }

    private fun loadChallenges(showActive: Boolean, category: String? = null) {
        binding.progressBar.visibility = View.VISIBLE
        binding.challengeRecyclerView.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val response = supabaseClient.getUserById(supabaseClient.getToken(requireContext()).toString())
                response.onSuccess { user ->
                    val activeChallengeResult = supabaseClient.fetchAllUserAcceptedChallenges(user.id_user!!)
                    val activeChallenge = activeChallengeResult.getOrElse {
                        Log.e("123", "Ошибка загрузки активных челленджей: ${it.message}")
                        return@launch
                    }

                    val activeChallengeIds = activeChallenge.map { it.challenge?.id_challenge }
                    val allChallengesResult = supabaseClient.fetchAllChallenges()
                    val allChallenges = allChallengesResult.getOrElse {
                        Log.e("123", "Ошибка загрузки всех челленджей: ${it.message}")
                        return@launch
                    }

                    var challengesToShow = if (showActive) {
                        allChallenges.filter { it.id_challenge in activeChallengeIds }
                    } else {
                        allChallenges.filterNot { it.id_challenge in activeChallengeIds }
                    }

                    // Фильтрация по категории
                    category?.let {
                        challengesToShow = challengesToShow.filter { it.category?.name == category }
                    }

                    withContext(Dispatchers.Main) {
                        val adapter = if (showActive) activeAdapter else inactiveAdapter
                        binding.challengeRecyclerView.adapter = adapter
                        adapter.updateData(challengesToShow.toMutableList())
                    }
                    binding.progressBar.visibility = View.GONE
                    binding.challengeRecyclerView.visibility = View.VISIBLE
                }.onFailure {
                    Toast.makeText(requireContext(), "Ошибка получения пользователя!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("123", "Ошибка: ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val category = arguments?.getString("category")
        if (category == "") {
            loadChallenges(isShowingActive)
        } else {
            loadChallenges(isShowingActive, category)
        }
    }
}