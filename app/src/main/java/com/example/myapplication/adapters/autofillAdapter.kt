
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class autofillAdapter(context: Context, resource: Int) : ArrayAdapter<String>(context, resource), Filterable {
    private val placesClient: PlacesClient = Places.createClient(context)
    private val resultList: MutableList<String> = ArrayList()

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(position: Int): String {
        return resultList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    resultList.clear()
                    resultList.addAll(getAutoComplete(constraint.toString()))
                    filterResults.values = resultList
                    filterResults.count = resultList.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    private fun getAutoComplete(query: String): List<String> {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        val predictions: MutableList<String> = ArrayList()
        val response = placesClient.findAutocompletePredictions(request).addOnSuccessListener {
            for (prediction: AutocompletePrediction in it.autocompletePredictions) {
                predictions.add(prediction.getFullText(null).toString())
            }
        }
        return predictions
    }
}