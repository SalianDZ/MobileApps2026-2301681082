package com.example.spotter.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
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
import com.example.spotter.databinding.FragmentEditPlaceBinding
import com.example.spotter.model.Place
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream

class EditPlaceFragment : Fragment() {

    private var _binding: FragmentEditPlaceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaceViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = PlaceRepository(database.placeDao())
        PlaceViewModelFactory(repository)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentPlace: Place? = null

    // Пазим текущите данни за да не ги загубим ако променим само едно нещо
    private var currentLat: Double? = null
    private var currentLng: Double? = null
    private var currentImagePath: String? = null

    // Камера и Локация
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            binding.imageViewPhoto.setImageBitmap(bitmap)
            currentImagePath = saveImageToInternalStorage(bitmap)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) takePictureLauncher.launch(null)
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) fetchLocation()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val placeId = arguments?.getInt("placeId") ?: return

        // 1. Зареждаме съществуващите данни
        viewModel.allPlaces.observe(viewLifecycleOwner) { places ->
            val place = places.find { it.id == placeId }
            if (place != null && currentPlace == null) {
                currentPlace = place
                populateData(place)
            }
        }

        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(null)
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnGetLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fetchLocation()
            } else {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.btnSave.setOnClickListener { saveChanges() }
    }

    private fun populateData(place: Place) {
        binding.editTextName.setText(place.name)
        binding.editTextDescription.setText(place.description)
        currentLat = place.latitude
        currentLng = place.longitude
        currentImagePath = place.imagePath

        if (currentLat != null && currentLng != null) {
            binding.tvCoordinates.text = "Координати: $currentLat, $currentLng"
        }

        if (!currentImagePath.isNullOrEmpty()) {
            val imgFile = File(currentImagePath!!)
            if (imgFile.exists()) {
                binding.imageViewPhoto.setImageURI(Uri.fromFile(imgFile))
            }
        }
    }

    private fun fetchLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLng = location.longitude
                binding.tvCoordinates.text = "Координати: $currentLat, $currentLng"
                Toast.makeText(requireContext(), "Координатите са обновени!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val filename = "spotter_image_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, filename)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file.absolutePath
    }

    private fun saveChanges() {
        val placeName = binding.editTextName.text.toString().trim()
        val placeDesc = binding.editTextDescription.text.toString().trim()

        if (placeName.isEmpty()) {
            binding.layoutName.error = "Името не може да е празно"
            return
        }

        currentPlace?.let { place ->
            // Тук правим копие на старото място но с новите данни и запазваме СЪЩОТО ID!
            val updatedPlace = place.copy(
                name = placeName,
                description = placeDesc,
                latitude = currentLat,
                longitude = currentLng,
                imagePath = currentImagePath
            )
            // Извикваме ъпдейт вместо инсерта
            viewModel.update(updatedPlace)
            Toast.makeText(requireContext(), "Промените са запазени!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}