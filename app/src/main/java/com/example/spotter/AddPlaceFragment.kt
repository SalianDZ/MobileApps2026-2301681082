package com.example.spotter

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import java.io.File
import java.io.FileOutputStream
import kotlin.getValue

class AddPlaceFragment : Fragment() {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaceViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = PlaceRepository(database.placeDao())
        PlaceViewModelFactory(repository)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Променливи за данните
    private var currentLat: Double? = null
    private var currentLng: Double? = null
    private var currentImagePath: String? = null // Тук ще пазим пътя до снимката

    // 1. Стартиране на Камерата и взимане на резултата (Снимката)
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Показваме снимката на екрана
            binding.imageViewPhoto.setImageBitmap(bitmap)
            binding.imageViewPhoto.visibility = View.VISIBLE

            // Запазваме снимката във файл и взимаме пътя до нея
            currentImagePath = saveImageToInternalStorage(bitmap)
        } else {
            Toast.makeText(requireContext(), "Снимката не беше направена", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Искане на права за Камерата
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null) // Отваряме камерата
        } else {
            Toast.makeText(requireContext(), "Нужно е разрешение за камерата!", Toast.LENGTH_SHORT).show()
        }
    }

    // Искане на права за Локация (остава същото)
    private val requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            fetchLocation()
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Бутон за Локация
        binding.btnGetLocation.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        // НОВО: Бутон за Камера
        binding.btnCamera.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        // Бутон за Запазване
        binding.btnSave.setOnClickListener {
            savePlaceToDatabase()
        }
    }

    // --- Логика за Камерата ---
    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePictureLauncher.launch(null)
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Функция, която запазва снимката в телефона и връща пътя до нея
    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val filename = "spotter_image_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, filename)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    // --- Логика за Локацията (остава същата) ---
    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun fetchLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLng = location.longitude
                binding.tvCoordinates.text = "Координати: $currentLat, $currentLng"
                Toast.makeText(requireContext(), "Локацията е взета!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Неуспешно взимане на локация. Включете GPS!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Запазване в Базата ---
    private fun savePlaceToDatabase() {
        val placeName = binding.editTextName.text.toString().trim()
        val placeDesc = binding.editTextDescription.text.toString().trim()

        if (placeName.isEmpty()) {
            binding.layoutName.error = "Моля, въведете име на мястото"
            return
        }
        binding.layoutName.error = null

        // Създаваме обекта, като вече подаваме и ПЪТЯ ДО СНИМКАТА (imagePath)
        val newPlace = Place(
            name = placeName,
            description = placeDesc,
            latitude = currentLat,
            longitude = currentLng,
            imagePath = currentImagePath, // НОВО!
            tags = listOf("spotter", "маркер")
        )

        viewModel.insert(newPlace)
        Toast.makeText(requireContext(), "Успешно запазено!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}