package com.example.spotter.data
import com.example.spotter.model.Place
import kotlinx.coroutines.flow.Flow

class PlaceRepository(private val placeDao: PlaceDao) {

    val allPlaces: Flow<List<Place>> = placeDao.getAllPlaces()

    suspend fun insert(place: Place) {
        placeDao.insertPlace(place)
    }

    suspend fun update(place: Place) {
        placeDao.updatePlace(place)
    }

    suspend fun delete(place: Place) {
        placeDao.deletePlace(place)
    }
}