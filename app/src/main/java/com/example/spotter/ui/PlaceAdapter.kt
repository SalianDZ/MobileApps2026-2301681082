package com.example.spotter.ui // Провери дали съвпада с твоя пакет!

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spotter.model.Place
import com.example.spotter.databinding.ItemPlaceBinding

class PlaceAdapter : ListAdapter<Place, PlaceAdapter.PlaceViewHolder>(PlaceDiffCallback()) {

    // Създава "кутийката" (визуалния дизайн item_place), когато списъкът има нужда
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    // Пълни "кутийката" с реални данни (име и описание)
    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val currentPlace = getItem(position)
        holder.bind(currentPlace)
    }

    // Вътрешен клас, който държи връзката с визуалните елементи
    inner class PlaceViewHolder(private val binding: ItemPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place: Place) {
            binding.tvPlaceName.text = place.name
            binding.tvPlaceDescription.text = place.description

            // Забележка: По-късно тук ще зареждаме и снимката, когато стигнем до камерата!
        }
    }

    // Този клас помага на RecyclerView да разбере дали данните са се променили
    class PlaceDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }
    }
}