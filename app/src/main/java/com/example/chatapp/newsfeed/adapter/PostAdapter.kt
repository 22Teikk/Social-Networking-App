package com.example.chatapp.newsfeed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.databinding.PostItemBinding
import com.example.chatapp.model.Posts
import com.example.chatapp.model.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class PostAdapter(private val listPost: ArrayList<Posts>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

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
        val post = listPost[position]
        holder?.apply {
            binding?.apply {
                val listImage: List<String>? = post.listPhoto
                descriptionPost.text = post.title
                post.publisher?.let { publisherInform(this, it) }
                val adapter = ImageInPostAdapter(ArrayList(listImage), listImagePost)
                listImagePost.adapter = adapter
            }
        }
    }

    private fun publisherInform(binding: PostItemBinding, userId: String) {
        Firebase.database.reference.child(Constant.USER_TABLE_NAME).child(userId)
        .addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users = snapshot.getValue(Users::class.java)!!
                Picasso.get().load(user.avatar).into(binding.avatarUser)
                binding.userName.setText(user.name)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}