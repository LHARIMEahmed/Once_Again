package com.example.myapplication.adapters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.chats
import com.example.myapplication.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class chatsAdapter(private val fragment: Fragment, private val chatsArrayList: ArrayList<chats>) :
    RecyclerView.Adapter<chatsAdapter.ChatHolder>() {

    class ChatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameTv: TextView = itemView.findViewById(R.id.nameTv)
        var lastmessageTv: TextView = itemView.findViewById(R.id.lastMessageTv)
        var datetimeview: TextView = itemView.findViewById(R.id.dateTimeTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_chats, parent, false)
        return ChatHolder(view)
    }

    override fun getItemCount(): Int {
        return chatsArrayList.size
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        val chats = chatsArrayList[position]
        LoadLastMessage(chats, holder)
        holder.itemView.setOnClickListener {
            if (chats.toUid != null) {
                val bundle = Bundle().apply {
                    putString("sellerId", chats.toUid)
                }
                fragment.findNavController().navigate(
                    R.id.action_message2_to_chatFragment,
                    bundle
                )
            }
        }
    }

    private fun LoadLastMessage(modelChats: chats, holder: ChatHolder) {
        val chatPath = modelChats.chatPath
        Log.d("LoadReacentMeessage", "loadLastMessage: chatPath: $chatPath")

        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("Messages")

        ref.whereEqualTo("chatPath", chatPath)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("LoadReacentMeessage", "listen:error", e)
                    return@addSnapshotListener
                }

                for (doc in snapshots!!) {
                    val fromUid = doc.getString("fromUid") ?: ""
                    val message = doc.getString("messageContent") ?: ""
                    val messageId = doc.getString("messageId") ?: ""
                    val messageType = doc.getString("messageType") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    val toUid = doc.getString("toUid") ?: ""

                    modelChats.messageType=messageType
                    modelChats.timestamp=timestamp
                    modelChats.messageId=messageId
                    modelChats.fromUid=fromUid
                    modelChats.toUid=toUid


                    val formattedDate = utils.formatDateFromTimestamp(timestamp)
                    holder.datetimeview.text = formattedDate
                    if (messageType == "TEXT") {
                        holder.lastmessageTv.text = message
                    } else {
                        holder.lastmessageTv.text = "sends Attachement"
                    }
                    LoadRecepteurInfos(modelChats, holder)
                }
            }
    }

    private fun LoadRecepteurInfos(modelChats: chats, holder: ChatHolder) {
        val fromUid = modelChats.fromUid
        val toUid = modelChats.toUid
        var recepteur = ""
        recepteur = if(fromUid == FirebaseAuth.getInstance().currentUser?.uid) {
            toUid
        } else {
            fromUid
        }
        Log.d("tag",fromUid)
        Log.d("tag",toUid)
        Log.d("tag",FirebaseAuth.getInstance().currentUser?.uid.toString())
        modelChats.recepteur = recepteur;
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("users").whereEqualTo("uid", recepteur)

        ref.addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                for (doc in snapshots.documents) {
                    val name = doc.getString("nom") ?: ""
                    holder.nameTv.text = name
                }
            }
        }
    }
}
