package com.example.chatapp.newsfeed.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentCommentBinding
import com.example.chatapp.model.Comments
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class CommentFragment : Fragment() {
    private lateinit var _binding: FragmentCommentBinding
    private val binding get() = _binding
    private val args: CommentFragmentArgs by navArgs()
    private lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommentBinding.inflate(layoutInflater, container, false)
        hideActionBar()

        database = Firebase.database.reference

        binding.apply {
            backToFeeds.setOnClickListener {
                findNavController().navigateUp()
            }
            args?.let {
                Picasso.get().load(it.ownImage).into(avatarOwn)
                ownPost.text = it.ownName
                titlePost.text = it.title
                getImageAuth()
            }
            postComment.setOnClickListener {
                commentPost()
            }
        }

        return binding.root
    }

    //Comment Post
    private fun commentPost() {
        binding.apply {
            if(edtComment.text.equals("")) {
                Toast.makeText(requireContext(), "Can't send empty comment!", Toast.LENGTH_SHORT).show()
            }else {
                val comments = Comments(edtComment.text.toString(), args.publisherID)
                database.child(Constant.COMMENT_TABLE_NAME).child(args.postID).push().setValue(comments)
                    .addOnCompleteListener {
                        edtComment.setText("")
                    }
            }
        }
    }
    //Comment Post

    private fun getImageAuth() {
        database.child(Constant.USER_TABLE_NAME).child(args.authID).child("avatar")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                        Picasso.get().load(snapshot.getValue(String::class.java)).into(binding.avatarAuth)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Cannot load your Image!", Toast.LENGTH_SHORT).show()
                }
            })
    }
    override fun onPause() {
        val actionbar = activity?.findViewById<LinearLayout>(R.id.actionbarNews)
        if (actionbar != null) {
            actionbar.visibility = View.VISIBLE
        }
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationNews)
        if (bottomNav != null) {
            bottomNav.visibility = View.VISIBLE
        }
        super.onPause()
    }
    private fun hideActionBar() {
        val actionbar = activity?.findViewById<LinearLayout>(R.id.actionbarNews)
        if (actionbar != null) {
            actionbar.visibility = View.GONE
        }
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationNews)
        if (bottomNav != null) {
            bottomNav.visibility = View.GONE
        }
    }

}