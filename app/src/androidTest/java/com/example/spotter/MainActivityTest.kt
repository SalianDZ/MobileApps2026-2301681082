package com.example.spotter

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    // Тази правило стартира основното Activity (екрана) преди теста
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addPlaceAndVerifyInList() {
        val testPlaceName = "Тестова Локация UI"
        val testPlaceDesc = "Това е автоматизиран UI тест с Espresso"

        // 1. Кликаме на бутона за добавяне
        onView(withId(R.id.fabAddPlace)).perform(click())

        // 2. Въвеждаме име на мястото
        onView(withId(R.id.editTextName))
            .perform(typeText(testPlaceName), closeSoftKeyboard())

        // 3. Въвеждаме описание
        onView(withId(R.id.editTextDescription))
            .perform(typeText(testPlaceDesc), closeSoftKeyboard())

        // 4. Кликаме на бутона за запазване
        onView(withId(R.id.btnSave)).perform(click())

        // Слагаме съвсем малка пауза (500 милисекунди), за да дадем време
        // на базата данни да запази записа и да обнови списъка
        Thread.sleep(500)

        // 5. Проверяваме дали мястото с това име се вижда вече на екрана в списъка
        onView(withText(testPlaceName)).check(matches(isDisplayed()))
    }
}