package com.example.chatapp.newsfeed.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.model.Posts
import com.example.chatapp.newsfeed.adapter.FollowingFriendsAdapter
import com.example.chatapp.newsfeed.adapter.PostAdapter
import com.example.chatapp.newsfeed.adapter.SummaryPostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ListPostFragment() : Fragment() {
    private lateinit var rcvListPost: RecyclerView
    private lateinit var adapterPost: SummaryPostAdapter
    private var listSummaryPost: ArrayList<String> = arrayListOf()
    private var listCountImage: ArrayList<Int> = arrayListOf()
    private lateinit var database: DatabaseReference

    lateinit var profileID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_list_post, container, false)
            rcvListPost = view.findViewById(R.id.rcvListPost)
        database = Firebase.database.reference
        profileID = arguments?.getString("profileID") ?: ""
        loadPost()
        with(rcvListPost) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
        adapterPost = SummaryPostAdapter(listSummaryPost, listCountImage)
        rcvListPost.adapter = adapterPost
        return view
    }

    fun loadPost() {
        database.child(Constant.POST_TABLE_NAME)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val post = data.getValue(Posts::class.java)
                        if (post?.publisher.equals(profileID)) {
                            if (post != null && post.listPhoto != null) {
                                listSummaryPost.add(post.listPhoto!![0])
                                listCountImage.add(post.listPhoto!!.size)
                                adapterPost.notifyDataSetChanged()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}