package com.decoroomsteel.dstracker.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.decoroomsteel.dstracker.DSTrackerApplication
import com.decoroomsteel.dstracker.databinding.ActivityQrScannerBinding
import com.decoroomsteel.dstracker.data.model.WorkLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Экран сканирования QR-кода и проверки геолокации
 */
class QrScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private val locationRepository by lazy { (application as DSTrackerApplication).locationRepository }
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val LOCATION_MAX_DISTANCE_METERS = 200.0 // Максимальное расстояние от рабочей зоны в метрах
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Инициализация сервисов
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Проверка разрешения на камеру
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
        
        // Кнопка закрытия
        binding.btnClose.setOnClickListener {
            finish()
        }
    }
    
    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Требуется разрешение на камеру для сканирования QR-кода",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
    
    private fun startCamera() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrCode ->
                        // Остановка анализа после обнаружения QR-кода
                        it.clearAnalyzer()
                        
                        // Проверка геолокации и QR-кода
                        processQrCode(qrCode)
                    })
                }
            
            try {
                // Отключение всех предыдущих привязок
                cameraProvider.unbindAll()
                
                // Привязка камеры к жизненному циклу
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка камеры: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun processQrCode(qrCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получение рабочей локации по QR-коду
                val location = locationRepository.getLocationByQrCode(qrCode)
                
                if (location == null) {
                    showMessage("QR-код не соответствует ни одной рабочей зоне")
                    return@launch
                }
                
                // Проверка геолокации
                checkLocationAndProceed(location, qrCode)
            } catch (e: Exception) {
                showMessage("Ошибка при обработке QR-кода: ${e.message}")
            }
        }
    }
    
    private fun checkLocationAndProceed(workLocation: WorkLocation, qrCode: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Если нет разрешения на геолокацию, просто возвращаем QR-код
            returnQrCodeResult(qrCode)
            return
        }
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location == null) {
                showMessageAndFinish("Не удалось получить местоположение. Проверьте, включен ли GPS")
                return@addOnSuccessListener
            }
            
            // Расчет расстояния между текущим местоположением и рабочей зоной
            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                workLocation.latitude, workLocation.longitude,
                results
            )
            
            val distanceInMeters = results[0]
            
            if (distanceInMeters <= LOCATION_MAX_DISTANCE_METERS) {
                // Пользователь находится в пределах допустимого расстояния
                returnQrCodeResult(qrCode)
            } else {
                // Пользователь слишком далеко от рабочей зоны
                showMessageAndFinish(
                    "Вы находитесь слишком далеко от рабочей зоны " +
                    "(${distanceInMeters.toInt()} м). Максимальное расстояние: " +
                    "$LOCATION_MAX_DISTANCE_METERS м"
                )
            }
        }.addOnFailureListener { e ->
            showMessageAndFinish("Ошибка определения местоположения: ${e.message}")
        }
    }
    
    private fun returnQrCodeResult(qrCode: String) {
        val resultIntent = Intent()
        resultIntent.putExtra("qr_code", qrCode)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
    
    private suspend fun showMessage(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@QrScannerActivity, message, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showMessageAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
} 