package com.example.chatapp.newsfeed.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.model.Posts
import com.example.chatapp.newsfeed.adapter.FollowingFriendsAdapter
import com.example.chatapp.newsfeed.adapter.PostAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class PostDetailFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var followingAdapter: FollowingFriendsAdapter
    private var followingList: ArrayList<String> = arrayListOf()
    private lateinit var postAdapter: PostAdapter
    private var postList: ArrayList<Posts> = arrayListOf()
    private lateinit var auth: FirebaseAuth
    private val args: PostDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_post_detail, container, false)
        val rcvPostDetail = view.findViewById<RecyclerView>(R.id.postDetail)
        val backToProfile = view.findViewById<ImageView>(R.id.backToProfile)
        auth = Firebase.auth
        database = Firebase.database.reference
        rcvPostDetail.setHasFixedSize(true)
        rcvPostDetail.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList, findNavController(), true)
        rcvPostDetail.adapter = postAdapter
        getPosts()
        backToProfile.setOnClickListener {
            findNavController().navigateUp()
        }
        return view
    }

    private fun getPosts() {
        database.child(Constant.POST_TABLE_NAME).child(args.postID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    val post = snapshot.getValue(Posts::class.java)
                    postList.add(post!!)
                    postAdapter.notifyDataSetChanged()
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