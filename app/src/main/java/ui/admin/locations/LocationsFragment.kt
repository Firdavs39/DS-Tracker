package com.decoroomsteel.dstracker.ui.admin.locations

import android.graphics.Bitmap
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
import com.decoroomsteel.dstracker.databinding.FragmentLocationsBinding
import com.decoroomsteel.dstracker.databinding.DialogAddLocationBinding
import com.decoroomsteel.dstracker.model.WorkLocation
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Фрагмент для управления рабочими зонами и их QR-кодами
 */
class LocationsFragment : Fragment() {

    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var locationsAdapter: LocationsAdapter
    
    private val locationRepository by lazy { 
        (requireActivity().application as DSTrackerApplication).locationRepository 
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Настройка списка локаций
        setupRecyclerView()
        
        // Загрузка списка локаций
        loadLocations()
        
        // Обработчик кнопки добавления локации
        binding.fabAddLocation.setOnClickListener {
            showAddLocationDialog()
        }
    }
    
    private fun setupRecyclerView() {
        locationsAdapter = LocationsAdapter(
            onViewQrClick = { location -> showQrCodeDialog(location) },
            onDeleteClick = { location -> showDeleteLocationDialog(location) }
        )
        
        binding.rvLocations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = locationsAdapter
        }
    }
    
    private fun loadLocations() {
        binding.progressBar.visibility = View.VISIBLE
        
        locationRepository.getAllActiveLocations().observe(viewLifecycleOwner, Observer { locations ->
            binding.progressBar.visibility = View.GONE
            locationsAdapter.submitList(locations)
            
            binding.tvNoLocations.visibility = if (locations.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
    }
    
    private fun showAddLocationDialog() {
        val dialogBinding = DialogAddLocationBinding.inflate(layoutInflater)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Добавить рабочую зону")
            .setView(dialogBinding.root)
            .setPositiveButton("Добавить") { _, _ ->
                val name = dialogBinding.etName.text.toString().trim()
                val address = dialogBinding.etAddress.text.toString().trim()
                val latitude = dialogBinding.etLatitude.text.toString().toDoubleOrNull()
                val longitude = dialogBinding.etLongitude.text.toString().toDoubleOrNull()
                
                if (name.isNotEmpty() && address.isNotEmpty() && latitude != null && longitude != null) {
                    addLocation(name, address, latitude, longitude)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Пожалуйста, заполните все поля корректно",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun addLocation(name: String, address: String, latitude: Double, longitude: Double) {
        binding.progressBar.visibility = View.VISIBLE
        
        // Генерируем уникальный код для QR
        val qrCode = UUID.randomUUID().toString()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Создание новой локации
                val newLocation = WorkLocation(
                    name = name,
                    address = address,
                    latitude = latitude,
                    longitude = longitude,
                    qrCode = qrCode
                )
                
                locationRepository.insert(newLocation)
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Рабочая зона добавлена",
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
    
    private fun showQrCodeDialog(location: WorkLocation) {
        try {
            // Генерация QR-кода
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                location.qrCode,
                BarcodeFormat.QR_CODE,
                500,
                500
            )
            
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            
            // Создание диалогового окна с QR-кодом
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(com.decoroomsteel.dstracker.R.layout.dialog_qr_code, null)
            
            val imageView = dialogView.findViewById<android.widget.ImageView>(
                com.decoroomsteel.dstracker.R.id.iv_qr_code
            )
            imageView.setImageBitmap(bitmap)
            
            val tvLocationName = dialogView.findViewById<android.widget.TextView>(
                com.decoroomsteel.dstracker.R.id.tv_location_name
            )
            tvLocationName.text = location.name
            
            AlertDialog.Builder(requireContext())
                .setTitle("QR-код рабочей зоны")
                .setView(dialogView)
                .setPositiveButton("Закрыть", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Ошибка генерации QR-кода: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun showDeleteLocationDialog(location: WorkLocation) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить рабочую зону")
            .setMessage("Вы уверены, что хотите удалить рабочую зону '${location.name}'?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteLocation(location)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun deleteLocation(location: WorkLocation) {
        binding.progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Вместо физического удаления помечаем как неактивную
                locationRepository.update(location.copy(active = false))
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Рабочая зона удалена",
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 