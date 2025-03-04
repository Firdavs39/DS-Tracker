package com.decoroomsteel.dstracker.ui.worker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.decoroomsteel.dstracker.databinding.ItemWorkSessionBinding
import com.decoroomsteel.dstracker.model.WorkSession
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Адаптер для отображения списка рабочих смен
 */
class WorkSessionsAdapter : ListAdapter<WorkSession, WorkSessionsAdapter.WorkSessionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkSessionViewHolder {
        val binding = ItemWorkSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkSessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkSessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WorkSessionViewHolder(private val binding: ItemWorkSessionBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        fun bind(session: WorkSession) {
            // Форматирование дат
            val startDate = dateFormat.format(session.startTime)
            val startTime = timeFormat.format(session.startTime)
            
            val endTime = if (session.endTime != null) {
                timeFormat.format(session.endTime)
            } else {
                "Активна"
            }
            
            // Расчет часов и заработка
            val hours = if (session.endTime != null) {
                String.format("%.2f ч", session.getDuration())
            } else {
                "В процессе"
            }
            
            val earnings = if (session.endTime != null) {
                String.format("%.2f ₽", session.getEarnings())
            } else {
                "—"
            }
            
            // Установка данных в представление
            binding.tvSessionDate.text = startDate
            binding.tvSessionTime.text = "$startTime - $endTime"
            binding.tvSessionDuration.text = hours
            binding.tvSessionEarnings.text = earnings
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WorkSession>() {
            override fun areItemsTheSame(oldItem: WorkSession, newItem: WorkSession): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: WorkSession, newItem: WorkSession): Boolean {
                return oldItem == newItem
            }
        }
    }
} 