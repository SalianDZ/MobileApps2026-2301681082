package com.example.spotter.ui // Ако файлът не е в папка ui, изтрий ".ui" накрая!

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.spotter.data.PlaceRepository
import com.example.spotter.model.Place
import kotlinx.coroutines.launch

// 1. Самият ViewModel, който управлява данните
class PlaceViewModel(private val repository: PlaceRepository) : ViewModel() {

    val allPlaces = repository.allPlaces.asLiveData()

    fun insert(place: Place) = viewModelScope.launch {
        repository.insert(place)
    }

    fun update(place: Place) = viewModelScope.launch {
        repository.update(place)
    }

    fun delete(place: Place) = viewModelScope.launch {
        repository.delete(place)
    }
}

// 2. Фабриката, която се грижи ViewModel-ът да получи достъп до базата
class PlaceViewModelFactory(private val repository: PlaceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}