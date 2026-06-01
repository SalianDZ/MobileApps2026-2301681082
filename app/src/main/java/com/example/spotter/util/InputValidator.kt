package com.example.spotter.util

object InputValidator {

    // Проверява дали името и описанието са попълнени правилно
    fun isValidPlace(name: String, description: String): Boolean {
        if (name.trim().isEmpty()) return false // Името не може да е празно
        if (name.length > 50) return false // Името е прекалено дълго
        if (description.length > 500) return false // Описанието е прекалено дълго
        return true
    }

    // Проверява дали GPS координатите са в реални географски граници
    fun hasValidCoordinates(lat: Double?, lng: Double?): Boolean {
        if (lat == null || lng == null) return false
        if (lat < -90.0 || lat > 90.0) return false // Ширината е от -90 до 90
        if (lng < -180.0 || lng > 180.0) return false // Дължината е от -180 до 180
        return true
    }
}