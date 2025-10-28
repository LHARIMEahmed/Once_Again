package com.example.myapplication.fragments.Acceuil.details

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.adapters.viewPager2images
import com.example.myapplication.databinding.FragmentDetailsBinding
import com.example.myapplication.models.Objet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class objetsDetails:Fragment() {
    private val args: objetsDetailsArgs by navArgs()
    private lateinit var binding: FragmentDetailsBinding
    private val viewPagerAdapter by lazy { viewPager2images() }



    private lateinit var objet :Objet
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        objet = args.objet!!
        setupViewPager()
        val mainActivity = activity as? MainActivity
        mainActivity?.hideBottomNav()
        binding.apply {
            detailObjetNom.text=objet.nomAnnonce;
            detailObjetCategorie.text ="${objet.categories} - ${objet.etat}"
            detailObjetDescription .text =objet.description
            dispo.text=objet.disponibilite
            locationObj.text=addressFromLatLng(objet.latitude,objet.longitude)
            loadDonneurInfos(objet.uid)
        }
        if(objet.uid == FirebaseAuth.getInstance().currentUser!!.uid){
            binding.delete.visibility=View.VISIBLE
            binding.edit.visibility=View.VISIBLE
        }else{
            binding.delete.visibility=View.GONE
            binding.edit.visibility=View.GONE
        }

        binding.edit.setOnClickListener {
            showEditDialog()
        }
        binding.delete.setOnClickListener{
            val firestore = FirebaseFirestore.getInstance()

            // Reference to the collection
            val collectionRef = firestore.collection("objets")

            // Query to find the document with the specified objetId
            collectionRef.whereEqualTo("id", objet.id).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            // Delete the document
                            collectionRef.document(document.id).delete()
                                .addOnSuccessListener {
                                    setFragmentResult("prductGiven", Bundle())
                                    findNavController().navigate(R.id.action_objetsDetails_to_acceuil2)
                                    // Document deleted successfully
                                    // Navigate back to the previous screen or perform any other action
                                }
                                .addOnFailureListener { e ->
                                    // Handle the failure to delete the document
                                }
                        }
                    } else {
                        // Document not found
                    }
                }
                .addOnFailureListener { e ->
                    // Handle the failure to query Firestore
                }
        }
        if(objet.favoris==true){
            binding.faveIcon.setImageResource(R.drawable.filled_heart_icon)
        }
        viewPagerAdapter.differ.submitList(objet.images)
        binding.imageClose.setOnClickListener{
            setFragmentResult("prductGiven", Bundle())
            findNavController().navigate(R.id.action_objetsDetails_to_acceuil2)
        }
        val shareIcon: ImageView = view.findViewById(R.id.share_icon)

        val parentFragment = parentFragment

        // Vérifiez si le fragment parent n'est pas nul et implémente l'interface OnItemMarkedAsDoneListener


        shareIcon.setOnClickListener {
            shareProduct()
        }
        binding.faveIcon.setOnClickListener{
            FavoriteFun()
        }
        binding.sellerCV.setOnClickListener {
            val uid = objet.uid
            Log.d("objetsDetails", "Navigating with UID: $uid")
            val bundle = Bundle().apply {
                putString("sellerId", uid)
            }
            findNavController().navigate(R.id.action_objetsDetails_to_DonneurInfos, bundle)
        }
        binding.buttonContacter.setOnClickListener{
            val uid = objet.uid
            Log.d("objetsDetails", "Navigating with UID: $uid")
            val bundle = Bundle().apply {
                putString("sellerId", uid)
            }
            findNavController().navigate(R.id.action_objetsDetails_to_chatFragment,bundle)
        }
    }

    private fun setupViewPager() {
        binding.apply {
            viewPagerImageObjet.adapter= viewPagerAdapter
        }
    }
    private fun FavoriteFun(){
        val firestore = FirebaseFirestore.getInstance()

        // Reference to the collection
        val collectionRef = firestore.collection("users")

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val itemId = objet.id



        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        usersCollection.whereEqualTo("uid", userId).get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                for (document in documents) {
                    val favorites = document.get("favorites") as? MutableList<String> ?: mutableListOf()

                    // Check if the item ID exists in the favorites list
                    val itemIndex = favorites.indexOfFirst { it == itemId }

                    if (itemIndex != -1) {
                        binding.faveIcon.setImageResource(R.drawable.heart_ic)
                        favorites.removeAt(itemIndex)

                    } else {
                        binding.faveIcon.setImageResource(R.drawable.filled_heart_icon)
                        favorites.add(itemId)
                    }

                    // Update the favorites list in the user's document
                    usersCollection.document(document.id).update("favorites", favorites)
                        .addOnSuccessListener {

                        }.addOnFailureListener { exception ->
                            // Handle the failure to update Firestore data
                        }
                }
            } else {
                // Handle the case where no document matches the user's UID
            }
        }.addOnFailureListener { exception ->
            // Handle the failure to query Firestore
        }





        collectionRef.whereEqualTo("id", objet.id).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        // Update the "dinner" field to true
                        collectionRef.document(document.id).update("favoris", !objet.favoris)
                            .addOnSuccessListener {
                                objet.favoris=!objet.favoris
                                if(objet.favoris==true){
                                    binding.faveIcon.setImageResource(R.drawable.filled_heart_icon)
                                }else{
                                    binding.faveIcon.setImageResource(R.drawable.heart_ic)
                                }

                            }
                            .addOnFailureListener { e ->
                                // Update failed
                            }
                    }
                } else {
                }
            }
            .addOnFailureListener { e ->
            }




    }
    private fun shareProduct() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this product: [Product URL or details]")
            type = "text/plain"
        }

        val chooser = Intent.createChooser(shareIntent, "Share Product via")
        startActivity(chooser)
    }
    private fun loadDonneurInfos(uid : String){
        val ref = FirebaseFirestore.getInstance().collection("users").whereEqualTo("uid", uid)
        ref.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Replace "nameField" with the name of the field in your documents that stores the name
                    val name = document.getString("nom")

                    if (name != null) {
                        // Update UI with the user's name
                        binding.apply {
                            sellerName.text =name

                        }
                        break; // Exit the loop after finding the first matching document
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }
    private fun showEditDialog(){
        val popupMenu=PopupMenu(requireContext(),binding.toolbarDT)

        popupMenu.menu.add(Menu.NONE,0,0,"Edit")
        popupMenu.menu.add(Menu.NONE,1,1,"Marquer Comme Donnee")

        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->

            val itemId = menuItem.itemId
            if (itemId == 0) {
                val bundle = Bundle().apply { putParcelable("objet", objet)
                    putBoolean("isEdit", true)}
                findNavController().navigate(R.id.action_objetsDetails_to_creer, bundle)
                true // Return true to indicate that the menu item click is consumed
            } else  {
                markAsDonee()
                true // Return true to indicate that the menu item click is consumed
            }
        }
    }

    private fun markAsDonee() {
        // Get a reference to the Firestore instance
        val firestore = FirebaseFirestore.getInstance()

        // Reference to the collection
        val collectionRef = firestore.collection("objets")

        // Query to find the document with the specified objetId
        collectionRef.whereEqualTo("id", objet.id).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        // Update the "dinner" field to true
                        collectionRef.document(document.id).update("estDonne", true)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener { e ->
                                // Update failed
                            }
                    }
                } else {
                }
            }
            .addOnFailureListener { e ->
            }
    }
    private fun addressFromLatLng(lat : Double,long :Double) :String{
        val geocoder = Geocoder(requireContext())
        try {
            val addressList = geocoder.getFromLocation(lat, long, 1)
            val address = addressList?.get(0)
            var selectedAddress = address?.getAddressLine(0) ?: ""
            return selectedAddress
        } catch (e: Exception) {
            Log.e("locati","addressFromLatLng: ")
        }
        return ""
    }

}
