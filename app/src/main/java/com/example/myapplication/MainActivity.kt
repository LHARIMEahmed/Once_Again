package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.libraries.places.api.Places

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.acceuilHost);
        binding.bottomNav.setupWithNavController(navController)
        Places.initialize(applicationContext, "AIzaSyDmx4rpIX1RKmYG_dB3eistlqH8BqkMlKo")
    }
    fun hideBottomNav() {
        if (::binding.isInitialized) {
            binding.bottomNav.visibility = View.GONE
        }
    }
    fun showBottomNav() {
        if (::binding.isInitialized) {
            binding.bottomNav.visibility = View.VISIBLE
        }
    }
}