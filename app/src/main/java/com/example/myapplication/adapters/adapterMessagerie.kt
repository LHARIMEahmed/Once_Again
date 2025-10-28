package com.example.myapplication.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.models.message
import com.example.myapplication.utils
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth

class adapterMessagerie(private val context: Context, private val chatArrayList: ArrayList<message>) : RecyclerView.Adapter<adapterMessagerie.HolderChat>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChat {
        val view = if (viewType == 1) {
            LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false)
        }
        return HolderChat(view)
    }

    override fun getItemCount(): Int {
        return chatArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatArrayList[position].fromUid == FirebaseAuth.getInstance().currentUser?.uid) {
            1
        } else {
            0
        }
    }

    override fun onBindViewHolder(holder: HolderChat, position: Int) {
        val modelChat = chatArrayList[position]
        val message = modelChat.messageContent
        val messageType = modelChat.messageType
        val timestamp = modelChat.timestamp
        val formattedDate = utils.formatDateFromTimestamp(timestamp)
        holder.timeTextView.text = formattedDate
        Log.d("error",messageType)
        if (messageType == "TEXT") {
            holder.messageTextView.visibility = View.VISIBLE
            holder.messageImageView.visibility = View.GONE
            holder.messageTextView.text = message
        } else {
            holder.messageTextView.visibility = View.GONE
            holder.messageImageView.visibility = View.VISIBLE

            try {
                Glide.with(context)
                    .load(message)
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.broken_image)
                    .into(holder.messageImageView)
            } catch (e: Exception) {
                Log.e("AdapterMessagerie", e.message.toString())
            }
        }
    }

    // ViewHolder for chat messages
    inner class HolderChat(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageTextView: TextView = itemView.findViewById(R.id.messageContent)
        var timeTextView: TextView = itemView.findViewById(R.id.Datetv)
        var messageImageView: ShapeableImageView = itemView.findViewById(R.id.MessageImage)
    }
}

// Load messages from Firestore
