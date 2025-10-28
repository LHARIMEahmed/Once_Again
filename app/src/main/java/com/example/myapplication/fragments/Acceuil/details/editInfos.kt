package com.example.myapplication.fragments.Acceuil.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.databinding.EditProfileLayoutBinding
import com.example.myapplication.models.user
import com.google.firebase.firestore.FirebaseFirestore

class editInfos: Fragment(R.layout.edit_profile_layout) {

    private val args: editInfosArgs by navArgs()
    private lateinit var binding: EditProfileLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=EditProfileLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = args.user
        binding.nameEt.setText(user.nom)
        binding.EmailEt.setText(user.email)
        binding.dateEt.setText(user.dateDeNaissance)

    }
    private fun updateUserInFirestore(updatedUser: user) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users")

        userRef
            .whereEqualTo("id", updatedUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("nom", updatedUser.nom)
                    document.reference.update("email", updatedUser.email)
                    document.reference.update("dateDeNaissance", updatedUser.dateDeNaissance)
                        .addOnSuccessListener {

                            // Optionally, navigate back to the previous screen after updating
                            requireView().findNavController().navigateUp()
                        }.addOnFailureListener { exception ->

                            // Handle the failure, e.g., display an error message to the user
                        }
                    break // Exit the loop after updating the first matching document
                }
            }
            .addOnFailureListener { exception ->
                // Handle the failure, e.g., display an error message to the user
            }
    }

}