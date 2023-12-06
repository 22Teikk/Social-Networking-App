package com.example.chatapp.newsfeed.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentFollowAndLikeBinding
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.adapter.SearchFriendAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FollowAndLikeFragment : Fragment() {
    private lateinit var _binding: FragmentFollowAndLikeBinding
    private val binding get() = _binding
    private lateinit var userAdapter: SearchFriendAdapter
    private var listUser: ArrayList<Users> = arrayListOf()
    private var listID: ArrayList<String> = arrayListOf()
    private val args: FollowAndLikeFragmentArgs by navArgs()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFollowAndLikeBinding.inflate(inflater, container, false)
        database = Firebase.database.reference
        auth = Firebase.auth
        binding.apply {
            tvTitle.text = args.option
            userAdapter = SearchFriendAdapter(listUser, arrayListOf(), findNavController())
            with(rcvFollowOrLike) {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = userAdapter
            }
            when (args.option) {
                "Like" -> {
                    userAdapter.option = "Like"
                    getLikeUsers()
                }
                "Followers" -> {
                    userAdapter.option = "Followers"
                    getFollowers()
                }
                "Following" -> {
                    userAdapter.option = "Following"
                    getFollowing()
                }
            }
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        return binding.root
    }

    private fun getFollowing() {
        database.child(Constant.FOLLOW_TABLE_NAME).child(args.id).child(Constant.FOLLOW_TABLE_FOLLOWING)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listID.clear()
                    for (data in snapshot.children) {
                        listID.add(data.key.toString())
                    }
                    getUser()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun getFollowers() {
        database.child(Constant.FOLLOW_TABLE_NAME).child(args.id).child(Constant.FOLLOW_TABLE_FOLLOWER)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listID.clear()
                    for (data in snapshot.children) {
                        listID.add(data.key.toString())
                    }
                    getUser()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun getLikeUsers() {
        database.child(Constant.LIKE_TABLE_NAME).child(args.id)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listID.clear()
                    for (data in snapshot.children) {
                        listID.add(data.key.toString())
                    }
                    getUser()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun getUser() {
        database.child(Constant.USER_TABLE_NAME)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listUser.clear()
                    for (data in snapshot.children) {
                        val user = data.getValue(Users::class.java)
                        user?.let {
                            for (id in listID)
                                if (it.uid.toString() == id)
                                    listUser.add(0, it)
                        }
                    }
                    userAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
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