package com.example.myapplication.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Objet(val id :String,val nomAnnonce : String,val description : String,val latitude:Double,val longitude :Double,val categories: String,var etat: String, var disponibilite: String , val images:List<String>,val uid:String,val estDonne : Boolean,var favoris : Boolean):Parcelable {
    constructor():this("0","","",0.0,0.0,"","","",images= emptyList(),"",false,false)
}