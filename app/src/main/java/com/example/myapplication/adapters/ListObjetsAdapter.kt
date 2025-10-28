package com.example.myapplication.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ObjetLayoutBinding
import com.example.myapplication.models.Objet
import com.example.myapplication.utils


class ListObjetsAdapter(private val context: Context) : RecyclerView.Adapter<ListObjetsAdapter.ListObjetsViewHolder>() ,Filterable{

    private var objetsList: List<Objet> = listOf()
    private var objetsListFiltered: List<Objet> = listOf()
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    init {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        currentLatitude = sharedPreferences.getFloat("currentLatitude", 48.8566f).toDouble()
        currentLongitude = sharedPreferences.getFloat("currentLongitude", 2.3522f).toDouble()
        objetsListFiltered=objetsList
    }

    class ListObjetsViewHolder(private val binding: ObjetLayoutBinding, private val currentLatitude: Double, private val currentLongitude: Double) : RecyclerView.ViewHolder(binding.root) {
        fun bind(objet: Objet) {
            binding.apply {
                Glide.with(itemView).load(objet.images[0]).into(ObjectImage)
                titreAnnonces.text = objet.nomAnnonce
                val dist = utils.calculateDistance(currentLatitude, currentLongitude, objet.latitude, objet.longitude)
                distance.text = String.format("%.2f km", dist) // Display the distance
            }
        }
    }

    private var diffCallback = object : DiffUtil.ItemCallback<Objet>() {
        override fun areItemsTheSame(oldItem: Objet, newItem: Objet): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Objet, newItem: Objet): Boolean {
            return oldItem == newItem
        }
    }

    var differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListObjetsViewHolder {
        val binding = ObjetLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListObjetsViewHolder(binding, currentLatitude, currentLongitude)
    }

    override fun getItemCount(): Int {
        return objetsListFiltered.size
    }

    override fun onBindViewHolder(holder: ListObjetsViewHolder, position: Int) {
        val objet =objetsListFiltered[position]
        holder.bind(objet)
        holder.itemView.setOnClickListener {
            onClick?.invoke(objet)
        }
    }

    var onClick: ((Objet) -> Unit)? = null

    fun submitList(list: List<Objet>) {
        objetsList = list
        objetsListFiltered = list // Ensure objetsListFiltered is set to the full list initially
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                Log.d("huu",charString)
                objetsListFiltered = if (charString.isEmpty()) {
                    objetsList
                } else {
                    objetsList.filter {
                        it.nomAnnonce.contains(charString, true) ||
                                it.description.contains(charString, true) ||
                                it.categories.contains(charString, true)
                    }
                }

                val filterResults = FilterResults()

                filterResults.values = objetsListFiltered
                Log.d("list",filterResults.toString())
                return filterResults
            }


            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                if (results?.values != null) {
                    objetsListFiltered = results.values as List<Objet>
                } else {
                    objetsListFiltered = emptyList() // If there are no results, set the filtered list to empty
                }
                notifyDataSetChanged() // Notify the adapter that the dataset has changed
            }
        }
    }
}