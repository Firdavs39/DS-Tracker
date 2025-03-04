package com.decoroomsteel.dstracker.ui.admin.locations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.decoroomsteel.dstracker.databinding.ItemLocationBinding
import com.decoroomsteel.dstracker.model.WorkLocation

/**
 * Адаптер для отображения списка рабочих зон
 */
class LocationsAdapter(
    private val onViewQrClick: (WorkLocation) -> Unit,
    private val onDeleteClick: (WorkLocation) -> Unit
) : ListAdapter<WorkLocation, LocationsAdapter.LocationViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LocationViewHolder(private val binding: ItemLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(location: WorkLocation) {
            binding.tvLocationName.text = location.name
            binding.tvLocationAddress.text = location.address
            binding.tvLocationCoordinates.text = 
                "Координаты: ${location.latitude}, ${location.longitude}"

            // Обработчики кнопок просмотра QR-кода и удаления
            binding.btnViewQr.setOnClickListener { onViewQrClick(location) }
            binding.btnDelete.setOnClickListener { onDeleteClick(location) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WorkLocation>() {
            override fun areItemsTheSame(oldItem: WorkLocation, newItem: WorkLocation): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: WorkLocation, newItem: WorkLocation): Boolean {
                return oldItem == newItem
            }
        }
    }
} 