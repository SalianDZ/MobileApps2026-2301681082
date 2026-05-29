package com.example.spotter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.spotter.data.AppDatabase
import com.example.spotter.data.PlaceRepository
import com.example.spotter.databinding.FragmentAddPlaceBinding
import com.example.spotter.model.Place
import com.example.spotter.ui.PlaceViewModel
import com.example.spotter.ui.PlaceViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AddPlaceFragment : Fragment() {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaceViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = PlaceRepository(database.placeDao())
        PlaceViewModelFactory(repository)
    }

    // 1. Клиентът на Google, който отговаря за локацията
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 2. Тук ще пазим координатите, преди да ги пратим в базата
    private var currentLat: Double? = null
    private var currentLng: Double? = null

    // 3. Механизъм за искане на права (Permissions)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocation() // Ако потребителят цъкне "Allow", взимаме локацията
        } else {
            Toast.makeText(requireContext(), "Нужно е разрешение за локацията!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализираме клиента за локация
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Слушател за новия бутон "Вземи координати"
        binding.btnGetLocation.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        // Слушател за бутона "Запази"
        binding.btnSave.setOnClickListener {
            savePlaceToDatabase()
        }
    }

    // Проверяваме дали имаме права. Ако да -> взимаме локацията. Ако не -> питаме потребителя.
    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Самата функция, която взима GPS данните от телефона
    private fun fetchLocation() {
        // Проверяваме отново правата заради сигурността на Android Studio
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Запазваме ги в променливите
                currentLat = location.latitude
                currentLng = location.longitude

                // Показваме ги на екрана
                binding.tvCoordinates.text = "Координати: $currentLat, $currentLng"
                Toast.makeText(requireContext(), "Локацията е взета!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Неуспешно взимане на локация. Включете GPS на емулатора/телефона!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePlaceToDatabase() {
        val placeName = binding.editTextName.text.toString().trim()
        val placeDesc = binding.editTextDescription.text.toString().trim()

        if (placeName.isEmpty()) {
            binding.layoutName.error = "Моля, въведете име на мястото"
            return
        }
        binding.layoutName.error = null

        // 4. Създаваме обекта и му подаваме ВЕЧЕ ВЗЕТИТЕ координати
        val newPlace = Place(
            name = placeName,
            description = placeDesc,
            latitude = currentLat,
            longitude = currentLng,
            tags = listOf("spotter", "маркер")
        )

        viewModel.insert(newPlace)
        Toast.makeText(requireContext(), "Успешно запазено с локация!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}