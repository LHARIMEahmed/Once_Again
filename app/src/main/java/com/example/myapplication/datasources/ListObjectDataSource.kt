package com.example.myapplication.datasources


import com.example.myapplication.R
import com.example.myapplication.models.ObjectListModel
class listObjectDataSource() {
    fun loadImages():List<ObjectListModel>{
        return listOf <ObjectListModel>(
            ObjectListModel(R.drawable.image1),
            ObjectListModel(R.drawable.image2),
            ObjectListModel(R.drawable.image3),
            ObjectListModel(R.drawable.image4)
            )
    }
}