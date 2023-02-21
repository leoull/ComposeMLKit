package com.fitness.composemlkit.poseGraphic

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetector
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

@Composable
fun PoseGraphic(
    poseDetector: PoseDetector,
    classificationExecutor: Executor,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewSize by remember { mutableStateOf(Pair(0f, 0f)) }
    var imgSize by remember { mutableStateOf(Pair(480, 640)) }
    var pose by remember { mutableStateOf<Pose?>(null) }

    // you can also use MlKitAnalyzer instead of my own implementation: https://developer.android.com/reference/androidx/camera/mlkit/vision/MlKitAnalyzer
    val cameraxMlKitAnalyzer = MlKitAnalyzer(
        listOf(poseDetector),
        ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
        classificationExecutor
    ) {
        pose = it.getValue(poseDetector)
    }

    val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        poseDetector.process(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            .continueWith(classificationExecutor) { poseTask ->
                pose = poseTask.result


                // reason for width/height calculation: https://stackoverflow.com/questions/63902317/camerax-image-analysiss-imageproxy-size-and-previewview-size-are-not-the-same
                // width/height calculation: https://github.com/husaynhakeem/android-playground/blob/master/FaceDetectorSample/app/src/main/java/com/husaynhakeem/facedetectorsample/AnalysisFaceDetector.kt
                // In order to correctly display the face bounds, the orientation of the analyzed
                // image and that of the viewfinder have to match. Which is why the dimensions of
                // the analyzed image are reversed if its rotation information is 90 or 270.
                val rotation = imageProxy.imageInfo.rotationDegrees
                val reverseDimens = rotation == 90 || rotation == 270
                imgSize = if (reverseDimens) Pair(imageProxy.height, imageProxy.width)
                else Pair(imageProxy.width, imageProxy.height)
                // TODO retrieve the image resolution width and height outside of this block
                // TODo retrieving it from the imageProxy is updating the imgSize for every callback of this block

                // The proxy has to be closed so the next image frame can be processed
                imageProxy.close()
            }
    }

    // setup Image Analyzer using
    val imageAnalysisBuilder = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//        .setOutputImageRotationEnabled(true) // TODO what's this??
        .build()
        .also {
            // Sets the analyzer to receive and analyze images.
            it.setAnalyzer(
                cameraExecutor,
                imageAnalyzer
            ) // cameraxMlKitAnalyzer can be used instead of imageAnalyzer
        }

    val imageAspectRatio: Float = imgSize.first.toFloat() / imgSize.second.toFloat()
    val previewHeight = previewSize.second
    // The factor of overlay View size to image size. Anything in the image coordinates need to be
    // scaled by this amount to fit with the area of overlay View.
    val scaleFactor = previewHeight / imgSize.second.toFloat()
    // The number of horizontal pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    val postScaleWidthOffset = (previewHeight * imageAspectRatio - previewSize.first) / 2
    // The number of vertical pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    val postScaleHeightOffset = 0f

    CameraPreview(
        imageAnalysisBuilder = imageAnalysisBuilder,
        context = context,
        lifecycleOwner = lifecycleOwner,
        pose = pose,
        scaleFactor = scaleFactor,
        postScaleWidthOffset = postScaleWidthOffset,
        postScaleHeightOffset = postScaleHeightOffset,
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                previewSize = Pair(it.width.toFloat(), it.height.toFloat())
            }
    )
}

/**
 * src: https://proandroiddev.com/compose-camerax-on-android-58578f37e6df
 */
@Composable
fun CameraPreview(
    imageAnalysisBuilder: ImageAnalysis,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    pose: Pose?,
    scaleFactor: Float,
    postScaleWidthOffset: Float,
    postScaleHeightOffset: Float,
    modifier: Modifier
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        // camera Preview
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
                        .requireLensFacing(LENS_FACING_FRONT)
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
            modifier = modifier,
        )

        // Pose graphic overlay
        pose?.let {
            PoseGraphicOverlay(
                pose = pose,
                scaleFactor = scaleFactor,
                postScaleWidthOffset = postScaleWidthOffset,
                postScaleHeightOffset = postScaleHeightOffset
            )
        }
    }
}
