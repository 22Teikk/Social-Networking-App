package com.example.chatapp.newsfeed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FriendSearchItemBinding
import com.example.chatapp.model.Notifications
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.screens.FollowAndLikeFragmentArgs
import com.example.chatapp.newsfeed.screens.FollowAndLikeFragmentDirections
import com.example.chatapp.newsfeed.screens.SearchFriendFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class SearchFriendAdapter(
    private val userList: ArrayList<Users>,
    private val userListBefore: ArrayList<Users>,
    private val navController: NavController,
    var option: String = "Search",
) : RecyclerView.Adapter<SearchFriendAdapter.SearchViewHolder>() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    inner class SearchViewHolder(val binding: FriendSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            FriendSearchItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val user = userList[position]
        auth = Firebase.auth
        database = Firebase.database.reference

        holder.apply {
            binding.apply {
                if (option == "Following" || option == "Search")
                    followFriends.visibility = View.VISIBLE
                else followFriends.visibility = View.GONE
                Picasso.get().load(user.avatar).into(avatarFriendSearchItem)
                friendNameSearchItem.setText(user.name)
                if (user.bio == null || user.bio == "") friendBioSearchItem.setText(user.gender)
                else friendBioSearchItem.setText(user.bio)
                isFollowing(user.uid!!, followFriends)
                followFriends.setOnClickListener {
                    if (followFriends.contentDescription == "Follow") {
                        Firebase.database.reference.child(Constant.FOLLOW_TABLE_NAME)
                            .child(auth.uid!!)
                            .child(Constant.FOLLOW_TABLE_FOLLOWING).child(
                                user.uid!!
                            ).setValue(true)
                        Firebase.database.reference.child(Constant.FOLLOW_TABLE_NAME)
                            .child(user.uid!!)
                            .child(Constant.FOLLOW_TABLE_FOLLOWER).child(
                                auth.uid!!
                            ).setValue(true)
                        addNotification(user.uid!!, "Start following you")
                        userListBefore.add(user)
                    } else {
                        Firebase.database.reference.child(Constant.FOLLOW_TABLE_NAME)
                            .child(auth.uid!!)
                            .child(Constant.FOLLOW_TABLE_FOLLOWING).child(
                                user.uid!!
                            ).removeValue()
                        Firebase.database.reference.child(Constant.FOLLOW_TABLE_NAME)
                            .child(user.uid!!)
                            .child(Constant.FOLLOW_TABLE_FOLLOWER).child(
                                auth.uid!!
                            ).removeValue()
                    }

                }
                if (user.uid.equals(auth.uid)) {
                    followFriends.visibility = View.GONE
                }
                viewFriend.setOnClickListener {
//                    userListBefore.add(user)
                    when (option) {
                        "Like", "Followers", "Following" -> {
                            val action = FollowAndLikeFragmentDirections.actionFollowAndLikeFragmentToProfileFragment(user.uid!!)
                            navController.navigate(action)
                        }
                        else -> {
                            val action =
                                SearchFriendFragmentDirections.actionSearchFriendFragmentToProfileFragment(
                                    user.uid!!
                                )
                            navController.navigate(action)
                        }
                    }
                }

            }
        }
    }

    fun isFollowing(userID: String, imageFollow: ImageView) {
        database.child(Constant.FOLLOW_TABLE_NAME).child(auth.uid!!)
            .child(Constant.FOLLOW_TABLE_FOLLOWING)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(userID).exists()) {
                        imageFollow.setImageResource(R.drawable.baseline_done_24)
                        imageFollow.contentDescription = "Following"
                    } else {
                        imageFollow.setImageResource(R.drawable.baseline_add_24)
                        imageFollow.contentDescription = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun addNotification(userId: String, content: String) {
        val notifications = Notifications(auth.uid.toString(), content, "")
        database.child(Constant.NOTIFICATION_TABLE_NAME).child(userId).push()
            .setValue(notifications)
    }
}