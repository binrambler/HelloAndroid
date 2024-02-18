package com.example.hello

import android.annotation.SuppressLint
import android.util.Log
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

class BarcodeAnalyzer(private val context: Context) : ImageAnalysis.Analyzer {
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_PDF417
        )
        .enableAllPotentialBarcodes()
        .build()

    private val scanner = BarcodeScanning.getClient(options)


    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image
            ?.let { image ->
                scanner.process(
                    InputImage.fromMediaImage(
                        image, imageProxy.imageInfo.rotationDegrees
                    )
                ).addOnSuccessListener { barcode ->
                    barcode?.takeIf { it.isNotEmpty() }
                        ?.mapNotNull { it.rawValue }
                        ?.joinToString(",")
                        ?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                }.addOnCompleteListener {
                    imageProxy.close()
                }
            }
    }
}


class MlKitCodeAnalyzer(private val barcodeListener: Context) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        defaultOptions()
    )

    private fun defaultOptions() = BarcodeScannerOptions.Builder().setBarcodeFormats(
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8,
    ).build()

    @OptIn(ExperimentalGetImage::class)
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: return
        val mlImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        val currentTimestamp = System.currentTimeMillis()
        scanner.process(mlImage).addOnSuccessListener { barcodes ->
            barcodes.firstOrNull()?.let {
                it.rawValue?.let {
                    Toast.makeText(barcodeListener, it, Toast.LENGTH_SHORT).show()
                    barcodeListener }
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
            val selector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            preview.setSurfaceProvider(previewView.surfaceProvider)

//            val imageAnalysis = ImageAnalysis.Builder().build()
//            imageAnalysis.setAnalyzer(
//                ContextCompat.getMainExecutor(context),
//                BarcodeAnalyzer(context)
//            )
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(context),
                MlKitCodeAnalyzer(context)
//                val analyzer : ImageAnalysis . Analyzer = MlKitCodeAnalyzer (
//                    barcodeListener = onData,
            )

            runCatching {
                cameraProviderFuture.get().bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageAnalysis
                )
            }.onFailure {
                Log.e("CAMERA", "Camera bind error ${it.localizedMessage}", it)
            }
            previewView
        }
    )
}
