package com.tradeguru.electrical.ui.views

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tradeguru.electrical.R
import java.nio.ByteBuffer

@Composable
fun CameraView(onCapture: (ByteArray) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(false) }
    var flashOn by remember { mutableStateOf(false) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }

    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermission) {
            AndroidView(factory = { ctx ->
                PreviewView(ctx).also { pv ->
                    val future = ProcessCameraProvider.getInstance(ctx)
                    future.addListener({
                        val provider = future.get()
                        val preview = Preview.Builder().build().also { it.surfaceProvider = pv.surfaceProvider }
                        provider.unbindAll()
                        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }, modifier = Modifier.fillMaxSize())
        } else {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painterResource(R.drawable.ic_bolt), null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                Text("Camera not available", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(top = 16.dp))
            }
        }

        Box(Modifier.fillMaxSize()) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
            ) { Icon(Icons.Default.Close, "Close camera", tint = Color.White) }

            Row(Modifier.align(Alignment.BottomCenter).padding(horizontal = 32.dp, vertical = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { flashOn = !flashOn }, modifier = Modifier.size(44.dp)) {
                    Icon(painterResource(R.drawable.ic_bolt), if (flashOn) "Flash on" else "Flash off", tint = if (flashOn) Color.Yellow else Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    imageCapture.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val buf: ByteBuffer = image.planes[0].buffer; val bytes = ByteArray(buf.remaining()); buf.get(bytes); image.close(); onCapture(bytes)
                        }
                        override fun onError(e: ImageCaptureException) {}
                    })
                }, modifier = Modifier.size(70.dp)) {
                    Box(Modifier.size(70.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(64.dp).clip(CircleShape).background(Color.White).border(2.dp, Color.Black.copy(alpha = 0.2f), CircleShape))
                    }
                }
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(44.dp))
            }
        }
    }
}
