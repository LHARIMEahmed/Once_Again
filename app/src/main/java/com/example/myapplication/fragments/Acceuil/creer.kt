package com.example.myapplication.fragments.Acceuil

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.adapterImage
import com.example.myapplication.databinding.CreateActivityBinding
import com.example.myapplication.fragments.Acceuil.location.locationPicker
import com.example.myapplication.models.Objet
import com.example.myapplication.utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Collections
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch

class creer : Fragment(R.layout.create_activity) {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var progressDialog: ProgressDialog
    private val imageUris: MutableList<Uri> = mutableListOf()
    private var objets= Firebase.storage.reference
    private var firestore = Firebase.firestore
    private val args: creerArgs by navArgs()
    private val categories = mutableListOf<String>()
    private val dispo = mutableListOf<String>()
    private val etat = mutableListOf<String>()
    private lateinit var binding: CreateActivityBinding
    private var isEdit :Boolean=false
    lateinit var database: DatabaseReference
    private var selectedImageUris: MutableList<Uri> = mutableListOf()
    private lateinit var imagesAdapter: adapterImage
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0


    private val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                val clipData = intent.clipData
                if (clipData != null) {
                    // Multiple images selected
                    val count = clipData.itemCount.coerceAtMost(4 - selectedImageUris.size) // Limite le nombre d'images à 4
                    for (i in 0 until count) {
                        val imageUri = clipData.getItemAt(i).uri
                        selectedImageUris.add(imageUri)
                    }
                } else {
                    // Single image selected
                    intent.data?.let { uri ->
                        selectedImageUris.add(uri)
                    }
                }
                imagesAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= CreateActivityBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val apiKey = getString(R.string.google_play_services_version)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)
        }
        progressDialog = ProgressDialog(requireContext())
        // Initialize RecyclerView and Adapter
        val recyclerViewImages = view.findViewById<RecyclerView>(R.id.imagesRv)
        recyclerViewImages.layoutManager = GridLayoutManager(requireContext(), 4)
        imagesAdapter = adapterImage(selectedImageUris) { uri ->
            selectedImageUris.remove(uri)
            imagesAdapter.notifyDataSetChanged()
        }
        recyclerViewImages.adapter = imagesAdapter

        isEdit=args.isEdit


        if(isEdit){
            val objet = args.objet
            Log.d("f",isEdit.toString())
            view.findViewById<TextView>(R.id.toolbarTitre).text="Modifier Annonce"
            view.findViewById<Button>(R.id.submitButton).text="Mettre A jour"
            loadDetails(objet!!)
        }else{
            view.findViewById<TextView>(R.id.toolbarTitre).text="Creer Annonce"
            view.findViewById<Button>(R.id.submitButton).text="Publier Annonce"
        }

        val importButton = view.findViewById<Button>(R.id.importerPhoto)
        importButton.setOnClickListener {
            if (selectedImageUris.size < 4) {
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                imageLauncher.launch(pickIntent)
            } else {
                Toast.makeText(requireContext(), "You can only select up to 4 images", Toast.LENGTH_SHORT).show()
            }
        }
        parentFragmentManager.setFragmentResultListener(
            locationPicker.REQUEST_KEY,
            this
        ) { _, bundle ->
            val address = bundle.getString(locationPicker.BUNDLE_KEY_ADDRESS)
            latitude = bundle.getDouble(locationPicker.BUNDLE_KEY_LATITUDE)
            Log.d("latitude",latitude.toString());
            longitude=bundle.getDouble(locationPicker.BUNDLE_KEY_LONGITUDE)

            Log.d("CreerFragment", "Received address: $address")
            val locationTextView=view.findViewById<EditText>(R.id.locationTextView)
            locationTextView.setText(address)  // Use setText for EditText, use text for TextView
        }




        val locationPickerActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                data?.let {
                    val latitude = it.getDoubleExtra("latitude", 0.0)
                    val longitude = it.getDoubleExtra("longitude", 0.0)
                    val address = it.getStringExtra("address")
                    Log.d(TAG, "onActivityResult: latitude: $latitude")
                    Log.d(TAG, "onActivityResult: longitude: $longitude")
                    Log.d(TAG, "onActivityResult: address: $address")
                }
            } else {
                Log.d(TAG, "onActivityResult: cancelled")
            }
        }

        val getLocationButton = view.findViewById<Button>(R.id.getLocation)
        getLocationButton.setOnClickListener {
            val locationPickerFragment = locationPicker()

            // Naviguez vers le fragment en utilisant le NavController
            findNavController().navigate(R.id.action_creer_to_locationPicker)
        }

        // Set fixed height for all image views in the array (optional)





        val submitButton = view.findViewById<Button>(R.id.submitButton)
        val dropdowncategories = view.findViewById<AutoCompleteTextView>(R.id.DropDownCate)
        val dropdownetat = view.findViewById<AutoCompleteTextView>(R.id.DropDownEtatO)
        val dropdowndispo = view.findViewById<AutoCompleteTextView>(R.id.DropDownDisponible)
        val adapterEtat = ArrayAdapter(requireContext(),R.layout.row_etat_layout, utils.condition)
        dropdownetat.setAdapter(adapterEtat)
        getCategories {
            if (categories.isNotEmpty()) {

                // Create an ArrayAdapter to bridge the data with the dropdown
                val adapter = ArrayAdapter<String>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, categories)
                dropdowncategories.setAdapter(adapter)
            }
        }
        getDispo {
            if (dispo.isNotEmpty()) {
                val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, dispo)
                dropdowndispo.setAdapter(adapter)
            }
        }
        getEtat {
            if (etat.isNotEmpty()) {
                val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, etat)
                dropdownetat.setAdapter(adapter)
            }
        }

        submitButton.setOnClickListener {
            val verification = verifyData()
            if(!verification){

            }else{
                if(isEdit){
                    updateData()
                }else{
                    saveData();
                }

            }
        }

    }
    private fun loadDetails(objet: Objet){
        requireView().findViewById<AutoCompleteTextView>(R.id.DropDownCate).setText(objet.categories)
        requireView().findViewById<EditText>(R.id.TitreAnnonce).setText(objet.nomAnnonce)
        requireView().findViewById<EditText>(R.id.TitreAnnonce).setText(objet.description)
        requireView().findViewById<AutoCompleteTextView>(R.id.DropDownDisponible).setText(objet.disponibilite)
        requireView().findViewById<AutoCompleteTextView>(R.id.locationTextView).setText(addressFromLatLng(objet.latitude,objet.longitude))
        requireView().findViewById<RecyclerView>(R.id.imagesRv).layoutManager=GridLayoutManager(requireContext(),4)





        imagesAdapter  = adapterImage(objet.images.map { Uri.parse(it) }) { uri ->
            imageUris.remove(uri)
            imagesAdapter.notifyDataSetChanged()
        }
        requireView().findViewById<RecyclerView>(R.id.imagesRv).adapter = imagesAdapter
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
    private fun saveData() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "L'utilisateur n'est pas connecté", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid

        val nomAnnonce = view?.findViewById<EditText>(R.id.TitreAnnonce)?.text.toString().trim()
        val descriptionObjet = view?.findViewById<EditText>(R.id.descriptionObjet)?.text.toString().trim()
        val locationObjets = view?.findViewById<EditText>(R.id.locationTextView)?.text.toString().trim()
        val selectedCategorie = view?.findViewById<AutoCompleteTextView>(R.id.DropDownCate)?.text.toString().trim()
        val selectedEtat = view?.findViewById<AutoCompleteTextView>(R.id.DropDownEtatO)?.text.toString().trim()
        val selectedDispo = view?.findViewById<AutoCompleteTextView>(R.id.DropDownDisponible)?.text.toString().trim()
        // Validation des champs
        if (nomAnnonce.isEmpty() || descriptionObjet.isEmpty() || locationObjets.isEmpty() || selectedImageUris.isEmpty()) {
            Toast.makeText(requireContext(), "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show()
            return
        }

        val imagesByteArrays = getImagesBytesArray()
        val images = Collections.synchronizedList(mutableListOf<String>())
        val latch = CountDownLatch(imagesByteArrays.size)

        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Enregistrement de l'annonce...")
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            show()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                imagesByteArrays.forEach { byteArray ->
                    launch {
                        try {
                            val id = UUID.randomUUID().toString()
                            val imageStorage = objets.child("objets/images/$id")
                            val result = imageStorage.putBytes(byteArray).await()
                            val downloadUrl = result.storage.downloadUrl.await().toString()
                            synchronized(images) {
                                images += downloadUrl
                            }
                            Log.d("ImageUploadSuccess", "Image téléchargée avec succès, URL: $downloadUrl")
                        } catch (e: Exception) {
                            Log.e("ImageUploadError", "Échec du téléchargement de l'image", e)
                        } finally {
                            latch.countDown()
                        }
                    }
                }

                latch.await()

                Log.d("ImageUploadComplete", "Toutes les images ont été téléchargées, liste des images: $images")

                val objet = Objet(
                    UUID.randomUUID().toString(),
                    nomAnnonce,
                    descriptionObjet,
                    latitude,
                    longitude,
                    selectedCategorie,
                    selectedEtat,
                    selectedDispo,
                    images,
                    uid,
                    false,
                    false
                )

                if (images.isNotEmpty()) {
                    firestore.collection("objets").add(objet)
                        .addOnSuccessListener {
                            Log.d("FirestoreSuccess", "Objet enregistré dans Firestore")
                            Toast.makeText(requireContext(), "Annonce enregistrée avec succès", Toast.LENGTH_SHORT).show()
                            setFragmentResult("productAdded", Bundle()) // Définir le résultat
                            findNavController().navigate(R.id.action_creer_to_acceuil2)
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Échec de l'enregistrement de l'objet dans Firestore", e)
                            Toast.makeText(requireContext(), "Erreur lors de l'enregistrement de l'annonce", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("SaveDataError", "Aucune image téléchargée, l'objet n'a pas été enregistré dans Firestore")
                    Toast.makeText(requireContext(), "Aucune image téléchargée, l'annonce n'a pas été enregistrée", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SaveDataError", "Échec de l'enregistrement des données", e)
                Toast.makeText(requireContext(), "Erreur lors de l'enregistrement de l'annonce", Toast.LENGTH_SHORT).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }




    private fun getImagesBytesArray():List<ByteArray>{
        val imagesByteArrays = mutableListOf<ByteArray>()
        selectedImageUris.forEach{
            val stream  = ByteArrayOutputStream()
            val imagebmp = MediaStore.Images.Media.getBitmap(requireContext().contentResolver,it)
            if(imagebmp.compress(Bitmap.CompressFormat.JPEG,100,stream)){
                imagesByteArrays.add(stream.toByteArray())
            }
        }
        return imagesByteArrays;
    }
    private fun verifyData():Boolean{
        if(R.id.TitreAnnonce.toString().trim().isEmpty()){
            Toast.makeText(requireContext(),"entrez le titre",Toast.LENGTH_SHORT).show()
            return false;
        }
        if(R.id.descriptionObjet.toString().trim().isEmpty()){
            Toast.makeText(requireContext(),"entrez la description",Toast.LENGTH_SHORT).show()
            return false;
        }
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(requireContext(), "At least one image is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if(R.id.locationTextView.toString().trim().isEmpty()){
            Toast.makeText(requireContext(),"entrez la location",Toast.LENGTH_SHORT).show()
            return false;
        }
        return true
    }
    private fun getEtat(callback: () -> Unit){
        database= FirebaseDatabase.getInstance().getReference("etet")

        database.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                etat.clear()
                if(snapshot.exists()){
                    for(dispoSnapshot in snapshot.children){
                        val dispoNom = dispoSnapshot.child("nom").getValue(String::class.java)
                        dispoNom?.let { etat.add(it) }
                    }
                }
                callback()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read data", error.toException())
            }

        })
    }


    private fun getDispo(callback: () -> Unit) {
        // Get a reference to the Firestore instance
        val firestore = FirebaseFirestore.getInstance()

        // Get a reference to the "disponibilite" collection
        val dispoCollection = firestore.collection("disponibilite")

        // Fetch the documents within the "disponibilite" collection
        dispoCollection.get()
            .addOnSuccessListener { documents ->
                // Clear the list to avoid duplicates if this method is called multiple times
                dispo.clear()

                // Check if there are any documents in the collection
                if (!documents.isEmpty) {
                    // Iterate through each document in the collection
                    for (document in documents) {
                        // Get the availability name from the document
                        val dispoNom = document.getString("nom")
                        // Add the availability name to the list if it's not null
                        dispoNom?.let { dispo.add(it) }
                    }
                }
                callback() // Call the callback function after availabilities are loaded
            }
            .addOnFailureListener { exception ->
                // Log the error if the Firestore read operation fails
                Log.e("Firestore", "Failed to read data", exception)
            }
    }


    private fun getCategories(callback: () -> Unit) {
        // Get a reference to the Firestore instance
        val firestore = FirebaseFirestore.getInstance()

        // Get a reference to the "categories" collection
        val categoriesCollection = firestore.collection("categories")

        // Fetch the documents within the "categories" collection
        categoriesCollection.get()
            .addOnSuccessListener { documents ->
                // Clear the list to avoid duplicates if this method is called multiple times
                categories.clear()

                // Check if there are any documents in the collection
                if (!documents.isEmpty) {
                    // Iterate through each document in the collection
                    for (document in documents) {
                        // Get the category name from the document
                        val categoryName = document.getString("nom")
                        // Add the category name to the list if it's not null
                        categoryName?.let { categories.add(it) }
                    }
                }
                callback() // Call the callback function after categories are loaded
            }
            .addOnFailureListener { exception ->
                // Log the error if the Firestore read operation fails
                Log.e("Firestore", "Failed to read data", exception)
            }
    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation(view: View) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                getAddressFromLocation(location.latitude, location.longitude, view)
            } else {
                Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double, view: View) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                val addressString = "${address.thoroughfare}, ${address.locality}, ${address.countryName}"
                val locationEditText = view.findViewById<EditText>(R.id.locationTextView)
                locationEditText.setText(addressString)
            } else {
                Toast.makeText(requireContext(), "Address not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Geocoder failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation(requireView())
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateData(){
        progressDialog.setMessage("mise a jour ...")
        progressDialog.show()

        val user = Firebase.auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }

        val uid = user.uid

        val nomAnnonce = view?.findViewById<EditText>(R.id.TitreAnnonce)?.text.toString().trim()
        val descriptionObjet = view?.findViewById<EditText>(R.id.descriptionObjet)?.text.toString().trim()
        val locationObjets = view?.findViewById<EditText>(R.id.locationTextView)?.text.toString().trim()
        val selectedCategorie = view?.findViewById<AutoCompleteTextView>(R.id.DropDownCate)?.text.toString().trim()
        val selectedEtat =view?.findViewById<AutoCompleteTextView>(R.id.DropDownEtat)?.text.toString().trim()
        val selectedDispo = view?.findViewById<AutoCompleteTextView>(R.id.DropDownDisponible)?.text.toString().trim()

        // Validation des champs
        if (nomAnnonce.isEmpty() || descriptionObjet.isEmpty() || locationObjets.isEmpty() || selectedImageUris.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }

        val imagesByteArrays = getImagesBytesArray()
        val images = Collections.synchronizedList(mutableListOf<String>())
        val latch = CountDownLatch(imagesByteArrays.size)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                imagesByteArrays.forEach { byteArray ->
                    launch {
                        try {
                            val id = UUID.randomUUID().toString()
                            val imageStorage = objets.child("objets/images/$id")
                            val result = imageStorage.putBytes(byteArray).await()
                            val downloadUrl = result.storage.downloadUrl.await().toString()
                            synchronized(images) {
                                images += downloadUrl
                            }
                            Log.d("ImageUploadSuccess", "Uploaded image and got URL: $downloadUrl")
                        } catch (e: Exception) {
                            Log.e("ImageUploadError", "Failed to upload image", e)
                        } finally {
                            latch.countDown()
                        }
                    }
                }

                latch.await()

                Log.d("ImageUploadComplete", "All images uploaded, images list: $images")
                val objet = args.objet
                val updatedObjet = Objet(
                    objet!!.id, // Use the existing objetId to update the product
                    nomAnnonce,
                    descriptionObjet,
                    latitude,
                    longitude,
                    selectedCategorie,
                    selectedEtat,
                    selectedDispo,
                    images,
                    uid,
                    objet.estDonne,
                    objet.favoris
                )

                firestore.collection("objets").document(objet.id)
                    .set(updatedObjet) // Use set() instead of add() to update the existing product
                    .addOnSuccessListener {
                        Log.d("FirestoreSuccess", "Object updated in Firestore")
                        progressDialog.dismiss()
                        // Show success message or perform any other action
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Failed to update object in Firestore", e)
                        progressDialog.dismiss()
                        // Show error message or perform any other action
                    }
            } catch (e: Exception) {
                Log.e("UpdateDataError", "Failed to update data", e)
                progressDialog.dismiss()
                // Show error message or perform any other action
            }
        }
    }


    }


