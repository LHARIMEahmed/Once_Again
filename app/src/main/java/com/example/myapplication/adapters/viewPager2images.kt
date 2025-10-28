package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ViewPagerImageBinding

class viewPager2images:RecyclerView.Adapter<viewPager2images.viewPager2imagesViewHolder> (){

    class viewPager2imagesViewHolder(val binding: ViewPagerImageBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(imagePath: String?) {
            Glide.with(itemView).load(imagePath).into(binding.imageObjetDisplay)
        }

    }
    private val diffCallback = object : DiffUtil.ItemCallback<String>(){
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this,diffCallback)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewPager2imagesViewHolder {
        return viewPager2imagesViewHolder(
            ViewPagerImageBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: viewPager2imagesViewHolder, position: Int) {
        val image=differ.currentList[position]
        holder.bind(image)
    }
}