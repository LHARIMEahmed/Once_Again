package com.example.myapplication.fragments.Acceuil.donneurInfos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.ListObjetsAdapter
import com.example.myapplication.databinding.DonneurInfosBinding
import com.example.myapplication.models.Objet
import com.example.myapplication.utils
import com.example.myapplication.viewmodel.allmodelview
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


class DonneurInfos : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var binding: DonneurInfosBinding
    private lateinit var adapter: ListObjetsAdapter
    private lateinit var viewModel: allmodelview
    private lateinit var objetsList: MutableList<Objet>
    private val args: DonneurInfosArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DonneurInfosBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = args.sellerId
        viewModel = ViewModelProvider(this).get(allmodelview::class.java)
        loadDonneurInfos(uid)
        loadAnnonces(uid)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = ListObjetsAdapter(requireContext())
        objetsList = mutableListOf()
        binding.annoncesPublier.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DonneurInfos.adapter
        }
    }

    private fun loadDonneurInfos(uid: String) {
        val ref = FirebaseFirestore.getInstance().collection("users").whereEqualTo("uid", uid)
        ref.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("nom")
                    val nbrAnnonce = document.get("nbrDePublication")
                    val datee= document.getTimestamp("dateInscription")
                    val ins = utils.formatDateFromTimes(datee!!.toDate().time)
                    if (name != null) {
                        binding.apply {
                            sellerName.text = name
                            nbrAnn.text=nbrAnnonce.toString()
                            dateMembre.text=ins
                        }
                        break // Exit the loop after finding the first matching document
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    private fun loadAnnonces(uid: String) {
        val ref = FirebaseFirestore.getInstance().collection("objets").whereEqualTo("uid",uid)
        ref.get()
            .addOnSuccessListener { documents ->
                objetsList.clear()
                for (document in documents) {
                    val objet = document.toObject(Objet::class.java)
                    objetsList.add(objet)
                }
                Log.d("h",objetsList.toString())
                adapter.submitList(objetsList)
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }


}