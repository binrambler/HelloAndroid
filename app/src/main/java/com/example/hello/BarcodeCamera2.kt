package com.example.hello

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.Composable
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MlKitCodeAnalyzer(private val barcodeListener: Context) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
//        Barcode.FORMAT_QR_CODE,
//        Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_CODE_128,
//        Barcode.FORMAT_CODE_39,
//        Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_EAN_13,
//        Barcode.FORMAT_UPC_A,
//        Barcode.FORMAT_UPC_E,
//        Barcode.FORMAT_PDF417
        ).build()

    private val scanner = BarcodeScanning.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: return
        val mlImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        val currentTimestamp = System.currentTimeMillis()
        scanner.process(mlImage).addOnSuccessListener { barcodes ->
            barcodes.firstOrNull()?.let {barcode ->
                barcode.rawValue?.let {it ->
                    Toast.makeText(barcodeListener, it, Toast.LENGTH_SHORT).show()
//                    barcodeListener
                }
            }
        }.addOnCompleteListener {
            // Позволяет производить сканирование раз в секунду
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000 - (System.currentTimeMillis() - currentTimestamp))
                image.close()
            }
        }
    }
}

@Composable
fun CameraScreen() {
    val localContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(localContext)
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val previewView = PreviewView(context)
            val preview = Preview.Builder().build()

            // делаем, чтоб не превью с камеры не занимало весь экран
            previewView.scaleType = PreviewView.ScaleType.FILL_END

            preview.setSurfaceProvider(previewView.surfaceProvider)

            // Важно выставить backpressure-стратегию
            // STRATEGY_KEEP_ONLY_LATEST в нашем случае подходит идеально,
            // так как модель распознания кодов из mlKit работает весьма шустро
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

            // подключаем анализатор
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(context),
                MlKitCodeAnalyzer(context)
            )

            runCatching {
                cameraProviderFuture.get().bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            }.onFailure {
                Toast.makeText(context, "Camera bind error ${it.localizedMessage}", Toast.LENGTH_LONG).show()
//                Log.e("CAMERA", "Camera bind error ${it.localizedMessage}", it)
            }
            previewView
        }
    )
}
