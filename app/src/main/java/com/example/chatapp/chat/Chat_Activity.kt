package com.example.chatapp.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.newsfeed.NewsfeedActivity

class Chat_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Dùng để bottom navigation có thể chuyển fragment
        navController = findNavController(R.id.fragmentContainerViewChat)
        binding.bottomNavigationChat.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Cập nhật TextView dựa trên label của fragment
            binding.actionbarNewsName.text = destination.label
        }
        //Chuyển về News Feed
        binding.imgNews.setOnClickListener {
            startActivity(Intent(this, NewsfeedActivity::class.java))
        }
    }
}