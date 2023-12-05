package com.example.chatapp.newsfeed.screens

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.model.Posts
import com.example.chatapp.newsfeed.adapter.SummaryPostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ListSaveFragment() : Fragment() {
    private lateinit var rcvListSave: RecyclerView
    lateinit var profileID: String
    private lateinit var adapterPost: SummaryPostAdapter
    private var listSavePost: ArrayList<String> = arrayListOf()
    private var listCountImage: ArrayList<Int> = arrayListOf()
    private var listPostID: ArrayList<String> = arrayListOf()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        profileID = arguments?.getString("profileID") ?: ""
        auth = Firebase.auth
//        if (profileID != auth.uid.toString()) return null
        val view = inflater.inflate(R.layout.fragment_list_save, container, false)
        rcvListSave = view.findViewById(R.id.rcvListSave)
        database = Firebase.database.reference
        loadSavePost()
        with(rcvListSave) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
        adapterPost = SummaryPostAdapter(listSavePost, listCountImage, findNavController(), listPostID)
        rcvListSave.adapter = adapterPost
        return view
    }

    private fun loadSavePost() {
        var listPostSaveID: ArrayList<String> = arrayListOf()
        database.child(Constant.SAVE_TABLE_NAME).child(auth.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        listPostSaveID.add(data.key.toString())
                    }
                    readSaveList(listPostSaveID)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun readSaveList(listPostSaveID: ArrayList<String>) {
        database.child(Constant.POST_TABLE_NAME)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listSavePost.clear()
                    listCountImage.clear()
                    for (data in snapshot.children) {
                        val post = data.getValue(Posts::class.java)
                        for (id in listPostSaveID) {
                            if (post != null) {
                                if (id == post.pid) {
                                    listPostID.add(post.pid!!)
                                    listSavePost.add(post.listPhoto!![0])
                                    listCountImage.add(post.listPhoto!!.size)
                                    adapterPost.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}