package com.decoroomsteel.dstracker.ui.scanner

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

/**
 * Анализатор изображений для сканирования QR-кода
 */
class QrCodeAnalyzer(private val onQrCodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    override fun analyze(image: ImageProxy) {
        // Проверка поддерживаемого формата
        if (image.format !in supportedImageFormats) {
            image.close()
            return
        }

        // Получение байтов изображения в формате YUV
        val bytes = image.planes[0].buffer.toByteArray()
        val width = image.width
        val height = image.height

        // Источник освещенности для ZXing
        val source = PlanarYUVLuminanceSource(
            bytes,
            width,
            height,
            0,
            0,
            width,
            height,
            false
        )

        // Создание бинарного изображения для распознавания
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            // Настройка типов кодов для распознавания
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
            )

            // Попытка декодирования QR-кода
            val result = MultiFormatReader().decode(binaryBitmap, hints)
            
            // Если QR-код найден, вызываем обратный вызов
            if (result != null) {
                onQrCodeScanned(result.text)
            }
        } catch (e: Exception) {
            // Исключение означает, что QR-код не найден или не может быть декодирован
        } finally {
            // Обязательно освобождаем ресурсы изображения
            image.close()
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
} 