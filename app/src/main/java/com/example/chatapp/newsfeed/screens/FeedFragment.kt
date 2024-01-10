package com.example.chatapp.newsfeed.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentFeedBinding
import com.example.chatapp.model.Posts
import com.example.chatapp.model.Stories
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.adapter.PostAdapter
import com.example.chatapp.newsfeed.adapter.StoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class FeedFragment : Fragment() {
    private lateinit var _binding: FragmentFeedBinding
    private val binding get() = _binding
    private lateinit var database: DatabaseReference
    private var followingList: ArrayList<String> = arrayListOf()
    private lateinit var postAdapter: PostAdapter
    private var postList: ArrayList<Posts> = arrayListOf()
    private var listStoryUID: ArrayList<String> = arrayListOf()
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        database = Firebase.database.reference

        checkFollowing()
        initAddStoryImage()
        initUIStories()
        initUIPost()
        refreshPost()

        binding.imageAddStory.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_storyNewsFragment)
        }

        return binding.root
    }

    private fun refreshPost() {
        binding.refreshPost.setOnRefreshListener {
            getPosts()
            getStories()
            binding.refreshPost.isRefreshing = false
        }
    }

    private fun initUIStories() {
        binding.apply {
            rcvStories.setHasFixedSize(true)
            rcvStories.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            storyAdapter = StoryAdapter(listStoryUID, findNavController())
            rcvStories.adapter = storyAdapter
        }
    }

    private fun initUIPost() {
        binding.apply {
            rcvNews.setHasFixedSize(true)
            rcvNews.layoutManager = LinearLayoutManager(requireContext())
            postAdapter = PostAdapter(postList, findNavController(), false)
            rcvNews.adapter = postAdapter
        }
    }

    private fun checkFollowing() {
        database.child(Constant.FOLLOW_TABLE_NAME).child(auth.uid.toString())
            .child(Constant.FOLLOW_TABLE_FOLLOWING)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    followingList.clear()
                    snapshot.children.forEach {
                        followingList.add(it.key!!)
                    }
                    getPosts()
                    getStories()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun initAddStoryImage() {
        database.child(Constant.USER_TABLE_NAME).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.child(auth.uid.toString()).getValue(Users::class.java)
                user?.let {
                    Picasso.get().load(it.avatar).into(binding.addStoryImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getStories() {
        for (id in followingList) {
            database.child(Constant.STORY_TABLE_NAME).child(id)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listStoryUID.clear()
                        val timeCurrent = System.currentTimeMillis()
                        for (data in snapshot.children) {
                            val story = data.getValue(Stories::class.java)
                            if (timeCurrent > story!!.timeStart && timeCurrent < story.timeEnd) {
                                if (!listStoryUID.contains(id)) {
                                    listStoryUID.add(id)
                                }
                            } else database.child(story!!.storyID.toString()).removeValue()
                        }
                        database.child(Constant.STORY_TABLE_NAME).child(auth.uid!!)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val timeCurrent = System.currentTimeMillis()
                                    for (data in snapshot.children) {
                                        val story = data.getValue(Stories::class.java)
                                        if (timeCurrent > story!!.timeStart && timeCurrent < story.timeEnd) {
                                            if (!listStoryUID.contains(auth.uid)) {
                                                listStoryUID.add(0, auth.uid!!)
                                            }
                                        } else database.child(story!!.storyID.toString()).removeValue()
                                    }
//                        listStoryUID = storyUIDSets.toList() as ArrayList<String>
                                    storyAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                        storyAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })



        }
    }

    private fun getPosts() {
        database.child(Constant.POST_TABLE_NAME)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    for (data in snapshot.children) {
                        val post = data.getValue(Posts::class.java)
                        for (id in followingList) {
                            if (post != null) {
                                if (post.publisher.equals(id)) {
                                    postList.add(0, post)
                                }
                            }
                        }
                    }
                    if (postList.size == 0) {
                        binding.rcvNews.visibility = View.GONE
                        binding.txtRecommend.visibility = View.VISIBLE
                    }
                    postAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}