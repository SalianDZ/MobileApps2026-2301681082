package com.example.spotter.ui // Провери дали съвпада с твоя пакет!

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spotter.databinding.ItemPlaceBinding
import com.example.spotter.model.Place
import java.io.File

class PlaceAdapter : ListAdapter<Place, PlaceAdapter.PlaceViewHolder>(PlaceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val currentPlace = getItem(position)
        holder.bind(currentPlace)

        // НОВО: Добавяме слушател за клик върху цялата карта
        holder.itemView.setOnClickListener { view ->
            val bundle = android.os.Bundle().apply {
                putInt("placeId", currentPlace.id) // Подаваме ID-то на мястото
            }
            // Отваряме екрана с детайлите (ще го добавим в навигацията в следващата стъпка)
            androidx.navigation.Navigation.findNavController(view).navigate(
                com.example.spotter.R.id.action_homeFragment_to_placeDetailsFragment,
                bundle
            )
        }
    }

    inner class PlaceViewHolder(private val binding: ItemPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place: Place) {
            binding.tvPlaceName.text = place.name
            binding.tvPlaceDescription.text = place.description

            // МАГИЯТА Е ТУК: Проверяваме дали мястото има записана снимка
            if (!place.imagePath.isNullOrEmpty()) {
                val imgFile = File(place.imagePath)
                if (imgFile.exists()) {
                    // Ако снимката съществува в телефона, я показваме вляво
                    binding.imagePlace.setImageURI(Uri.fromFile(imgFile))
                } else {
                    // Защита: Ако файлът е изчезнал, показваме иконка по подразбиране
                    binding.imagePlace.setImageResource(android.R.drawable.ic_menu_camera)
                }
            } else {
                // Ако потребителят е записал мястото без снимка, показваме стандартната иконка
                binding.imagePlace.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }
    }

    class PlaceDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }
    }
}