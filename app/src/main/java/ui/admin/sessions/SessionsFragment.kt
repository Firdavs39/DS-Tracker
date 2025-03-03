package com.decoroomsteel.dstracker.ui.admin.sessions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.decoroomsteel.dstracker.DSTrackerApplication
import com.decoroomsteel.dstracker.databinding.FragmentSessionsBinding
import com.decoroomsteel.dstracker.databinding.DialogStartSessionBinding
import com.decoroomsteel.dstracker.model.User
import com.decoroomsteel.dstracker.model.WorkLocation
import com.decoroomsteel.dstracker.model.WorkSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Фрагмент для управления рабочими сменами сотрудников
 */
class SessionsFragment : Fragment() {

    private var _binding: FragmentSessionsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sessionsAdapter: AdminSessionsAdapter
    
    private val userRepository by lazy { 
        (requireActivity().application as DSTrackerApplication).userRepository 
    }
    
    private val locationRepository by lazy { 
        (requireActivity().application as DSTrackerApplication).locationRepository 
    }
    
    private val sessionRepository by lazy { 
        (requireActivity().application as DSTrackerApplication).sessionRepository 
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Настройка списка смен
        setupRecyclerView()
        
        // Загрузка списка смен
        loadActiveSessions()
        
        // Обработчик кнопки добавления смены
        binding.fabStartSession.setOnClickListener {
            showStartSessionDialog()
        }
    }
    
    private fun setupRecyclerView() {
        sessionsAdapter = AdminSessionsAdapter(
            onEndSessionClick = { session -> endSession(session) }
        )
        
        binding.rvSessions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sessionsAdapter
        }
    }
    
    private fun loadActiveSessions() {
        binding.progressBar.visibility = View.VISIBLE
        
        sessionRepository.getAllActiveSessions().observe(viewLifecycleOwner, Observer { sessions ->
            binding.progressBar.visibility = View.GONE
            sessionsAdapter.submitList(sessions)
            
            binding.tvNoSessions.visibility = if (sessions.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
    }
    
    private fun showStartSessionDialog() {
        val dialogBinding = DialogStartSessionBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Начать смену сотрудника")
            .setView(dialogBinding.root)
            .setPositiveButton("Начать", null) // Null, чтобы не закрывать окно при ошибке валидации
            .setNegativeButton("Отмена", null)
            .create()
        
        // Загрузка списка сотрудников
        CoroutineScope(Dispatchers.IO).launch {
            val employees = userRepository.getAllActiveUsersSync()
            val locations = locationRepository.getAllActiveLocationsSync()
            
            withContext(Dispatchers.Main) {
                // Настройка выпадающего списка сотрудников
                val employeeAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    employees.map { it.name }
                )
                dialogBinding.spinnerEmployee.adapter = employeeAdapter
                
                // Настройка выпадающего списка локаций
                val locationAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    locations.map { it.name }
                )
                dialogBinding.spinnerLocation.adapter = locationAdapter
                
                // Настройка обработчика кнопки "Начать"
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val selectedEmployeePos = dialogBinding.spinnerEmployee.selectedItemPosition
                        val selectedLocationPos = dialogBinding.spinnerLocation.selectedItemPosition
                        
                        if (selectedEmployeePos >= 0 && selectedLocationPos >= 0) {
                            val employee = employees[selectedEmployeePos]
                            val location = locations[selectedLocationPos]
                            startSession(employee, location)
                            dialog.dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Выберите сотрудника и локацию",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
        
        dialog.show()
    }
    
    private fun startSession(employee: User, location: WorkLocation) {
        binding.progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Проверка наличия активной смены у сотрудника
                val activeSession = sessionRepository.getActiveSessionForUserSync(employee.id)
                
                if (activeSession != null) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "У сотрудника уже есть активная смена",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }
                
                // Создание новой смены
                val session = WorkSession(
                    userId = employee.id,
                    locationId = location.id,
                    startTime = Date(),
                    hourlyRate = employee.hourlyRate,
                    startedByAdmin = true
                )
                
                sessionRepository.insert(session)
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Смена начата для сотрудника ${employee.name}",
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
    
    private fun endSession(session: WorkSession) {
        AlertDialog.Builder(requireContext())
            .setTitle("Завершить смену")
            .setMessage("Вы уверены, что хотите завершить текущую смену этого сотрудника?")
            .setPositiveButton("Завершить") { _, _ ->
                binding.progressBar.visibility = View.VISIBLE
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Обновление смены с указанием времени окончания
                        val updatedSession = session.copy(
                            endTime = Date(),
                            endedByAdmin = true
                        )
                        
                        sessionRepository.update(updatedSession)
                        
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Смена завершена",
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
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 