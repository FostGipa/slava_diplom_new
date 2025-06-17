package com.example.slava.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slava.R
import com.example.slava.adapters.RatingAdapter
import com.example.slava.databinding.ActivityRatingBinding
import com.example.slava.models.UserChallenge
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch

class RatingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRatingBinding
    private lateinit var adapter: RatingAdapter
    private val supabaseClient = SupabaseClient()
    private var challengeId = 0
    private var challenges = mutableListOf<UserChallenge>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener{
            finish()
        }

        challengeId = intent.getIntExtra("challengeId", 0)

        adapter = RatingAdapter(mutableListOf())
        binding.ratingRecyclerView.layoutManager = LinearLayoutManager(this@RatingActivity)
        binding.ratingRecyclerView.adapter = adapter

        lifecycleScope.launch {
            val response = supabaseClient.fetchRating(challengeId)
            response.onSuccess { ratingList ->
                challenges = ratingList.toMutableList()
                adapter.updateData(challenges)
            }.onFailure { e ->
                Log.d("123", e.message.toString())
            }
        }
    }
}