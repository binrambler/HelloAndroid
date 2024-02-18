package com.example.hello

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

const val TAG = "bin"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalGetImage::class)
    @kotlin.OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
//            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
//                val permission = arrayOf<String>(Manifest.permission.CAMERA)
//                requestPermissions(permission, 112)
//            }

            val cameraPermission = rememberPermissionState(
                android.Manifest.permission.CAMERA
            )
            LaunchedEffect(key1 = true) {
                if (!cameraPermission.status.isGranted) {
                    cameraPermission.launchPermissionRequest()
                }
            }
            CameraScreen()
//            val camera = remember { BarcodeCamera() }
//            var lastScannedBarcode by remember { mutableStateOf<String?>(null) }
//
//            Column(modifier = Modifier.fillMaxSize()) {
//                Row (modifier = Modifier.fillMaxWidth().weight(0.4F)){
//                    Box(modifier= Modifier.fillMaxSize()){
//                        if (cameraPermission.status.isGranted) {
//                            camera.CameraPreview(
//                                onBarcodeScanned = { barcode ->
//                                    barcode?.displayValue?.let {
//                                        lastScannedBarcode = it
//                                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
//                                    }
//                                }
//                            )
//                        }
//                    }
//                }
//                Row (modifier = Modifier.fillMaxWidth().weight(0.6F)){
//
//                }
//            }
        }
    }
}


