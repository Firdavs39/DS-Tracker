package com.decoroomsteel.dstracker.ui.admin.employees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.decoroomsteel.dstracker.DSTrackerApplication
import com.decoroomsteel.dstracker.databinding.FragmentEmployeesBinding
import com.decoroomsteel.dstracker.databinding.DialogAddEmployeeBinding
import com.decoroomsteel.dstracker.databinding.DialogEditEmployeeBinding
import com.decoroomsteel.dstracker.data.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Фрагмент для управления сотрудниками (добавление, удаление, изменение ставки)
 */
class EmployeesFragment : Fragment() {

    private var _binding: FragmentEmployeesBinding? = null
    private val binding get() = _binding!!

    private lateinit var employeesAdapter: EmployeesAdapter
    private lateinit var auth: FirebaseAuth

    private val userRepository by lazy {
        (requireActivity().application as DSTrackerApplication).userRepository
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Настройка списка сотрудников
        setupRecyclerView()

        // Загрузка списка сотрудников
        loadEmployees()

        // Обработчик кнопки добавления сотрудника
        binding.fabAddEmployee.setOnClickListener {
            showAddEmployeeDialog()
        }
    }

    private fun setupRecyclerView() {
        employeesAdapter = EmployeesAdapter(
            onEditClick = { user -> showEditEmployeeDialog(user) },
            onDeleteClick = { user -> showDeleteEmployeeDialog(user) }
        )

        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = employeesAdapter
        }
    }

    private fun loadEmployees() {
        binding.progressBar.visibility = View.VISIBLE

        userRepository.getAllActiveUsers().observe(viewLifecycleOwner, Observer { users ->
            binding.progressBar.visibility = View.GONE
            employeesAdapter.submitList(users)

            binding.tvNoEmployees.visibility = if (users.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
    }

    private fun showAddEmployeeDialog() {
        val dialogBinding = DialogAddEmployeeBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить сотрудника")
            .setView(dialogBinding.root)
            .setPositiveButton("Добавить") { _, _ ->
                val email = dialogBinding.etEmail.text.toString().trim()
                val name = dialogBinding.etName.text.toString().trim()
                val hourlyRate = dialogBinding.etHourlyRate.text.toString().toDoubleOrNull() ?: 0.0
                val isAdmin = dialogBinding.switchAdmin.isChecked

                if (email.isNotEmpty() && name.isNotEmpty()) {
                    addEmployee(email, name, hourlyRate, isAdmin)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Пожалуйста, заполните все поля",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addEmployee(email: String, name: String, hourlyRate: Double, isAdmin: Boolean) {
        binding.progressBar.visibility = View.VISIBLE

        // Генерируем временный пароль для нового пользователя
        val tempPassword = generateRandomPassword()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Создание пользователя в Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, tempPassword).await()
                val userId = authResult.user?.uid

                if (userId != null) {
                    // Создание пользователя в локальной базе данных
                    val newUser = User(
                        id = userId,
                        email = email,
                        name = name,
                        isAdmin = isAdmin,
                        hourlyRate = hourlyRate
                    )

                    userRepository.insert(newUser)

                    // Отправка ссылки для сброса пароля
                    auth.sendPasswordResetEmail(email).await()

                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Сотрудник добавлен. Ссылка для установки пароля отправлена на $email",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showEditEmployeeDialog(user: User) {
        val dialogBinding = DialogEditEmployeeBinding.inflate(layoutInflater)

        // Заполняем поля текущими значениями
        dialogBinding.etName.setText(user.name)
        dialogBinding.etHourlyRate.setText(user.hourlyRate.toString())
        dialogBinding.switchAdmin.isChecked = user.isAdmin

        AlertDialog.Builder(requireContext())
            .setTitle("Редактировать сотрудника")
            .setView(dialogBinding.root)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = dialogBinding.etName.text.toString().trim()
                val hourlyRate = dialogBinding.etHourlyRate.text.toString().toDoubleOrNull() ?: 0.0
                val isAdmin = dialogBinding.switchAdmin.isChecked

                if (name.isNotEmpty()) {
                    updateEmployee(user.copy(
                        name = name,
                        hourlyRate = hourlyRate,
                        isAdmin = isAdmin
                    ))
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Имя не может быть пустым",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateEmployee(user: User) {
        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                userRepository.update(user)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Данные сотрудника обновлены",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showDeleteEmployeeDialog(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить сотрудника")
            .setMessage("Вы уверены, что хотите удалить сотрудника ${user.name}?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteEmployee(user)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteEmployee(user: User) {
        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Вместо физического удаления помечаем как неактивного
                userRepository.update(user.copy(active = false))

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Сотрудник удален",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun generateRandomPassword(): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(12) { chars.random() }.joinToString("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}