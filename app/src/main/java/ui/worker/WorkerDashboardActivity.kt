package com.decoroomsteel.dstracker.ui.worker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.decoroomsteel.dstracker.DSTrackerApplication
import com.decoroomsteel.dstracker.databinding.ActivityWorkerDashboardBinding
import com.decoroomsteel.dstracker.model.User
import com.decoroomsteel.dstracker.model.WorkSession
import com.decoroomsteel.dstracker.ui.auth.LoginActivity
import com.decoroomsteel.dstracker.ui.scanner.QrScannerActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Экран работника для отслеживания смен и сканирования QR-кода
 */
class WorkerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: User
    private lateinit var sessionsAdapter: WorkSessionsAdapter
    
    private val userRepository by lazy { (application as DSTrackerApplication).userRepository }
    private val sessionRepository by lazy { (application as DSTrackerApplication).sessionRepository }
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val QR_SCANNER_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        
        // Настройка заголовка
        binding.tvAppTitle.text = "Decoroom Steel Time"
        
        // Настройка адаптера для списка смен
        sessionsAdapter = WorkSessionsAdapter()
        binding.rvWorkSessions.apply {
            layoutManager = LinearLayoutManager(this@WorkerDashboardActivity)
            adapter = sessionsAdapter
        }

        // Кнопка сканирования QR-кода
        binding.btnScanQr.setOnClickListener {
            checkLocationPermission()
        }
        
        // Кнопка выхода из аккаунта
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        
        loadUserData()
    }
    
    override fun onResume() {
        super.onResume()
        loadActiveSession()
        loadRecentSessions()
    }
    
    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        
        if (userId == null) {
            // Если пользователь не авторизован, перенаправляем на экран входа
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        userRepository.getActiveUserById(userId).observe(this, Observer { user ->
            if (user == null) {
                // Пользователь не найден или деактивирован
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return@Observer
            }
            
            currentUser = user
            binding.tvUserName.text = "Привет, ${user.name}"
            binding.tvHourlyRate.text = "Ставка: ${user.hourlyRate} ₽/час"
        })
    }
    
    private fun loadActiveSession() {
        val userId = auth.currentUser?.uid ?: return
        
        sessionRepository.getActiveSessionForUser(userId).observe(this, Observer { session ->
            if (session != null) {
                // У пользователя есть активная смена
                binding.cardActiveSession.visibility = View.VISIBLE
                binding.btnScanQr.text = "Завершить смену"
                
                val startTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val startTimeText = startTimeFormat.format(session.startTime)
                
                binding.tvActiveSessionInfo.text = "Смена начата: $startTimeText"
            } else {
                // Нет активной смены
                binding.cardActiveSession.visibility = View.GONE
                binding.btnScanQr.text = "Начать смену (QR)"
            }
        })
    }
    
    private fun loadRecentSessions() {
        val userId = auth.currentUser?.uid ?: return
        
        sessionRepository.getRecentSessionsForUser(userId, 10).observe(this, Observer { sessions ->
            sessionsAdapter.submitList(sessions)
            binding.tvNoSessions.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
        })
    }
    
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Запрос разрешения на геолокацию
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startQrScanner()
        }
    }
    
    private fun startQrScanner() {
        val intent = Intent(this, QrScannerActivity::class.java)
        startActivityForResult(intent, QR_SCANNER_REQUEST_CODE)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQrScanner()
            } else {
                Toast.makeText(
                    this,
                    "Требуется разрешение на доступ к местоположению для работы с QR-кодом",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == QR_SCANNER_REQUEST_CODE && resultCode == RESULT_OK) {
            val scannedQrCode = data?.getStringExtra("qr_code")
            if (scannedQrCode != null) {
                handleQrCode(scannedQrCode)
            }
        }
    }
    
    private fun handleQrCode(qrCode: String) {
        val userId = auth.currentUser?.uid ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Проверка активной смены
                val activeSession = sessionRepository.getActiveSessionForUserSync(userId)
                
                if (activeSession == null) {
                    // Начинаем новую смену
                    startNewSession(userId, qrCode)
                } else {
                    // Заканчиваем текущую смену
                    endCurrentSession(activeSession)
                }
            } catch (e: Exception) {
                showMessage("Ошибка: ${e.message}")
            }
        }
    }
    
    private suspend fun startNewSession(userId: String, qrCode: String) {
        try {
            // Получаем локацию по QR-коду
            val location = (application as DSTrackerApplication).locationRepository.getLocationByQrCode(qrCode)
            
            if (location == null) {
                showMessage("QR-код не соответствует ни одной рабочей зоне")
                return
            }
            
            // Создаем новую сессию
            val session = WorkSession(
                userId = userId,
                locationId = location.id,
                startTime = Date(),
                hourlyRate = currentUser.hourlyRate
            )
            
            sessionRepository.insert(session)
            showMessage("Смена начата на объекте: ${location.name}")
            
            withContext(Dispatchers.Main) {
                loadActiveSession()
            }
        } catch (e: Exception) {
            showMessage("Ошибка начала смены: ${e.message}")
        }
    }
    
    private suspend fun endCurrentSession(session: WorkSession) {
        try {
            // Завершаем текущую сессию
            val updatedSession = session.copy(endTime = Date())
            sessionRepository.update(updatedSession)
            
            val hours = updatedSession.getDuration()
            val earnings = updatedSession.getEarnings()
            
            showMessage("Смена завершена. Отработано: %.2f ч. Заработок: %.2f ₽".format(hours, earnings))
            
            withContext(Dispatchers.Main) {
                loadActiveSession()
                loadRecentSessions()
            }
        } catch (e: Exception) {
            showMessage("Ошибка завершения смены: ${e.message}")
        }
    }
    
    private suspend fun showMessage(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@WorkerDashboardActivity, message, Toast.LENGTH_LONG).show()
        }
    }
} 