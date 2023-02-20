package com.fitness.composemlkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fitness.composemlkit.poseGraphic.CameraPreview
import com.fitness.composemlkit.poseGraphic.PoseGraphic
import com.fitness.composemlkit.ui.theme.ComposeMLKitTheme
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    // designated executor for camera so UI thread doesn't get blocked
    lateinit var cameraExecutor: ExecutorService

    // designated executor for classifier so UI thread doesn't get blocked
    lateinit var classificationExecutor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        classificationExecutor = Executors.newSingleThreadExecutor()

        // Base pose detector with streaming frames, when depending on the pose-detection sdk
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        val poseDetector = PoseDetection.getClient(options)

        setContent {
            ComposeMLKitTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    CameraPreview(
//                        poseDetector = poseDetector,
//                        classificationExecutor = classificationExecutor,
//                        cameraExecutor = cameraExecutor
//                    )

//                    CameraPreview()
                    PoseGraphic(
                        poseDetector = poseDetector,
                        classificationExecutor = classificationExecutor,
                        cameraExecutor = cameraExecutor
                    )

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
