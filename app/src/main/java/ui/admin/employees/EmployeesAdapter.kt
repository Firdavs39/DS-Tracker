package com.decoroomsteel.dstracker.ui.admin.employees

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.decoroomsteel.dstracker.databinding.ItemEmployeeBinding
import com.decoroomsteel.dstracker.data.model.User

/**
 * Адаптер для отображения списка сотрудников с возможностью редактирования и удаления
 */
class EmployeesAdapter(
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : ListAdapter<User, EmployeesAdapter.EmployeeViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val binding = ItemEmployeeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmployeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EmployeeViewHolder(private val binding: ItemEmployeeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvEmployeeName.text = user.name
            binding.tvEmployeeEmail.text = user.email
            binding.tvEmployeeHourlyRate.text = "%.2f ₽/час".format(user.hourlyRate)
            binding.tvEmployeeRole.text = if (user.isAdmin) "Администратор" else "Сотрудник"

            // Обработчики кнопок редактирования и удаления
            binding.btnEdit.setOnClickListener { onEditClick(user) }
            binding.btnDelete.setOnClickListener { onDeleteClick(user) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}