package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object utils {
    val condition= arrayOf(
        "Comme neuf",
        "Bon état",
        "État moyen",
        "À bricoler"
    )

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Radius of the Earth in kilometers
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
    fun chatPath(str1:String,str2:String):String{
        val array = arrayOf(str1,str2)
        array.sort()
        return "${array[0]}_${array[1]}"
    }
    fun getTimeStamp():Long{
        return System.currentTimeMillis()
    }
    fun formatDateFromTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.FRANCE)
        calendar.timeInMillis = timestamp
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:a", Locale.FRANCE)
        return sdf.format(calendar.time)
    }
    fun formatDateFromTimes(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.FRANCE)
        calendar.timeInMillis = timestamp
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
        return sdf.format(calendar.time)
    }
}