package com.decoroomsteel.dstracker.ui.admin.sessions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.decoroomsteel.dstracker.databinding.ItemAdminSessionBinding
import com.decoroomsteel.dstracker.model.WorkSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Адаптер для отображения списка активных смен в панели администратора
 */
class AdminSessionsAdapter(
    private val onEndSessionClick: (WorkSession) -> Unit
) : ListAdapter<AdminSessionItem, AdminSessionsAdapter.SessionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemAdminSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SessionViewHolder(private val binding: ItemAdminSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        
        fun bind(item: AdminSessionItem) {
            binding.tvEmployeeName.text = item.employeeName
            binding.tvLocationName.text = item.locationName
            binding.tvStartTime.text = "Начало: ${dateFormat.format(item.session.startTime)}"
            binding.tvHourlyRate.text = "Ставка: %.2f ₽/час".format(item.session.hourlyRate)
            
            // Длительность смены
            val duration = getDuration(item.session.startTime, Date())
            binding.tvDuration.text = "Длительность: $duration"
            
            // Дополнительные пометки
            if (item.session.startedByAdmin) {
                binding.tvStartedByAdmin.visibility = android.view.View.VISIBLE
            } else {
                binding.tvStartedByAdmin.visibility = android.view.View.GONE
            }
            
            // Обработчик кнопки завершения смены
            binding.btnEndSession.setOnClickListener {
                onEndSessionClick(item.session)
            }
        }
        
        private fun getDuration(startTime: Date, currentTime: Date): String {
            val durationMs = currentTime.time - startTime.time
            val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
            
            return if (hours > 0) {
                "$hours ч $minutes мин"
            } else {
                "$minutes мин"
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AdminSessionItem>() {
            override fun areItemsTheSame(oldItem: AdminSessionItem, newItem: AdminSessionItem): Boolean {
                return oldItem.session.id == newItem.session.id
            }

            override fun areContentsTheSame(oldItem: AdminSessionItem, newItem: AdminSessionItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}

/**
 * Элемент списка смен с дополнительной информацией о сотруднике и локации
 */
data class AdminSessionItem(
    val session: WorkSession,
    val employeeName: String,
    val locationName: String
) 