package com.example.slava.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slava.R
import com.example.slava.adapters.RatingAdapter
import com.example.slava.databinding.ActivityRatingBinding
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch

class RatingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRatingBinding
    private lateinit var adapter: RatingAdapter
    private val viewModel: SupabaseClient by viewModels()

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

        adapter = RatingAdapter(mutableListOf())
        binding.ratingRecyclerView.layoutManager = LinearLayoutManager(this@RatingActivity)
        binding.ratingRecyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.ratingItems.collect { items ->
                adapter.updateData(items)
            }
        }

        val intent = intent
        viewModel.fetchRating(intent.getIntExtra("challengeId", 0))
    }
}