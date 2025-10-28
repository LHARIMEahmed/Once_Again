package com.example.myapplication.fragments.Acceuil.details;

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.ListObjetsAdapter
import com.example.myapplication.databinding.MesFavorisBinding
import com.example.myapplication.models.Objet
import com.example.myapplication.viewmodel.allmodelview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class mesFavoris: Fragment() {
private lateinit var binding: MesFavorisBinding
private lateinit var adapter: ListObjetsAdapter
private lateinit var viewModel: allmodelview
private lateinit var objetsList: MutableList<Objet>
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        }

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View? {
        binding= MesFavorisBinding.inflate(inflater)
        return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(allmodelview::class.java)
        loadAnnonces()
        setupRecyclerView()
        }

private fun setupRecyclerView() {
        adapter = ListObjetsAdapter(requireContext())
        objetsList = mutableListOf()
        binding.annoncesPublier.apply {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = this@mesFavoris.adapter
        }
                }


private fun loadAnnonces() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

// Get a reference to the Firestore instance
        val firestore = FirebaseFirestore.getInstance()

// Reference to the users collection
        val usersRef = firestore.collection("users")

// Query to find the document with the specified user ID
        usersRef.whereEqualTo("uid", userId).get()
                .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                                for (document in documents) {
                                        // Access the favorites field from the document's data
                                        val favorites = document.get("favorites") as? List<String>
                                        // Check if favorites is not null and contains the object ID
                                        if (favorites != null) {
                                                // Load objects from the 'objets' collection based on the IDs in favorites
                                                val objetsList = mutableListOf<Objet>()
                                                val objetsRef = firestore.collection("objets")
                                                favorites.forEach { objectId ->
                                                        objetsRef.whereEqualTo("id", objectId).get()
                                                                .addOnSuccessListener { objQuerySnapshot ->
                                                                        objQuerySnapshot.documents.forEach { objDocument ->
                                                                                // Convert the document to Objet object and add it to the list
                                                                                val objet = objDocument.toObject(Objet::class.java)
                                                                                objet?.let {
                                                                                        objetsList.add(it)
                                                                                }
                                                                        }
                                                                        // Submit the list to the adapter or update UI
                                                                        adapter.submitList(objetsList)
                                                                }
                                                                .addOnFailureListener { e ->
                                                                        // Handle failure to fetch object document
                                                                        Log.e("loadFavoriteObjects", "Error getting object document", e)
                                                                }
                                                }
                                        } else {
                                                // Favorites list is empty, handle accordingly
                                        }
                                }
                        } else {
                                // User document not found, handle accordingly
                        }
                }
                .addOnFailureListener { e ->
                        // Handle failure, log error, etc.
                }


        }
}
