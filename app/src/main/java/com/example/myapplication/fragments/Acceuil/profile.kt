package com.example.myapplication.fragments.Profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.ProfileActivityBinding
import com.example.myapplication.first_page
import com.example.myapplication.models.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class profile : Fragment(R.layout.profile_activity) {
    private lateinit var binding: ProfileActivityBinding
    //Context for this fragment class
    private lateinit var mContext: Context
    //Firebase Auth for auth related tasks
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onAttach(context: Context) {
//get and init the context for this fragment class
        mContext = context
        super.onAttach(context)
    }
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
// Inflate the layout for this fragment
            binding =ProfileActivityBinding.inflate(layoutInflater, container,  false)

            return binding.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.logoutCv.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), first_page::class.java))
            activity?.finishAffinity()
        }
        loadInfos(firebaseAuth.currentUser!!.uid)

        binding.editProfileCv.setOnClickListener{
            loadAndNavigateToEditProfile()
        }
        binding.FavoriteCv.setOnClickListener(){
            findNavController().navigate(R.id.action_profile_to_mesFavoris)
        }
    }
    private fun loadInfos(uid:String){
        val ref = FirebaseFirestore.getInstance().collection("users").whereEqualTo("uid", uid)
        ref.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Replace "nameField" with the name of the field in your documents that stores the name
                    val name = document.getString("nom")
                    val dateNaissance = document.get("dateNaissance")
                    if (name != null) {
                        // Update UI with the user's name
                        binding.apply {
                            nameTv.text=name
                            emailTv.text=FirebaseAuth.getInstance().currentUser!!.email
                            if(dateNaissance!=null){
                                dateTv.text=dateNaissance.toString()
                            }
                        }
                        break; // Exit the loop after finding the first matching document
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }
    private fun loadAndNavigateToEditProfile() {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            val ref = FirebaseFirestore.getInstance().collection("users").whereEqualTo("uid", uid)
            ref.get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        for (document in querySnapshot) {
                            val userData = document.toObject(user::class.java)
                            // Create a bundle to pass user data as arguments to the fragment
                            val bundle = Bundle().apply {
                                putParcelable("user", userData)
                            }
                            // Navigate to edit profile fragment with user data as arguments
                            findNavController().navigate(R.id.action_profile_to_editInfos, bundle)
                            return@addOnSuccessListener // Exit loop after navigating once
                        }
                    } else {
                        Log.d("TAG", "No documents found with uid: $uid")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        }
    }
}