package com.decoroomsteel.dstracker.ui.admin.reports

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.decoroomsteel.dstracker.DSTrackerApplication
import com.decoroomsteel.dstracker.R
import com.decoroomsteel.dstracker.data.model.User
import com.decoroomsteel.dstracker.data.model.WorkLocation
import com.decoroomsteel.dstracker.data.model.WorkSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Фрагмент для формирования и экспорта отчетов
 */
class ReportsFragment : Fragment() {

    private var _binding: View? = null
    private val binding get() = _binding!!

    private val userRepository by lazy {
        (requireActivity().application as DSTrackerApplication).userRepository
    }

    private val locationRepository by lazy {
        (requireActivity().application as DSTrackerApplication).locationRepository
    }

    private val sessionRepository by lazy {
        (requireActivity().application as DSTrackerApplication).sessionRepository
    }

    // Справочники
    private lateinit var usersMap: Map<String, User>
    private lateinit var locationsMap: Map<Long, WorkLocation>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflater.inflate(R.layout.fragment_reports, container, false)
        return binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Загрузка списков сотрудников и локаций для фильтров
        loadFilters()

        // Установка текущего месяца в датапикере
        setupDatePickers()

        // Обработчики кнопок отчетов
        view.findViewById<Button>(R.id.btnGenerateEmployeeReport).setOnClickListener {
            generateEmployeeReport()
        }

        view.findViewById<Button>(R.id.btnGenerateLocationReport).setOnClickListener {
            generateLocationReport()
        }

