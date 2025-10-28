package com.example.myapplication.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.google.android.material.imageview.ShapeableImageView

class adapterImage (private val imageUris: List<Uri> , private val onRemoveClick: (Uri) -> Unit) : RecyclerView.Adapter<adapterImage.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.imageSelectedLayout)
        val removeIcon: ImageView = itemView.findViewById(R.id.cancelselectedimage)
        fun bind(item: Any) {
            if (item is Uri) {
                // Handle URI
                Glide.with(itemView).load(item).into(imageView)
            } else if (item is String) {
                // Handle URL
                Glide.with(itemView).load(item).into(imageView)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_picked, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item =imageUris[position]
        holder.bind(item)
        holder.removeIcon.setOnClickListener {
            onRemoveClick(imageUris[position])
        }
    }

    override fun getItemCount(): Int = imageUris.size
}