package com.example.chatapp.newsfeed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.databinding.CommentItemBinding
import com.example.chatapp.databinding.FragmentCommentBinding
import com.example.chatapp.model.Comments
import com.example.chatapp.model.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class CommentAdapter(
    private val listComment: ArrayList<Comments>
) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val binding: CommentItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listComment.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = listComment[position]
        holder.apply {
            binding.apply {
                getUserCommentInfo(comment.publisher.toString(), this )
                commentContent.setText(comment.comment)
            }
        }
    }

    private fun getUserCommentInfo(publisherComment: String, binding: CommentItemBinding) {
        Firebase.database.reference.child(Constant.USER_TABLE_NAME).child(publisherComment)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userComment = snapshot.getValue(Users::class.java)
                    binding.apply {
                        if (userComment != null) {
                            Picasso.get().load(userComment.avatar).into(avatarUserComment)
                            userNameComment.setText(userComment.name)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

}