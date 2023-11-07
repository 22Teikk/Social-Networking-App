package com.example.chatapp.newsfeed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.chatapp.databinding.ImagePostItemInlistBinding
import com.example.chatapp.databinding.ListImagePostItemBinding
import com.squareup.picasso.Picasso

class ImageInPostAdapter(
    private val imageList: ArrayList<String>,
    private val viewPager2: ViewPager2
) : RecyclerView.Adapter<ImageInPostAdapter.ImageInPostViewHolder>() {

    inner class ImageInPostViewHolder(val binding: ImagePostItemInlistBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageInPostViewHolder {
        return ImageInPostViewHolder(
            ImagePostItemInlistBinding.inflate(LayoutInflater.from(parent.context), parent, false )
        )
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ImageInPostViewHolder, position: Int) {
        val image = imageList[position]
        holder.apply {
            binding.apply {
                Picasso.get().load(image).into(imageInListImagePost)
//                if (position == imageList.size - 1)
//                    viewPager2.post(runnable)
            }
        }
    }

    private val runnable = Runnable {
        imageList.addAll(imageList)
        notifyDataSetChanged()
    }
}