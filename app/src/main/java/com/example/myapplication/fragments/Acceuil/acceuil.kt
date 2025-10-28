package com.example.myapplication.fragments.Acceuil

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.adapters.ListObjetsAdapter
import com.example.myapplication.databinding.ActivityListObjectsBinding
import com.example.myapplication.fragments.Acceuil.location.locationPicker
import com.example.myapplication.models.Objet
import com.example.myapplication.util.Resource
import com.example.myapplication.viewmodel.allmodelview
import com.google.android.material.slider.Slider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class acceuil : Fragment(R.layout.activity_list_objects) {
    private lateinit var allAdapter: ListObjetsAdapter
    private lateinit var binding: ActivityListObjectsBinding
    private lateinit var viewModel: allmodelview
    private val selectedCategoryIndices = mutableListOf<Int>() // Modifier cette ligne pour utiliser des indices entiers au lieu de chaînes
    private val categories = mutableListOf<String>()
    private var currentLatitude = 48.8566 // Default latitude (Paris)
    private var currentLongitude = 2.3522 // Default longitude (Paris)
    private var currentAddress = "Paris, France" // Default address
    // Flag to check if location is picked

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityListObjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(allmodelview::class.java)

        if (hasLocationPermission()) {
            // Permission is granted, display the current address
            displayCurrentAddress()
        }
        parentFragmentManager.setFragmentResultListener("productAdded", this) { _, _ ->
            // Rafraîchir la liste des produits
            observeData()
        }
        parentFragmentManager.setFragmentResultListener("prductGiven", this) { _, _ ->
            // Rafraîchir la liste des produits
            observeData()
        }

        binding.searchObject.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }



            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim() // Get the search query from the input field

                // Apply the filter on the adapter with the current search query
                allAdapter.filter.filter(query)
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })
        var selectedDistance = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getInt("selectedDistance",15)
        binding.distancefilter.text="$selectedDistance Km"
        binding.distancefilter.setOnClickListener { view ->
            // Inflate the custom layout for the pop-up menu
            val popupView = layoutInflater.inflate(R.layout.slider_filter_layout, null)

            // Create a PopupWindow with the inflated layout
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))

            // Show the pop-up menu
            popupWindow.isFocusable = true
            popupWindow.isOutsideTouchable = true

            // Show the pop-up window at a specific location relative to the button
            popupWindow.showAsDropDown(view)

            // Find views and set listeners
            val slider = popupView.findViewById<Slider>(R.id.distanceSlider)
            val applyButton = popupView.findViewById<Button>(R.id.applyButton)

            // Initialize with default value

            // Set initial text for the distanceTextView
            slider.value = selectedDistance.toFloat()

            // Set listener for the slider value changes
            slider.addOnChangeListener { _, value, _ ->
                // Update the selected distance value
                selectedDistance = value.toInt()
                // Update the text of the distanceTextView dynamically
                slider.value = selectedDistance.toFloat()
                binding.distancefilter.text= "$selectedDistance Km"
            }

            binding.categorieFilter.setOnClickListener{
                showCategorieDialog()
            }

            // Set listener for the apply button
            applyButton.setOnClickListener {
                // Save the selected value to SharedPreferences or any other storage mechanism
                // For example, you can use SharedPreferences to save the value
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putInt("selectedDistance", selectedDistance)
                editor.apply()

                // Refresh the data with the new distance filter
                observeData()

                // Dismiss the pop-up window when done
                popupWindow.dismiss()
            }
        }


        binding.locationTv.setOnClickListener {
            val locationPickerFragment = locationPicker()

            // Navigate to the fragment using the NavController
            findNavController().navigate(R.id.action_acceuil2_to_locationPicker)
        }

        parentFragmentManager.setFragmentResultListener(
            locationPicker.REQUEST_KEY,
            this
        ) { _, bundle ->
            currentAddress = bundle.getString(locationPicker.BUNDLE_KEY_ADDRESS).toString()
            currentLatitude = bundle.getDouble(locationPicker.BUNDLE_KEY_LATITUDE)
            currentLongitude = bundle.getDouble(locationPicker.BUNDLE_KEY_LONGITUDE)


            Log.d("CreerFragment", "Received address: $currentAddress")

            updateSharedPrefs(currentLatitude,currentLongitude,currentAddress)

            binding.locationTv.text = currentAddress
            observeData() // Refresh the data when a new location is picked
        }

        setupAllRecyclerView()
        observeData()
    }

    private fun setupAllRecyclerView() {
        allAdapter = ListObjetsAdapter(requireContext()).apply {
            onClick = { objet ->
                val bundle = Bundle().apply { putParcelable("objet", objet) }
                findNavController().navigate(R.id.action_acceuil2_to_objetsDetails, bundle)
            }
        }


        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerMain.apply {
            this.layoutManager = layoutManager
            adapter = allAdapter
        }
    }
    private fun showCategorieDialog() {
        getCategories {
            if (categories.isNotEmpty()) {
                val selectedCategories = BooleanArray(categories.size) { false }

                // Set the selection state for previously selected categories
                for (index in selectedCategoryIndices.indices) {
                    val categoryIndex = selectedCategoryIndices[index]
                    selectedCategories[categoryIndex] = true
                }

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Select Categories")
                    .setMultiChoiceItems(categories.toTypedArray(), selectedCategories) { _, which, isChecked ->
                        if (isChecked) {
                            selectedCategoryIndices.add(which)
                        } else {
                            selectedCategoryIndices.remove(which)
                        }
                    }
                    .setPositiveButton("Ok") { dialog, _ ->
                        // Store the selected categories indices

                        // Update the text of your TextView based on the selected categories
                        val selectedCategoriesList = selectedCategoryIndices.map { categories[it] }
                        val selectedCategoriesText = if (selectedCategoriesList.size == 1) {
                            // Only one category selected, display its name
                            selectedCategoriesList[0]
                        } else if(selectedCategoriesList.size>1){
                            // Multiple categories selected, concatenate the names with "+"
                            "${selectedCategoriesList[0]} + ${selectedCategoriesList.size - 1}"
                        } else{
                            "Categories"
                        }
                        // Set the updated text to your TextView
                        binding.colorselected.text = selectedCategoriesText

                        // Change the color of categorieFilter if categories are selected
                        if (selectedCategoryIndices.isNotEmpty()) {
                            binding.categorieFilter.setBackgroundColor(Color.LTGRAY) // Change to desired color
                        } else {
                            binding.categorieFilter.setBackgroundColor(Color.WHITE) // Change to initial color
                        }
                        filterObjectsAndUpdateAdapter()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // Handle cancellation

                        // Reset the color of categorieFilter to initial co
                    }
                    .setNeutralButton("Clear All") { dialog, _ ->
                        // Uncheck all the selected categories
                        val listView = (dialog as AlertDialog).listView
                        for (i in 0 until listView.count) {
                            listView.setItemChecked(i, false)
                        }

                        // Clear the selected category indices
                        selectedCategoryIndices.clear()

                        // Reset the color of categorieFilter to initial color
                        binding.categorieFilter.setBackgroundColor(Color.WHITE)

                        // Update the text of the TextView
                        binding.colorselected.text = "Categorie"
                        observeData()
                    }
                    .show()
            }
        }
    }
    private fun filterObjectsAndUpdateAdapter() {
        lifecycleScope.launch {
            viewModel.objets.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        val selectedDistance = sharedPreferences.getInt("selectedDistance", 15)


                        currentLatitude = sharedPreferences.getFloat("currentLatitude", 48.8566f).toDouble()
                        currentLongitude = sharedPreferences.getFloat("currentLongitude", 2.3522f).toDouble()
                        Log.d("latitude", currentLatitude.toString())// default to 15 if not found
                        val filteredObjects = resource.data?.let { filterObjectsWithinDistance(it, currentLatitude, currentLongitude, selectedDistance) }
                        Log.d("hello",filteredObjects.toString())
                        val filteredObjects2 = filteredObjects!!.filter { objet ->
                            selectedCategoryIndices.any { selectedCategoryIndex ->
                                val selectedCategory = categories[selectedCategoryIndex]
                                objet.categories.contains(selectedCategory)
                            }
                        }
                        allAdapter.submitList(filteredObjects2)
                        // Mettre à jour les valeurs de latitude et de longitude ici

                    }
                    else -> {
                        // Handle error, log it if necessary
                    }
                }
            }
        }
        // Filtrer la liste d'objets en fonction des catégories sélectionnées


        // Mettre à jour l'adaptateur RecyclerView avec la liste d'objets filtrés
    }


    private fun observeData() {
        val user = "GdLWjB8spdgQfQjhDCe6dZf04FF2"
        lifecycleScope.launch {
            viewModel.objets.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        val selectedDistance = sharedPreferences.getInt("selectedDistance", 15)

                        val filteredByEstDonne = resource.data?.filter { !it.estDonne }




                        currentLatitude = sharedPreferences.getFloat("currentLatitude", 48.8566f).toDouble()
                        currentLongitude = sharedPreferences.getFloat("currentLongitude", 2.3522f).toDouble()
                        Log.d("latitude", currentLatitude.toString())// default to 15 if not found
                        val filteredObjects = filteredByEstDonne?.let { filterObjectsWithinDistance(it, currentLatitude, currentLongitude, selectedDistance) }
                        Log.d("hello",filteredObjects.toString())
                        allAdapter.submitList(filteredObjects!!)
                        // Mettre à jour les valeurs de latitude et de longitude ici

                    }
                    else -> {
                        // Handle error, log it if necessary
                    }
                }
            }
        }
    }

    private fun filterObjectsWithinDistance(objects: List<Objet>, currentLatitude: Double, currentLongitude: Double, selectedDistance: Int): List<Objet> {
        return objects.filter { obj ->
            val distance = calculateDistance(currentLatitude, currentLongitude, obj.latitude, obj.longitude)
            distance <= selectedDistance
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Radius of the Earth in kilometers
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c // Distance in kilometers
    }

    override fun onResume() {
        super.onResume()
        val mainActivity = activity as? MainActivity
        observeData()
        mainActivity?.showBottomNav()
    }
    private fun updateSharedPrefs(latitude: Double, longitude: Double,location:String) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("currentLatitude", latitude.toFloat())
        editor.putFloat("currentLongitude", longitude.toFloat())
        editor.putString("currentLocation",location)
        editor.apply()
    }
    private fun displayCurrentAddress() {
        // Use SharedPreferences to check if latitude and longitude are set
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLocationSet = sharedPreferences.contains("currentLatitude") && sharedPreferences.contains("currentLongitude")

        if (isLocationSet) {
            // Latitude and longitude are set, retrieve the address
            currentLatitude = sharedPreferences.getFloat("currentLatitude", 0f).toDouble()
            currentLongitude = sharedPreferences.getFloat("currentLongitude", 0f).toDouble()
            currentAddress= sharedPreferences.getString("currentAddress",addressFromLatLng(currentLatitude,currentLongitude))
                .toString()
            // Update locationTextView with current address
            binding.locationTv.text = currentAddress
        } else {
            // Latitude and longitude are not set, show default text or handle accordingly
            binding.locationTv.text = "Location not available"
        }
    }

    private fun hasLocationPermission(): Boolean {
        // Check if location permission is granted
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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


}





