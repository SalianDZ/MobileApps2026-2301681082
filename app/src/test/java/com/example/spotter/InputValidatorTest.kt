package com.example.spotter

import com.example.spotter.util.InputValidator
import org.junit.Assert.assertEquals
import org.junit.Test

class InputValidatorTest {

    // 1. Тест за правилно въведени данни
    @Test
    fun `valid place details returns true`() {
        val result = InputValidator.isValidPlace("Връх Мусала", "Страхотен преход")
        assertEquals(true, result)
    }

    // 2. Тест за празно име
    @Test
    fun `empty name returns false`() {
        val result = InputValidator.isValidPlace("   ", "Описание")
        assertEquals(false, result)
    }

    // 3. Тест за прекалено дълго име (над 50 символа)
    @Test
    fun `name exceeding 50 characters returns false`() {
        val longName = "A".repeat(51)
        val result = InputValidator.isValidPlace(longName, "Описание")
        assertEquals(false, result)
    }

    // 4. Тест за правилни GPS координати
    @Test
    fun `valid coordinates returns true`() {
        val result = InputValidator.hasValidCoordinates(42.6977, 23.3219)
        assertEquals(true, result)
    }

    // 5. Тест за невалидна географска ширина (над 90 градуса)
    @Test
    fun `invalid latitude returns false`() {
        val result = InputValidator.hasValidCoordinates(100.0, 23.3219)
        assertEquals(false, result)
    }

    // 6. Тест за липсващи координати (null)
    @Test
    fun `null coordinates returns false`() {
        val result = InputValidator.hasValidCoordinates(null, null)
        assertEquals(false, result)
    }
}