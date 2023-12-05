package com.example.chatapp.newsfeed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.PostItemBinding
import com.example.chatapp.model.Posts
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.screens.FeedFragmentDirections
import com.example.chatapp.newsfeed.screens.PostDetailFragmentDirections
import com.example.chatapp.newsfeed.screens.ProfileFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class PostAdapter(private val listPost: ArrayList<Posts>, val navController: NavController, val isProfile: Boolean) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var user: Users

    inner class PostViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listPost.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        database = Firebase.database.reference
        auth = Firebase.auth
        val post = listPost[position]
        holder?.apply {
            binding?.apply {
                val listImage: List<String>? = post.listPhoto
                descriptionPost.text = post.title
                post.publisher?.let { publisherInform(this, it) }
                post.pid?.let { isLike(this, it) }
                post.pid?.let { isSave(this, it) }
                post.pid?.let { countLike(binding, it) }
                val adapter = ImageInPostAdapter(ArrayList(listImage))
                listImagePost.adapter = adapter

                likePost.setOnClickListener {
                    post.pid?.let { it1 -> likePostFromAuth(this, it1) }
                }
                commentPost.setOnClickListener {
                    if (isProfile) {
                        val action = PostDetailFragmentDirections.actionPostDetailFragmentToCommentFragment(
                            post.pid.toString(),
                            user.name.toString(),
                            user.avatar.toString(),
                            post.title.toString(),
                            auth.uid.toString(),
                            post.publisher.toString()
                        )
                        navController.navigate(action)
                    } else {
                        val action = FeedFragmentDirections.actionFeedFragmentToCommentFragment(
                            post.pid.toString(),
                            user.name.toString(),
                            user.avatar.toString(),
                            post.title.toString(),
                            auth.uid.toString(),
                            post.publisher.toString()
                        )
                        navController.navigate(action)
                    }
                }

                savePost.setOnClickListener {
                    post.pid?.let { it1 -> SavePost(this, it1) }
                }

                avatarUser.setOnClickListener {
                    val action = FeedFragmentDirections.actionFeedFragmentToProfileFragment(post.publisher.toString())
                    navController.navigate(action)
                }

                userName.setOnClickListener {
                    val action = FeedFragmentDirections.actionFeedFragmentToProfileFragment(post.publisher.toString())
                    navController.navigate(action)
                }

                viewDetailPost.setOnClickListener {
                    
                }
            }
        }
    }

    //For Save Post
    private fun SavePost(binding: PostItemBinding, postId: String) {
        if (binding.savePost.tag.equals("Save")) {
            auth.uid?.let {
                database.child(Constant.SAVE_TABLE_NAME).child(it).child(postId).setValue(true)
            }
        } else {
            auth.uid?.let {
                database.child(Constant.SAVE_TABLE_NAME).child(it).child(postId).removeValue()
            }
        }
    }
    private fun isSave(binding: PostItemBinding, postId: String) {
        database.child(Constant.SAVE_TABLE_NAME).child(auth.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(postId).exists()) {
                        binding.savePost.setImageResource(R.drawable.baseline_flag_24)
                        binding.savePost.setTag("Saved")
                    } else {
                        binding.savePost.setImageResource(R.drawable.baseline_outlined_flag_24)
                        binding.savePost.setTag("Save")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
    //For Save Post

    //Set action like Post
    private fun likePostFromAuth(binding: PostItemBinding, postId: String) {
        if (binding.likePost.tag.equals("Like")) {
            auth.uid?.let {
                database.child(Constant.LIKE_TABLE_NAME).child(postId).child(it).setValue(true)
            }
        } else {
            auth.uid?.let {
                database.child(Constant.LIKE_TABLE_NAME).child(postId).child(it).removeValue()
            }
        }
    }
    //Set action like Post

    //For check like Post from Auth
    private fun isLike(binding: PostItemBinding, postId: String) {
        database.child(Constant.LIKE_TABLE_NAME).child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(Firebase.auth.uid!!).exists()) {
                        binding.likePost.setImageResource(R.drawable.baseline_favorite_24)
                        binding.likePost.setTag("Liked")
                    } else {
                        binding.likePost.setImageResource(R.drawable.baseline_favorite_before)
                        binding.likePost.setTag("Like")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
    //For check like Post from Auth

    //For count like of Post
    private fun countLike(binding: PostItemBinding, postId: String) {
        database.child(Constant.LIKE_TABLE_NAME).child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount.toInt() == 0) {
                        binding.countLikePost.text = "Be the first to like this post"
                    } else binding.countLikePost.text =
                        snapshot.childrenCount.toInt().toString() + " people liked this post"
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    //For count like of Post

    //For set user Information
    private fun publisherInform(binding: PostItemBinding, userId: String) {
        database.child(Constant.USER_TABLE_NAME).child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(Users::class.java)!!
                    Picasso.get().load(user.avatar).into(binding.avatarUser)
                    binding.userName.setText(user.name)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
    //For set user Information

}