package com.example.chatapp.newsfeed.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView.OnCloseListener
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentSearchFriendBinding
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.adapter.SearchFriendAdapter
import com.example.chatapp.newsfeed.adapter.SearchFriendBeforeAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SearchFriendFragment : Fragment() {
    private lateinit var _binding: FragmentSearchFriendBinding
    private val binding get() = _binding
    private var listUser: ArrayList<Users> = arrayListOf()
    private var listUserSearchBefore: ArrayList<Users> = arrayListOf()
    private lateinit var searchAdapter: SearchFriendAdapter
    private lateinit var searchBeforeAdapter: SearchFriendBeforeAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchFriendBinding.inflate(inflater, container, false)

        binding.apply {
            //For rcv search
            rcvFriendSearch.setHasFixedSize(true)
            rcvFriendSearch.layoutManager = LinearLayoutManager(requireContext())
            searchAdapter = SearchFriendAdapter(listUser, listUserSearchBefore, findNavController())
            rcvFriendSearch.adapter = searchAdapter


            //For rcv save friend search before
            rcvFriendSearchBefore.setHasFixedSize(true)
            rcvFriendSearchBefore.layoutManager = LinearLayoutManager(requireContext())
            searchBeforeAdapter = SearchFriendBeforeAdapter(listUserSearchBefore, findNavController())
            rcvFriendSearchBefore.adapter = searchBeforeAdapter

            searchFriend.setOnSearchClickListener {
                rcvFriendSearch.visibility = View.VISIBLE
                rcvFriendSearchBefore.visibility = View.GONE
            }
            searchFriend.setOnQueryTextListener(object : OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    rcvFriendSearch.visibility = View.VISIBLE
                    rcvFriendSearchBefore.visibility = View.GONE
                    if (newText == "") {
                        listUser.clear()
                    }
                    queryFriendByName(newText)
                    return true
                }
            })
            searchFriend.setOnCloseListener(object : OnCloseListener{
                override fun onClose(): Boolean {
                    rcvFriendSearch.visibility = View.GONE
                    rcvFriendSearchBefore.visibility = View.VISIBLE
                    return true
                }
            })
        }

        return binding.root
    }

    private fun queryFriendByName(queryName: String?) {
        val database = Firebase.database.reference
        database.child(Constant.USER_TABLE_NAME).orderByChild("name").startAt(queryName).endAt(queryName+"\uf8ff")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listUser.clear()
                    if (snapshot.exists()) {
                        for (item in snapshot.children) {
                            val user: Users = item.getValue(Users::class.java)!!
                            listUser.add(user)
                        }
                    }
                    searchAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

}