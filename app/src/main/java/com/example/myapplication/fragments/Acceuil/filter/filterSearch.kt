package com.example.myapplication.fragments.Acceuil.filter

import android.widget.Filter
import com.example.myapplication.adapters.ListObjetsAdapter
import com.example.myapplication.models.Objet
import java.util.Locale

class filterSearch(private val adapter: ListObjetsAdapter, private val original: List<Objet>) : Filter() {

    private var originalList: List<Objet> = original

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()

        if (!constraint.isNullOrBlank()) {
            val filteredModels = ArrayList<Objet>()
            val filterPattern = constraint.toString().uppercase(Locale.getDefault())
            for (item in originalList) {
                if (item.nomAnnonce.uppercase(Locale.getDefault()).contains(filterPattern) ||
                    item.categories.uppercase(Locale.getDefault()).contains(filterPattern) ||
                    item.description.uppercase(Locale.getDefault()).contains(filterPattern)
                ) {
                    filteredModels.add(item)
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            results.count = originalList.size
            results.values = originalList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        @Suppress("UNCHECKED_CAST")
        adapter.submitList(results.values as List<Objet>)
    }

    fun setOriginalList(list: List<Objet>) {
        originalList = list.toList()
    }
}