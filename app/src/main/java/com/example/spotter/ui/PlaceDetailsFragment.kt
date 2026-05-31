package com.example.spotter.ui

import android.net.Uri
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
import com.example.spotter.databinding.FragmentPlaceDetailsBinding
import com.example.spotter.model.Place
import java.io.File

class PlaceDetailsFragment : Fragment() {

    private var _binding: FragmentPlaceDetailsBinding? = null
    private val binding get() = _binding!!

    // Инициализираме връзката с базата данни
    private val viewModel: PlaceViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = PlaceRepository(database.placeDao())
        PlaceViewModelFactory(repository)
    }

    private var currentPlace: Place? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Взимаме ID-то на мястото, което сме подали при клик (ще го настроим в Адаптера след малко)
        val placeId = arguments?.getInt("placeId") ?: return

        // 2. Намираме мястото от списъка и го показваме
        viewModel.allPlaces.observe(viewLifecycleOwner) { places ->
            val place = places.find { it.id == placeId }
            if (place != null) {
                currentPlace = place
                bindUI(place)
            }
        }

        // 3. Логика за бутона "Изтрий"
        binding.btnDelete.setOnClickListener {
            currentPlace?.let { place ->
                viewModel.delete(place)
                Toast.makeText(requireContext(), "Мястото е изтрито!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Връщаме се назад
            }
        }

        // 4. Логика за бутона "Редактирай"
        binding.btnEdit.setOnClickListener { view ->
            currentPlace?.let { place ->
                val bundle = Bundle().apply {
                    putInt("placeId", place.id) // Подаваме ID-то на екрана за редакция
                }
                // Отваряме екрана
                androidx.navigation.Navigation.findNavController(view).navigate(
                    com.example.spotter.R.id.action_placeDetailsFragment_to_editPlaceFragment,
                    bundle
                )
            }
        }
    }

    // Функция, която пълни визуалните елементи с данни
    private fun bindUI(place: Place) {
        binding.tvDetailName.text = place.name
        binding.tvDetailDescription.text = place.description

        if (place.latitude != null && place.longitude != null) {
            binding.tvDetailCoordinates.text = "Координати: ${place.latitude}, ${place.longitude}"
        } else {
            binding.tvDetailCoordinates.text = "Координати: Няма налични"
        }

        // Зареждане на снимката
        if (!place.imagePath.isNullOrEmpty()) {
            val imgFile = File(place.imagePath)
            if (imgFile.exists()) {
                binding.imagePlaceDetail.setImageURI(Uri.fromFile(imgFile))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}