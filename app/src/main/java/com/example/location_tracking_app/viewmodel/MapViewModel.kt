package com.example.location_tracking_app.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.location_tracking_app.repository.MapRepository
import com.google.android.gms.maps.model.LatLng

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val mapRepository: MapRepository

    init {
        mapRepository = MapRepository(application)
    }


    fun toast(message:String){
        Toast.makeText(getApplication(),message, Toast.LENGTH_SHORT).show()
    }
}