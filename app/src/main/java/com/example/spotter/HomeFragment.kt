package com.example.spotter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotter.data.AppDatabase
import com.example.spotter.data.PlaceRepository
import com.example.spotter.ui.PlaceAdapter
import com.example.spotter.databinding.FragmentHomeBinding
import com.example.spotter.ui.PlaceViewModel
import com.example.spotter.ui.PlaceViewModelFactory
import kotlin.getValue

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaceViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = PlaceRepository(database.placeDao())
        PlaceViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val placeAdapter = PlaceAdapter()

        binding.recyclerViewPlaces.apply {
            adapter = placeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Наблюдаваме списъка с места
        viewModel.allPlaces.observe(viewLifecycleOwner) { places ->
            placeAdapter.submitList(places)

            // Ако базата е празна, показваме "Празния екран", иначе показваме Решетката
            if (places.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewPlaces.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerViewPlaces.visibility = View.VISIBLE
            }
        }

        binding.fabAddPlace.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addPlaceFragment)
        }

        // Логика за изтриване с плъзгане (Swipe to Delete)
        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(
            object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                0,
                androidx.recyclerview.widget.ItemTouchHelper.LEFT or androidx.recyclerview.widget.ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: androidx.recyclerview.widget.RecyclerView,
                    viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                    target: androidx.recyclerview.widget.RecyclerView.ViewHolder
                ): Boolean {
                    return false // Не ни трябва преместване нагоре/надолу
                }

                override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                    // Взимаме позицията на плъзнатия елемент
                    val position = viewHolder.adapterPosition
                    val placeToDelete = placeAdapter.currentList[position]

                    // Казваме на ViewModel да го изтрие от базата
                    viewModel.delete(placeToDelete)
                }
            }
        )
        // Прикачваме тази логика към нашия списък
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewPlaces)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}