        view.findViewById<Button>(R.id.btnGenerateGeneralReport).setOnClickListener {
            generateGeneralReport()
        }
    }

    private fun setupDatePickers() {
        // Получаем текущую дату
        val calendar = Calendar.getInstance()

        // Здесь нужна реализация в зависимости от вашего виджета выбора даты
        // Если используете стандартный DatePicker, то код может быть таким:
        // binding.findViewById<DatePicker>(R.id.monthYearPicker).updateDate(
        //    calendar.get(Calendar.YEAR),
        //    calendar.get(Calendar.MONTH),
        //    1
        // )
    }

    private fun loadFilters() {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            // Загрузка сотрудников
            val users = userRepository.getAllActiveUsersSync()
            usersMap = users.associateBy { it.id }

            // Загрузка локаций
            val locations = locationRepository.getAllActiveLocationsSync()
            locationsMap = locations.associateBy { it.id }

            withContext(Dispatchers.Main) {
                view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE

                // Настройка выпадающих списков
                val employeeAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    users.map { it.name }
                )
                view?.findViewById<Spinner>(R.id.spinnerEmployee)?.adapter = employeeAdapter

                val locationAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    locations.map { it.name }
                )
                view?.findViewById<Spinner>(R.id.spinnerLocation)?.adapter = locationAdapter
            }
        }
    }

    private fun generateEmployeeReport() {
        val selectedPosition = view?.findViewById<Spinner>(R.id.spinnerEmployee)?.selectedItemPosition ?: -1

        if (selectedPosition < 0) {
            Toast.makeText(
                requireContext(),
                "Выберите сотрудника",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

        val selectedEmployee = usersMap.values.toList()[selectedPosition]
        val startDate = getStartDate()
        val endDate = getEndDate()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получение сессий сотрудника за выбранный период
                val sessions = sessionRepository.getSessionsForPeriodByUserSync(
                    userId = selectedEmployee.id,
                    startDate = startDate,
                    endDate = endDate
                )

                val reportContent = generateEmployeeReportContent(
                    employee = selectedEmployee,
                    sessions = sessions,
                    startDate = startDate,
                    endDate = endDate
                )

                // Сохранение отчета и отправка
                val reportFile = saveReportToFile(
                    "отчет_сотрудник_${selectedEmployee.name}_${formatDateForFileName(startDate)}.csv",
                    reportContent
                )

                withContext(Dispatchers.Main) {
                    view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                    shareReport(reportFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun generateLocationReport() {
        val selectedPosition = view?.findViewById<Spinner>(R.id.spinnerLocation)?.selectedItemPosition ?: -1

        if (selectedPosition < 0) {
            Toast.makeText(
                requireContext(),
                "Выберите рабочую зону",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

        val selectedLocation = locationsMap.values.toList()[selectedPosition]
        val startDate = getStartDate()
        val endDate = getEndDate()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получение сессий на объекте за выбранный период
                val sessions = sessionRepository.getSessionsForPeriodByLocationSync(
                    locationId = selectedLocation.id,
                    startDate = startDate,
                    endDate = endDate
                )

                val reportContent = generateLocationReportContent(
                    location = selectedLocation,
                    sessions = sessions,
                    startDate = startDate,
                    endDate = endDate
                )

                // Сохранение отчета и отправка
                val reportFile = saveReportToFile(
                    "отчет_объект_${selectedLocation.name}_${formatDateForFileName(startDate)}.csv",
                    reportContent
                )

                withContext(Dispatchers.Main) {
                    view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                    shareReport(reportFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun generateGeneralReport() {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

        val startDate = getStartDate()
        val endDate = getEndDate()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получение всех сессий за выбранный период
                val sessions = sessionRepository.getSessionsForPeriodSync(
                    startDate = startDate,
                    endDate = endDate
                )

                val reportContent = generateGeneralReportContent(
                    sessions = sessions,
                    startDate = startDate,
                    endDate = endDate
                )

                // Сохранение отчета и отправка
                val reportFile = saveReportToFile(
                    "отчет_общий_${formatDateForFileName(startDate)}.csv",
                    reportContent
                )

                withContext(Dispatchers.Main) {
                    view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                    shareReport(reportFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun getStartDate(): Date {
        val calendar = Calendar.getInstance()
        // Первый день выбранного месяца
        // Адаптируйте этот код под ваш виджет выбора даты
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.time
    }

    private fun getEndDate(): Date {
        val calendar = Calendar.getInstance()
        // Последний день выбранного месяца
        // Адаптируйте этот код под ваш виджет выбора даты
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        return calendar.time
    }

    private fun generateEmployeeReportContent(
        employee: User,
        sessions: List<WorkSession>,
        startDate: Date,
        endDate: Date
    ): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val sb = StringBuilder()

        // Заголовок отчета
        sb.appendLine("Отчет по сотруднику: ${employee.name}")
        sb.appendLine("Email: ${employee.email}")
        sb.appendLine("Ставка: ${employee.hourlyRate} ₽/час")
        sb.appendLine("Период: ${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}")
        sb.appendLine()

        // Заголовки столбцов
        sb.appendLine("Дата,Начало,Окончание,Локация,Часов,Сумма")

        // Данные о сменах
        var totalHours = 0.0
        var totalEarnings = 0.0

        sessions.forEach { session ->
            val locationName = locationsMap[session.locationId]?.name ?: "Неизвестная локация"
            val duration = session.getDuration()
            val earnings = session.getEarnings()

            sb.appendLine(
                "${dateFormat.format(session.startTime).split(" ")[0]}," +
                        "${dateFormat.format(session.startTime).split(" ")[1]}," +
                        "${if (session.endTime != null) dateFormat.format(session.endTime!!).split(" ")[1] else "Активна"}," +
                        "$locationName," +
                        "${String.format("%.2f", duration)}," +
                        "${String.format("%.2f", earnings)}"
            )

            totalHours += duration
            totalEarnings += earnings
        }

        // Итоги
        sb.appendLine()
        sb.appendLine("Итого часов,,,,$totalHours,")
        sb.appendLine("Итого сумма,,,,,${String.format("%.2f", totalEarnings)}")

        return sb.toString()
    }

    private fun generateLocationReportContent(
        location: WorkLocation,
        sessions: List<WorkSession>,
        startDate: Date,
        endDate: Date
    ): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val sb = StringBuilder()

        // Заголовок отчета
        sb.appendLine("Отчет по объекту: ${location.name}")
        sb.appendLine("Адрес: ${location.address}")
        sb.appendLine("Период: ${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}")
        sb.appendLine()

        // Заголовки столбцов
        sb.appendLine("Дата,Сотрудник,Начало,Окончание,Часов,Сумма")

        // Данные о сменах
        var totalHours = 0.0
        var totalEarnings = 0.0

        sessions.forEach { session ->
            val employeeName = usersMap[session.userId]?.name ?: "Неизвестный сотрудник"
            val duration = session.getDuration()
            val earnings = session.getEarnings()

            sb.appendLine(
                "${dateFormat.format(session.startTime).split(" ")[0]}," +
                        "$employeeName," +
                        "${dateFormat.format(session.startTime).split(" ")[1]}," +
                        "${if (session.endTime != null) dateFormat.format(session.endTime!!).split(" ")[1] else "Активна"}," +
                        "${String.format("%.2f", duration)}," +
                        "${String.format("%.2f", earnings)}"
            )

            totalHours += duration
            totalEarnings += earnings
        }

        // Итоги
        sb.appendLine()
        sb.appendLine("Итого часов,,,,${String.format("%.2f", totalHours)},")
        sb.appendLine("Итого сумма,,,,,${String.format("%.2f", totalEarnings)}")

        return sb.toString()
    }

    private fun generateGeneralReportContent(
        sessions: List<WorkSession>,
        startDate: Date,
        endDate: Date
    ): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val sb = StringBuilder()

        // Заголовок отчета
        sb.appendLine("Общий отчет по всем сотрудникам и объектам")
        sb.appendLine("Период: ${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}")
        sb.appendLine()

        // Заголовки столбцов
        sb.appendLine("Дата,Сотрудник,Локация,Начало,Окончание,Часов,Сумма")

        // Данные о сменах
        var totalHours = 0.0
        var totalEarnings = 0.0

        sessions.forEach { session ->
            val employeeName = usersMap[session.userId]?.name ?: "Неизвестный сотрудник"
            val locationName = locationsMap[session.locationId]?.name ?: "Неизвестная локация"
            val duration = session.getDuration()
            val earnings = session.getEarnings()

            sb.appendLine(
                "${dateFormat.format(session.startTime).split(" ")[0]}," +
                        "$employeeName," +
                        "$locationName," +
                        "${dateFormat.format(session.startTime).split(" ")[1]}," +
                        "${if (session.endTime != null) dateFormat.format(session.endTime!!).split(" ")[1] else "Активна"}," +
                        "${String.format("%.2f", duration)}," +
                        "${String.format("%.2f", earnings)}"
            )

            totalHours += duration
            totalEarnings += earnings
        }

        // Итоги
        sb.appendLine()
        sb.appendLine("Итого часов,,,,,${String.format("%.2f", totalHours)},")
        sb.appendLine("Итого сумма,,,,,,${String.format("%.2f", totalEarnings)}")

        return sb.toString()
    }

    private fun saveReportToFile(fileName: String, content: String): File {
        // Создание директории для отчетов
        val reportsDir = File(requireContext().filesDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        // Создание файла отчета
        val file = File(reportsDir, fileName)
        FileOutputStream(file).use {
            it.write(content.toByteArray())
        }

        return file
    }

    private fun shareReport(file: File) {
        // Создание URI через FileProvider
        val uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".fileprovider",
            file
        )

        // Создание Intent для отправки отчета
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "text/csv"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // Отображение диалога выбора приложения
        startActivity(Intent.createChooser(shareIntent, "Отправить отчет"))
    }

    private fun formatDateForFileName(date: Date): String {
        val format = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        return format.format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}