package com.example.slava.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.slava.R
import com.example.slava.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.let { fragment ->
            (fragment as? androidx.navigation.fragment.NavHostFragment)?.navController
        } ?: throw IllegalStateException("NavController не найден")
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_home)
                    true
                }
                R.id.nav_challenge -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_challenge)
                    true
                }
                R.id.nav_setting -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_setting)
                    true
                }
                R.id.nav_profile -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_profile)
                    true
                }
                else -> false
            }
        }

        binding.addButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, ChallengeCreateActivity::class.java))
        }
    }
}