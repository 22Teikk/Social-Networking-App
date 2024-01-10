package com.example.chatapp.newsfeed.screens

import ImageStoryAdapter
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentViewStoryBinding
import com.example.chatapp.model.Stories
import com.example.chatapp.model.Users
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Timer
import java.util.TimerTask

class ViewStoryFragment : Fragment() {
    private lateinit var _binding: FragmentViewStoryBinding
    private val binding get() = _binding
    private val args: ViewStoryFragmentArgs by navArgs()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var listStory: ArrayList<Stories> = arrayListOf()
    private var adapter: ImageStoryAdapter ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hideActionBar()
        _binding = FragmentViewStoryBinding.inflate(inflater, container, false)
        database = Firebase.database.reference
        auth = Firebase.auth
        binding.apply {
            tabStory.setupWithViewPager(viewPagerImageStory)
            getUser(this)
            getStory(this)
            autoImageSlide(this)
            exitStory.setOnClickListener {
                findNavController().navigateUp()
            }
            userLayout.setOnClickListener {
                val action = ViewStoryFragmentDirections.actionViewStoryFragmentToProfileFragment(args.userID)
                findNavController().navigate(action)
            }
        }

        return binding.root
    }

    private fun autoImageSlide(binding: FragmentViewStoryBinding) {
        val handler = Handler()
        val runable = object : Runnable {
            override fun run() {
                if (binding.viewPagerImageStory.currentItem == listStory.size - 1)
                    binding.viewPagerImageStory.currentItem = 0
                else{
                    binding.viewPagerImageStory.setCurrentItem(binding.viewPagerImageStory.currentItem + 1, true)
                }
            }
        }
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post(runable)
            }
        }, 5000, 5000)
    }

    private fun getUser(binding: FragmentViewStoryBinding) {
        database.child(Constant.USER_TABLE_NAME)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.child(args.userID).getValue(Users::class.java)
                    user?.let {
                        Picasso.get().load(it.avatar).into(binding.avatarFriend)
                        binding.txtName.text = it.name
                    }
                }

                override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                }

            })
    }



    private fun getStory(binding: FragmentViewStoryBinding) {
        database.child(Constant.STORY_TABLE_NAME).child(args.userID)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listStory.clear()
                    for (data in snapshot.children) {
                        val story = data.getValue(Stories::class.java)
                        if (story != null) {
                            if (story.viewer == null) {
                                listStory.add(story)
                            }else {
                                if (story.viewer!!.contains(auth.uid)) listStory.add(story)
                                else listStory.add(0, story)
                            }
                        }
                    }
                    adapter = ImageStoryAdapter(requireContext().applicationContext, listStory, findNavController())
                    binding.viewPagerImageStory.adapter = adapter
                    adapter?.notifyDataSetChanged()
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