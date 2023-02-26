package com.dream.chatbot

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dream.chatbot.activity.LoginActivity
import com.dream.chatbot.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.postDelayed(
            {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }, 2500
        )
    }
}