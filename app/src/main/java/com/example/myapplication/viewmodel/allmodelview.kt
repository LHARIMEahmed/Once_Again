package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.Objet
import com.example.myapplication.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class allmodelview : ViewModel() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _objets = MutableStateFlow<Resource<List<Objet>>>(Resource.Unspecified())
    val objets: StateFlow<Resource<List<Objet>>> =_objets
    private val pagingInfo = PagingInfo()
    init {
        fetchData()
    }

     fun fetchData(){
        firestore.collection("objets").limit(pagingInfo.page*10).get().addOnSuccessListener {result ->
            val objetList = result.toObjects(Objet::class.java)
            viewModelScope.launch {
                _objets.emit(Resource.Success(objetList))
            }
            pagingInfo.page++
        }.addOnFailureListener{
            viewModelScope.launch {
                _objets.emit(Resource.Error(it.message.toString()))
            }
        }
    }
}
internal data class PagingInfo(
    var page:Long  =1
)