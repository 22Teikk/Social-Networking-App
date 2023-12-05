package com.example.chatapp.newsfeed.screens

import android.os.Bundle
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
import com.example.chatapp.newsfeed.adapter.FollowingFriendsAdapter
import com.example.chatapp.newsfeed.adapter.PostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FeedFragment : Fragment() {
    private lateinit var _binding: FragmentFeedBinding
    private val binding get() = _binding
    private lateinit var database: DatabaseReference
    private lateinit var followingAdapter: FollowingFriendsAdapter
    private var followingList: ArrayList<String> = arrayListOf()
    private lateinit var postAdapter: PostAdapter
    private var postList: ArrayList<Posts> = arrayListOf()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        database = Firebase.database.reference

        initUIFollowingFriends()
        initUIPost()

        refreshPost()

        binding.imageAddFriends.setOnClickListener {
            findNavController().navigate(R.id.searchFriendFragment)
        }

        return binding.root
    }

    private fun refreshPost() {
        binding.refreshPost.setOnRefreshListener {
            getPosts()
            binding.refreshPost.isRefreshing = false
        }
    }

    private fun initUIFollowingFriends() {
        checkFollowing()
        binding.apply {
            rcvFriends.setHasFixedSize(true)
            rcvFriends.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            followingAdapter = FollowingFriendsAdapter(followingList, findNavController())
            rcvFriends.adapter = followingAdapter
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
                    followingAdapter.notifyDataSetChanged()
                    getPosts()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            })
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
                    } else binding.txtRecomend.visibility = View.GONE
                    postAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}