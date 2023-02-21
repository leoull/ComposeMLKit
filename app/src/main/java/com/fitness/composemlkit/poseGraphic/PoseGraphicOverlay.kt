package com.fitness.composemlkit.poseGraphic

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_ANKLE
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_ELBOW
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_FOOT_INDEX
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_HEEL
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_HIP
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_INDEX
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_KNEE
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_PINKY
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_SHOULDER
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_THUMB
import com.google.mlkit.vision.pose.PoseLandmark.LEFT_WRIST
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_ANKLE
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_ELBOW
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_FOOT_INDEX
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_HEEL
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_HIP
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_INDEX
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_KNEE
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_PINKY
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_SHOULDER
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_THUMB
import com.google.mlkit.vision.pose.PoseLandmark.RIGHT_WRIST
import kotlin.math.absoluteValue

/**
 * @author leoul
 * Draw the detected pose in preview.
 */
@Composable
fun PoseGraphicOverlay(
    pose: Pose,
    scaleFactor: Float,
    postScaleWidthOffset: Float,
    postScaleHeightOffset: Float
) {
    // TODO what's this
    val infiniteTransition = rememberInfiniteTransition()
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // TODO ??
        // Unused but required for draw update
        // There may be a better way to do this
        @Suppress("UNUSED_VARIABLE") val progress = animatedProgress.absoluteValue

        val poseLandmarks = pose.allPoseLandmarks
        if (poseLandmarks.isEmpty()) return@Canvas

        // draw point for each landmark. Landmarks - https://developers.google.com/ml-kit/vision/pose-detection
        poseLandmarks.forEach {
            this.drawPoint(
                it, scaleFactor, postScaleWidthOffset, postScaleHeightOffset
            )
        }

        val leftShoulder = pose.getPoseLandmark(LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(LEFT_HIP)
        val rightHip = pose.getPoseLandmark(RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(RIGHT_KNEE)
        val leftAnkle = pose.getPoseLandmark(LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(RIGHT_ANKLE)

        val leftPinky = pose.getPoseLandmark(LEFT_PINKY)
        val rightPinky = pose.getPoseLandmark(RIGHT_PINKY)
        val leftIndex = pose.getPoseLandmark(LEFT_INDEX)
        val rightIndex = pose.getPoseLandmark(RIGHT_INDEX)
        val leftThumb = pose.getPoseLandmark(LEFT_THUMB)
        val rightThumb = pose.getPoseLandmark(RIGHT_THUMB)
        val leftHeel = pose.getPoseLandmark(LEFT_HEEL)
        val rightHeel = pose.getPoseLandmark(RIGHT_HEEL)
        val leftFootIndex = pose.getPoseLandmark(LEFT_FOOT_INDEX)
        val rightFootIndex = pose.getPoseLandmark(RIGHT_FOOT_INDEX)

        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftShoulder, rightShoulder
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftHip, rightHip
        )

        // Left body
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftShoulder, leftElbow
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftElbow, leftWrist
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftShoulder, leftHip
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftHip, leftKnee
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftKnee, leftAnkle
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftWrist, leftThumb
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftWrist, leftPinky
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftWrist, leftIndex
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftIndex, leftPinky
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftAnkle, leftHeel
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, leftHeel, leftFootIndex
        )
        // Right body
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightShoulder, rightElbow
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightElbow, rightWrist
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightShoulder, rightHip
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightHip, rightKnee
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightKnee, rightAnkle
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightWrist, rightThumb
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightWrist, rightPinky
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightWrist, rightIndex
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightIndex, rightPinky
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightAnkle, rightHeel
        )
        drawLine(
            scaleFactor, postScaleWidthOffset, postScaleHeightOffset, rightHeel, rightFootIndex
        )
    }
}

private fun DrawScope.drawLine(
    scaleFactor: Float,
    postScaleWidthOffset: Float,
    postScaleHeightOffset: Float,
    startLandmark: PoseLandmark?,
    endLandmark: PoseLandmark?,
    color: Color = Color.White
) {
    val start = startLandmark!!.position3D
    val end = endLandmark!!.position3D

    this.drawLine(
        color = color,
        start = Offset(
            translateX(
                start.x, scaleFactor, postScaleWidthOffset = postScaleWidthOffset
            ),
            translateY(start.y, scaleFactor, postScaleHeightOffset = postScaleHeightOffset)
        ),
        end = Offset(
            translateX(
                end.x, scaleFactor, postScaleWidthOffset = postScaleWidthOffset
            ),
            translateY(end.y, scaleFactor, postScaleHeightOffset = postScaleHeightOffset)
        ),
        strokeWidth = STROKE_WIDTH
    )
}

fun DrawScope.drawPoint(
    landmark: PoseLandmark,
    scaleFactor: Float,
    postScaleWidthOffset: Float,
    postScaleHeightOffset: Float,
    color: Color = Color.White
) {
    val point = landmark.position3D
    this.drawCircle(
        center = Offset(
            translateX(x = point.x, scaleFactor, postScaleWidthOffset = postScaleWidthOffset),
            translateY(y = point.y, scaleFactor, postScaleHeightOffset = postScaleHeightOffset)
        ),
        radius = DOT_RADIUS,
        color = color
    )
}

/**
 * TODO move to shared
 * Adjusts the x coordinate from the image's coordinate system to the view coordinate system.
 */
fun translateX(
    x: Float,
    scaleFactor: Float,
    postScaleWidthOffset: Float
): Float {
    // Adjusts the supplied value from the image scale to the view scale
    val scale = x * scaleFactor
    return scale - postScaleWidthOffset
}

/**
 *  * TODO move to shared
 * Adjusts the y coordinate from the image's coordinate system to the view coordinate system.
 */
fun translateY(
    y: Float, scaleFactor: Float,
    postScaleHeightOffset: Float
): Float {
    // Adjusts the supplied value from the image scale to the view scale
    val scale = y * scaleFactor
    return scale - postScaleHeightOffset
}

private const val DOT_RADIUS = 8.0f
private const val STROKE_WIDTH = 10.0f
