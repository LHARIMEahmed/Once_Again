package com.example.myapplication.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class user(
    val uid: String ,
    val nom: String ,
    val email: String ,
    val dateDeNaissance: String,
    val dateInscription: com.google.firebase.Timestamp,
    val nbrDePublication: Int,
    val image:String="",
    var favorites: MutableList<String> = mutableListOf()
):Parcelable {
    constructor():this("0","","","", Timestamp.now(),0,"", mutableListOf())
}