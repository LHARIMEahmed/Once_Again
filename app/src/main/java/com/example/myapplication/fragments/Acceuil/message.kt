package com.example.myapplication.fragments.Acceuil

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.adapters.chatsAdapter
import com.example.myapplication.databinding.MessageBinding
import com.example.myapplication.models.chats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class message : Fragment(R.layout.message) {
    private lateinit var binding:MessageBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mcontext: Context
    private lateinit var chatsArrayList: ArrayList<chats>
    private lateinit var chatsAdapter: chatsAdapter

    override fun onAttach(context: Context) {
        mcontext=context
        super.onAttach(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =MessageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth=FirebaseAuth.getInstance()
        loadChats()
    }
    private fun loadChats(){
        chatsArrayList = ArrayList()
        val currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid
        Log.d("id",currentUserUid)
        if (currentUserUid != null) {
            val db = FirebaseFirestore.getInstance()
            val messagesRef = db.collection("Messages")

            messagesRef.addSnapshotListener { snapshots, e ->
                if (e != null) {

                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    chatsArrayList.clear()
                    for (doc in snapshots.documents) {
                        val chatPath = doc.getString("chatPath") ?: ""
                        if (chatPath.contains(currentUserUid)) {


                            val modelChats = chats()
                            modelChats.chatPath = chatPath
                            if(!chatsArrayList.contains(modelChats)){
                                chatsArrayList.add(modelChats)
                            }

                        } else {

                        }
                    }
                    Log.d("array",chatsArrayList.toString())
                    chatsAdapter = chatsAdapter(this, chatsArrayList)
                    binding.chatsRv.adapter = chatsAdapter
                }
            }
        }
    }
    private fun sort() {
        // Delay of 1 second before sorting the list
        Handler().postDelayed({
            // Sort chatsArrayList
            chatsArrayList.sortWith { model1: chats, model2: chats ->
                model2.timestamp.compareTo(model1.timestamp)
            }

            // Notify changes after sorting
            chatsAdapter.notifyDataSetChanged()
        }, 1000) // Delay of 1000 milliseconds (1 second)
    }

}