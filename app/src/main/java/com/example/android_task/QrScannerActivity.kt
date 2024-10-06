package com.example.android_task

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QrScannerActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        // Get camera provider instance
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val previewView = findViewById<PreviewView>(R.id.previewView)

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val imageAnalyzer = ImageAnalysis.Builder().build().also {
            it.setAnalyzer(ContextCompat.getMainExecutor(this), QrCodeAnalyzer { qrCode ->
                val resultIntent = Intent()
                resultIntent.putExtra("QR_CODE", qrCode)
                setResult(RESULT_OK, resultIntent)
                finish() // Close the scanner
            })
        }

        // Bind the camera to the lifecycle
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
    }

    class QrCodeAnalyzer(private val onQrCodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
        private val barcodeScanner = BarcodeScanning.getClient()

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val qrCode = barcodes.firstOrNull { it.rawValue != null }?.rawValue
                        qrCode?.let {
                            onQrCodeDetected(it)
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close() // Close the image proxy when done
                    }
            }
        }
    }
}