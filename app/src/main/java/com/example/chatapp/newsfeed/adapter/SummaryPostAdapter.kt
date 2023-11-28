package com.example.chatapp.newsfeed.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.databinding.ListImagePostItemBinding
import com.example.chatapp.databinding.ListPostItemBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class SummaryPostAdapter (
    private val listSummaryPost: ArrayList<String>,
    private val listCountPost: ArrayList<Int>
    ): RecyclerView.Adapter<SummaryPostAdapter.SummaryPostViewHolder>() {

    inner class SummaryPostViewHolder(val binding: ListPostItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryPostViewHolder {
        return SummaryPostViewHolder(
            ListPostItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return listSummaryPost.size
    }

    override fun onBindViewHolder(holder: SummaryPostViewHolder, position: Int) {
        val imageUri = listSummaryPost[position]
        holder.apply {
            binding.apply {
                Picasso.get().load(imageUri).into(postImage)
                val count = listCountPost[position] - 1
                if (count > 0) moreImages.visibility = View.VISIBLE
                else moreImages.visibility = View.GONE
            }
        }
    }
}