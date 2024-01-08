package com.example.chatapp.newsfeed.screens

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class ViewStoryFragment : Fragment() {
    private lateinit var _binding: FragmentViewStoryBinding
    private val binding get() = _binding
    private val args: ViewStoryFragmentArgs by navArgs()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var isProgressBarRunning = false
    private var story: Stories ?= null
    private var user: Users ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hideActionBar()
        _binding = FragmentViewStoryBinding.inflate(inflater, container, false)
        database = Firebase.database.reference
        auth = Firebase.auth
        binding.apply {
            getUser(this)
            getStory(this)
            if (args.userID == auth.uid) {
                getCountViewStory(this)
                delStory.setOnClickListener {
                    database.child(Constant.STORY_TABLE_NAME).child(args.storyID).removeValue()
                    Toast.makeText(requireContext(), "Delete Success", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
            exitStory.setOnClickListener {
                stopProgressBar()
                findNavController().navigateUp()
            }
            userLayout.setOnClickListener {
                val action = ViewStoryFragmentDirections.actionViewStoryFragmentToProfileFragment(args.userID)
                findNavController().navigate(action)
            }
            previousStory.setOnClickListener {

            }
            nextStory.setOnClickListener {
//                findNavController().navigateUp()
//                findNavController().navigate(R.id.action_viewStoryFragment_to_likeStoryFragment)
            }
            showViewer.setOnClickListener {
                stopProgressBar()
                val action = story?.storyID?.let { it1 ->
                    ViewStoryFragmentDirections.actionViewStoryFragmentToFollowAndLikeFragment("Viewer",
                        it1
                    )
                }
                action?.let { it1 -> findNavController().navigate(it1) }
            }
        }

        return binding.root
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

    private fun getCountViewStory(binding: FragmentViewStoryBinding) {
        binding.countViewLayout.visibility = View.VISIBLE
        database.child(Constant.STORY_TABLE_NAME).child(args.storyID).child("viewer")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    if (count > 1)
                        binding.countView.text =  "${count - 1} people view your story"
                    else binding.countView.text = "None view your story"
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun getStory(binding: FragmentViewStoryBinding) {
        database.child(Constant.STORY_TABLE_NAME)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        story = data.getValue(Stories::class.java)
                        if (story!!.storyID == args.storyID) {
                            Picasso.get().load(story!!.imageURL).into(binding.imageStory)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        startProgressBarAnimation()

        // Delay for 5 seconds and then remove the fragment
        Handler(Looper.getMainLooper()).postDelayed({
            if (isProgressBarRunning) {
                stopProgressBar()
                findNavController().navigateUp()
            }
        }, 5000)
    }

    private fun startProgressBarAnimation() {
        val progressMax = 100
        val progressIncrement = 1
        val delayMillis = 50 // Set the delay between each increment
        isProgressBarRunning = true
        Thread {
            var progress = 0
            while (progress <= progressMax && isProgressBarRunning) {
                activity?.runOnUiThread {
                    binding.pgbStory.progress = progress
                }
                try {
                    Thread.sleep(delayMillis.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                progress += progressIncrement
            }
            isProgressBarRunning = false
        }.start()
    }

    private fun stopProgressBar() {
        isProgressBarRunning = false
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