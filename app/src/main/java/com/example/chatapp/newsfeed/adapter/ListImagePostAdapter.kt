package com.example.chatapp.newsfeed.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ListImagePostItemBinding

class ListImagePostAdapter(
    private val listImagePost: ArrayList<Uri>
): RecyclerView.Adapter<ListImagePostAdapter.ListImagePostViewHolder>() {

    inner class ListImagePostViewHolder(val binding: ListImagePostItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListImagePostViewHolder {
        return ListImagePostViewHolder(
            ListImagePostItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return listImagePost.size
    }

    override fun onBindViewHolder(holder: ListImagePostViewHolder, position: Int) {
        val imageUri = listImagePost[position]
        holder.apply {
            binding.apply {
                imagePost.setImageURI(imageUri)
            }
        }
    }
}