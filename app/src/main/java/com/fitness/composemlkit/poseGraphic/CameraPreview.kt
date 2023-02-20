package com.fitness.composemlkit.poseGraphic

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetector
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraPreview(
    poseDetector: PoseDetector,
    classificationExecutor: Executor,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // setup view to display video
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }

    var poseState by remember { mutableStateOf<Pose?>(null) }
    var imgSize by remember { mutableStateOf(Pair(0, 0)) }
    var previewSize by remember { mutableStateOf(Pair(0f, 0f)) }
//
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }


    val imageAnalyzer = ImageAnalysis.Analyzer { imgProxy ->
        poseDetector.process(imgProxy.image!!, imgProxy.imageInfo.rotationDegrees)
            .continueWith(classificationExecutor) { poseTask ->
                val pose = poseTask.result
//                poseState = pose

                // TODo Look at the ff github code to organize imageAnalyzer [for example, the imgProxy should be closed onFailure]
                // reason for width/height calculation: https://stackoverflow.com/questions/63902317/camerax-image-analysiss-imageproxy-size-and-previewview-size-are-not-the-same
                // width/height calculation: https://github.com/husaynhakeem/android-playground/blob/master/FaceDetectorSample/app/src/main/java/com/husaynhakeem/facedetectorsample/AnalysisFaceDetector.kt
                // In order to correctly display the face bounds, the orientation of the analyzed
                // image and that of the viewfinder have to match. Which is why the dimensions of
                // the analyzed image are reversed if its rotation information is 90 or 270.
                val rotation = imgProxy.imageInfo.rotationDegrees
                val reverseDimens = rotation == 90 || rotation == 270
                imgSize = if (reverseDimens) Pair(imgProxy.height, imgProxy.width)
                else Pair(imgProxy.width, imgProxy.height)

                imgProxy.close()
            }
    }

    // setup Image Analyzer using
    val imageAnalysisBuilder = ImageAnalysis.Builder()
        //.setTargetResolution(Size(720, 1280)) // TODO??
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//        .setOutputImageRotationEnabled(true) // TODO what's this??
        .build()
        .also {
            // Sets the analyzer to receive and analyze images.
            it.setAnalyzer(cameraExecutor, imageAnalyzer)
        }

    CameraSurfaceProvider(
        context = context,
        preview = preview,
        previewView = previewView,
        imageAnalysisBuilder = imageAnalysisBuilder,
        lifecycleOwner = lifecycleOwner,
        cameraLensFacingAngle = lensFacing
    )

    CameraPreview(
        previewView = previewView
    )
}

@Composable
fun CameraPreview(
    previewView: PreviewView
) {
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            {
                previewView.apply {
                    keepScreenOn = true // prevent screen from turning off
                }
            },
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

/**
 * Camera surface provider
 * CameraX code adapted from: https://www.youtube.com/watch?v=GRHQcl496P4&t=649s
 * @param cameraLensFacingAngle whether the camera is facing front or back
 */
@Composable
fun CameraSurfaceProvider(
    context: Context,
    preview: Preview,
    previewView: PreviewView,
    imageAnalysisBuilder: ImageAnalysis,
    lifecycleOwner: LifecycleOwner,
    cameraLensFacingAngle: Int
) {
    // setup the camera
    LaunchedEffect(cameraLensFacingAngle) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraLensFacingAngle)
            .build()
        val cameraProvider = context.getCameraProvider()
        // unbind anything that might be already bound to it
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            imageAnalysisBuilder, // TODo move this within here
            preview
        )
        // Update the preview - this shows what's being processed by the camera feed
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

//// TODO delete above

@Composable
fun PoseGraphic(
    poseDetector: PoseDetector,
    classificationExecutor: Executor,
    cameraExecutor: ExecutorService // TODO not used
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imgSize by remember { mutableStateOf(Pair(0, 0)) }

    val imageAnalyzer = ImageAnalysis.Analyzer { imgProxy ->
        poseDetector.process(imgProxy.image!!, imgProxy.imageInfo.rotationDegrees)
            .continueWith(classificationExecutor) { poseTask ->
                val pose = poseTask.result
                println("++Pose: " + pose.allPoseLandmarks.getOrNull(0)?.position3D?.x)
//                poseState = pose

                // TODo Look at the ff github code to organize imageAnalyzer [for example, the imgProxy should be closed onFailure]
                // reason for width/height calculation: https://stackoverflow.com/questions/63902317/camerax-image-analysiss-imageproxy-size-and-previewview-size-are-not-the-same
                // width/height calculation: https://github.com/husaynhakeem/android-playground/blob/master/FaceDetectorSample/app/src/main/java/com/husaynhakeem/facedetectorsample/AnalysisFaceDetector.kt
                // In order to correctly display the face bounds, the orientation of the analyzed
                // image and that of the viewfinder have to match. Which is why the dimensions of
                // the analyzed image are reversed if its rotation information is 90 or 270.
                val rotation = imgProxy.imageInfo.rotationDegrees
                val reverseDimens = rotation == 90 || rotation == 270
                imgSize = if (reverseDimens) Pair(imgProxy.height, imgProxy.width)
                else Pair(imgProxy.width, imgProxy.height)

                imgProxy.close()
            }
    }

    // setup Image Analyzer using
    val imageAnalysisBuilder = ImageAnalysis.Builder()
        //.setTargetResolution(Size(720, 1280)) // TODO??
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//        .setOutputImageRotationEnabled(true) // TODO what's this??
        .build()
        .also {
            // Sets the analyzer to receive and analyze images.
            it.setAnalyzer(cameraExecutor, imageAnalyzer)
        }

    CameraPreviewTest(
        imageAnalysisBuilder = imageAnalysisBuilder,
//        cameraExecutor = cameraExecutor, // TODO delete
        context = context,
        lifecycleOwner = lifecycleOwner
    )
}

/**
 * src: https://proandroiddev.com/compose-camerax-on-android-58578f37e6df
 */
@Composable
fun CameraPreviewTest(
    imageAnalysisBuilder: ImageAnalysis,
    context: Context,
    lifecycleOwner: LifecycleOwner,
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalysisBuilder,
                    preview
                )
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize(),
    )
}
