package com.example.myapplication.interfaces

import com.example.myapplication.models.Objet

interface OnItemMarkedAsDoneListener {
    fun onItemMarkedAsDone(item: Objet)
}