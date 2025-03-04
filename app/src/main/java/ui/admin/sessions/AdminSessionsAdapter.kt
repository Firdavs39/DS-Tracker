package ui.admin.sessions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.decoroomsteel.dstracker.R
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SessionViewHolder(private val view: View) :
        RecyclerView.ViewHolder(view) {

        private val tvEmployeeName: TextView = view.findViewById(R.id.tvEmployeeName)
        private val tvLocationName: TextView = view.findViewById(R.id.tvLocationName)
        private val tvStartTime: TextView = view.findViewById(R.id.tvStartTime)
        private val tvHourlyRate: TextView = view.findViewById(R.id.tvHourlyRate)
        private val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        private val tvStartedByAdmin: TextView = view.findViewById(R.id.tvStartedByAdmin)
        private val btnEndSession: Button = view.findViewById(R.id.btnEndSession)

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        fun bind(item: AdminSessionItem) {
            tvEmployeeName.text = item.employeeName
            tvLocationName.text = item.locationName
            tvStartTime.text = "Начало: ${dateFormat.format(item.session.startTime)}"
            tvHourlyRate.text = "Ставка: %.2f ₽/час".format(item.session.hourlyRate)

            // Длительность смены
            val duration = getDuration(item.session.startTime, Date())
            tvDuration.text = "Длительность: $duration"

            // Дополнительные пометки
            tvStartedByAdmin.visibility = if (item.session.startedByAdmin) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Обработчик кнопки завершения смены
            btnEndSession.setOnClickListener {
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