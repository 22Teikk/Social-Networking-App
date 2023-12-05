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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentCommentBinding
import com.example.chatapp.model.Comments
import com.example.chatapp.model.Notifications
import com.example.chatapp.newsfeed.adapter.CommentAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
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
    private var listComment: ArrayList<Comments> = arrayListOf()
    private lateinit var adapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommentBinding.inflate(layoutInflater, container, false)
        hideActionBar()

        database = Firebase.database.reference

        initCommentBefore()
        getComment()
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

    private fun initCommentBefore() {
        binding.apply {
            adapter = CommentAdapter(listComment)
            rcvComment.setHasFixedSize(true)
            rcvComment.layoutManager = LinearLayoutManager(requireContext())
            rcvComment.adapter = adapter
        }
    }

    private fun getComment() {
        database.child(Constant.COMMENT_TABLE_NAME).child(args.postID)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listComment.clear()
                    for (data in snapshot.children) {
                        val comment = data.getValue(Comments::class.java)
                        if (comment != null) {
                            listComment.add(comment)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    //Comment Post
    private fun commentPost() {
        binding.apply {
            if(edtComment.text.equals("")) {
                Toast.makeText(requireContext(), "Can't send empty comment!", Toast.LENGTH_SHORT).show()
            }else {
                val comments = Comments(database.child(Constant.COMMENT_TABLE_NAME).push().key ,edtComment.text.toString(), args.authID)
                database.child(Constant.COMMENT_TABLE_NAME).child(args.postID).child(comments.commentID.toString()).setValue(comments)
                    .addOnCompleteListener {
                        edtComment.setText("")
                        addNotification("Comment your post: " + comments.comment)
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

    //For create Notification
    private fun addNotification(content: String) {
        val notifications = Notifications(Firebase.auth.uid, content, args.postID)
        database.child(Constant.NOTIFICATION_TABLE_NAME).child(args.publisherID).push().setValue(notifications)
    }

    override fun onStart() {
        hideActionBar()
        super.onStart()
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