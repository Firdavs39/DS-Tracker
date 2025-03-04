package ui.admin.sessions

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
import com.decoroomsteel.dstracker.R
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

    private var _binding: View? = null
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
        _binding = inflater.inflate(R.layout.fragment_sessions, container, false)
        return binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка списка смен
        setupRecyclerView()

        // Загрузка списка смен
        loadActiveSessions()

        // Обработчик кнопки добавления смены
        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabStartSession).setOnClickListener {
            showStartSessionDialog()
        }
    }

    private fun setupRecyclerView() {
        sessionsAdapter = AdminSessionsAdapter(
            onEndSessionClick = { session -> endSession(session) }
        )

        view?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSessions)?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sessionsAdapter
        }
    }

    private fun loadActiveSessions() {
        view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

        sessionRepository.getAllActiveSessions().observe(viewLifecycleOwner, Observer { sessions ->
            view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = View.GONE

            // Преобразование WorkSession в AdminSessionItem
            val sessionItems = mutableListOf<AdminSessionItem>()

            CoroutineScope(Dispatchers.IO).launch {
                for (session in sessions) {
                    val user = userRepository.getUserById(session.userId)
                    val location = locationRepository.getLocationByIdSync(session.locationId)

                    val item = AdminSessionItem(
                        session = session,
                        employeeName = user?.name ?: "Неизвестный сотрудник",
                        locationName = location?.name ?: "Неизвестная локация"
                    )

                    sessionItems.add(item)
                }

                withContext(Dispatchers.Main) {
                    sessionsAdapter.submitList(sessionItems)

                    view?.findViewById<android.widget.TextView>(R.id.tvNoSessions)?.visibility = if (sessionItems.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
        })
    }

    private fun showStartSessionDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_start_session, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Начать смену сотрудника")
            .setView(dialogView)
            .setPositiveButton("Начать", null) // Null, чтобы не закрывать окно при ошибке валидации
            .setNegativeButton("Отмена", null)
            .create()

        val spinnerEmployee = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerEmployee)
        val spinnerLocation = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerLocation)

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
                spinnerEmployee.adapter = employeeAdapter

                // Настройка выпадающего списка локаций
                val locationAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    locations.map { it.name }
                )
                spinnerLocation.adapter = locationAdapter

                // Настройка обработчика кнопки "Начать"
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val selectedEmployeePos = spinnerEmployee.selectedItemPosition
                        val selectedLocationPos = spinnerLocation.selectedItemPosition

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
        view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Проверка наличия активной смены у сотрудника
                val activeSession = sessionRepository.getActiveSessionForUserSync(employee.id)

                if (activeSession != null) {
                    withContext(Dispatchers.Main) {
                        view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = View.GONE
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
                    view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Смена начата для сотрудника ${employee.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = View.GONE
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
                view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Обновление смены с указанием времени окончания
                        val updatedSession = session.copy(
                            endTime = Date(),
                            endedByAdmin = true
                        )

                        sessionRepository.update(updatedSession)

                        withContext(Dispatchers.Main) {
                            view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Смена завершена",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = View.GONE
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