package com.example.spotter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spotter.data.AppDatabase
import com.example.spotter.data.PlaceRepository
import com.example.spotter.databinding.FragmentAddPlaceBinding
import com.example.spotter.model.Place

class AddPlaceFragment : Fragment() {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!

    // Инициализираме ViewModel-а чрез нашата фабрика и базата данни
    private val viewModel: PlaceViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = PlaceRepository(database.placeDao())
        PlaceViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Слушател за бутона ЗАПАЗИ
        binding.btnSave.setOnClickListener {
            savePlaceToDatabase()
        }
    }

    private fun savePlaceToDatabase() {
        // 1. Взимаме текста от полетата
        val placeName = binding.editTextName.text.toString().trim()
        val placeDesc = binding.editTextDescription.text.toString().trim()

        // 2. Валидация: Проверяваме дали името не е празно
        if (placeName.isEmpty()) {
            binding.layoutName.error = "Моля, въведете име на мястото"
            return
        }
        binding.layoutName.error = null // Изчистваме грешката, ако всичко е точно

        // 3. Създаваме нов обект Place (координати и снимка са null засега)
        val newPlace = Place(
            name = placeName,
            description = placeDesc
        )

        // 4. Казваме на ViewModel да го запази в базата
        viewModel.insert(newPlace)

        // 5. Показваме малко съобщение за успех
        Toast.makeText(requireContext(), "Успешно запазено!", Toast.LENGTH_SHORT).show()

        // 6. Връщаме се автоматично назад към списъка
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}