package com.example.chatapp.newsfeed

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chatapp.R
import com.example.chatapp.chat.Chat_Activity
import com.example.chatapp.databinding.ActivityNewsfeedBinding

class NewsfeedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsfeedBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsfeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Dùng để bottom navigation có thể chuyển fragment
        navController = findNavController(R.id.fragmentContainerViewNews)
        binding.bottomNavigationNews.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Cập nhật TextView dựa trên label của fragment
            binding.actionbarNewsName.text = destination.label
        }
        //Chuyển qua chat
        binding.imgChat.setOnClickListener {
            startActivity(Intent(this, Chat_Activity::class.java))
        }
        binding.addNews.setOnClickListener {
            navController.navigate(R.id.postNewsFragment)
        }
    }
}