package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.ExerciseAnimation
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ExerciseIllustration(
    animationType: ExerciseAnimation,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    // Continuous infinite progression for joint calculations
    val infiniteTransition = rememberInfiniteTransition(label = "illustration")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * java.lang.Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val activeProgress = if (isPaused) 0f else animationProgress

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val baseLineY = centerY + 80f
        
        // Draw elegant athletic ground line
        if (animationType != ExerciseAnimation.REST) {
            drawLine(
                color = onSurfaceVariant.copy(alpha = 0.15f),
                start = Offset(20f, baseLineY + 20f),
                end = Offset(width - 20f, baseLineY + 20f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }

        when (animationType) {
            ExerciseAnimation.JUMPING_JACKS -> {
                // Alternately jump out and raise arms
                val cycle = sin(activeProgress) // -1.0 to 1.0
                val ratio = (cycle + 1f) / 2f // 0 to 1
                
                // Coordinates
                val headCenter = Offset(centerX, centerY - 100f)
                val hip = Offset(centerX, centerY)
                
                // Feet start together, jump 80dp apart
                val legWidth = 40f + (ratio * 70f)
                val leftFoot = Offset(centerX - legWidth, baseLineY)
                val rightFoot = Offset(centerX + legWidth, baseLineY)
                
                // Hands start down, go overhead
                val armAngle = Math.toRadians((30.0 + (ratio * 120.0))).toFloat()
                val armLength = 100f
                val leftHand = Offset(
                    centerX - armLength * sin(armAngle),
                    centerY - 50f - armLength * cos(armAngle)
                )
                val rightHand = Offset(
                    centerX + armLength * sin(armAngle),
                    centerY - 50f - armLength * cos(armAngle)
                )

                // Head
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                // Spine
                drawLine(color = secondaryColor, start = headCenter, end = hip, strokeWidth = 12f, cap = StrokeCap.Round)
                // Left Leg
                drawLine(color = secondaryColor, start = hip, end = leftFoot, strokeWidth = 10f, cap = StrokeCap.Round)
                // Right Leg
                drawLine(color = secondaryColor, start = hip, end = rightFoot, strokeWidth = 10f, cap = StrokeCap.Round)
                // Left Arm
                drawLine(color = tertiaryColor, start = Offset(centerX, centerY - 80f), end = leftHand, strokeWidth = 10f, cap = StrokeCap.Round)
                // Right Arm
                drawLine(color = tertiaryColor, start = Offset(centerX, centerY - 80f), end = rightHand, strokeWidth = 10f, cap = StrokeCap.Round)
            }

            ExerciseAnimation.SQUAT -> {
                // Drop hips down and up
                val cycle = sin(activeProgress)
                val ratio = (cycle + 1f) / 2f // 0 to 1 (0 = standing, 1 = deep squat)
                
                val standY = centerY - 50f
                val dipY = standY + 70f * ratio
                
                val hip = Offset(centerX, dipY)
                val headCenter = Offset(centerX, dipY - 100f)
                
                val kneeLeftY = dipY + ((baseLineY - dipY) * 0.45f)
                val kneeLeft = Offset(centerX - 60f - (ratio * 30f), kneeLeftY)
                val leftFoot = Offset(centerX - 50f, baseLineY)

                val kneeRightY = dipY + ((baseLineY - dipY) * 0.45f)
                val kneeRight = Offset(centerX + 60f + (ratio * 30f), kneeRightY)
                val rightFoot = Offset(centerX + 50f, baseLineY)

                // Hands held forward
                val handXOffset = 90f
                val leftHand = Offset(centerX - handXOffset, dipY - 80f)
                val rightHand = Offset(centerX + handXOffset, dipY - 80f)

                // Draw Head
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                // Spine
                drawLine(color = secondaryColor, start = headCenter, end = hip, strokeWidth = 12f, cap = StrokeCap.Round)
                // Left Leg (folded)
                drawLine(color = secondaryColor, start = hip, end = kneeLeft, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = kneeLeft, end = leftFoot, strokeWidth = 10f, cap = StrokeCap.Round)
                // Right Leg (folded)
                drawLine(color = secondaryColor, start = hip, end = kneeRight, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = kneeRight, end = rightFoot, strokeWidth = 10f, cap = StrokeCap.Round)
                // Arms forward
                drawLine(color = tertiaryColor, start = Offset(centerX, dipY - 60f), end = leftHand, strokeWidth = 9f, cap = StrokeCap.Round)
                drawLine(color = tertiaryColor, start = Offset(centerX, dipY - 60f), end = rightHand, strokeWidth = 9f, cap = StrokeCap.Round)
            }

            ExerciseAnimation.PUSH_UP -> {
                // Diagonally lying figure going up and down
                val cycle = sin(activeProgress)
                val ratio = (cycle + 1f) / 2f // 0 = fully pushed up, 1 = touching floor
                
                val feet = Offset(centerX - 120f, baseLineY)
                val headY = baseLineY - 100f + (ratio * 40f)
                val headX = centerX + 110f
                val headCenter = Offset(headX, headY)
                
                val shoulder = Offset(centerX + 80f, headY + 30f)
                val hipY = baseLineY - 50f + (ratio * 20f)
                val hip = Offset(centerX - 30f, hipY)

                val elbowY = baseLineY + 20f
                val leftHand = Offset(centerX + 70f, baseLineY)
                val elbow = Offset(centerX + 40f + (ratio * 25f), elbowY - 30f * (1f - ratio))

                // Head
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                // Back & Glutes line
                drawLine(color = secondaryColor, start = feet, end = hip, strokeWidth = 12f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = hip, end = shoulder, strokeWidth = 12f, cap = StrokeCap.Round)
                // Arm bend
                drawLine(color = tertiaryColor, start = shoulder, end = elbow, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = tertiaryColor, start = elbow, end = leftHand, strokeWidth = 10f, cap = StrokeCap.Round)
            }

            ExerciseAnimation.PLANK -> {
                // Horizontal flat plank (breathing/pulsing core ring)
                val pulse = sin(activeProgress) * 60f
                val feet = Offset(centerX - 130f, baseLineY - 20f)
                val headCenter = Offset(centerX + 130f, baseLineY - 80f)
                val shoulder = Offset(centerX + 90f, baseLineY - 70f)
                val hip = Offset(centerX - 20f, baseLineY - 45f)
                
                val elbow = Offset(centerX + 90f, baseLineY)
                val hand = Offset(centerX + 120f, baseLineY)

                // Glowing core aura
                drawCircle(
                    color = primaryColor.copy(alpha = 0.12f),
                    radius = 90f + pulse * 0.3f,
                    center = hip
                )

                // Head
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                // Horizontal Spine
                drawLine(color = secondaryColor, start = feet, end = hip, strokeWidth = 12f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = hip, end = shoulder, strokeWidth = 12f, cap = StrokeCap.Round)
                // Arm forearm support
                drawLine(color = tertiaryColor, start = shoulder, end = elbow, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = tertiaryColor, start = elbow, end = hand, strokeWidth = 10f, cap = StrokeCap.Round)
                // Toe support
                drawLine(color = secondaryColor, start = feet, end = Offset(centerX - 135f, baseLineY), strokeWidth = 8f, cap = StrokeCap.Round)
            }

            ExerciseAnimation.MOUNTAIN_CLIMBER -> {
                // Diagonal push-up position, alternating legs running
                val cycle = sin(activeProgress)
                val ratio = (cycle + 1f) / 2f
                
                val hands = Offset(centerX + 90f, baseLineY)
                val shoulder = Offset(centerX + 80f, baseLineY - 105f)
                val headCenter = Offset(centerX + 120f, baseLineY - 120f)
                val hip = Offset(centerX - 40f, baseLineY - 60f)

                // Left Leg extended
                val leftFoot = Offset(centerX - 130f + (ratio * 40f), baseLineY)
                
                // Right Leg drawn completely to chest
                val rightKnee = Offset(centerX + 10f - (ratio * 35f), baseLineY - 45f)
                val rightFoot = Offset(centerX, baseLineY - 15f)

                // Head
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                // Spine
                drawLine(color = secondaryColor, start = shoulder, end = hip, strokeWidth = 12f, cap = StrokeCap.Round)
                // Hand columns
                drawLine(color = tertiaryColor, start = shoulder, end = hands, strokeWidth = 10f, cap = StrokeCap.Round)
                // Left Leg
                drawLine(color = secondaryColor, start = hip, end = leftFoot, strokeWidth = 10f, cap = StrokeCap.Round)
                // Running Right Leg
                drawLine(color = secondaryColor, start = hip, end = rightKnee, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = rightKnee, end = rightFoot, strokeWidth = 10f, cap = StrokeCap.Round)
            }

            ExerciseAnimation.LUNGES -> {
                // One leg backward and one forward in 90 degrees
                val cycle = sin(activeProgress)
                val ratio = (cycle + 1f) / 2f
                
                val headCenter = Offset(centerX, centerY - 100f + (20f * ratio))
                val hip = Offset(centerX, centerY + (20f * ratio))
                
                // Front foot flat
                val frontKnee = Offset(centerX + 50f, centerY + 60f + (15f * ratio))
                val frontFoot = Offset(centerX + 50f, baseLineY)

                // Back knee bent almost touching floor
                val backKnee = Offset(centerX - 40f, baseLineY - 30f + (30f * ratio))
                val backFoot = Offset(centerX - 100f, baseLineY)

                // Head
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                // Spine
                drawLine(color = secondaryColor, start = headCenter, end = hip, strokeWidth = 12f, cap = StrokeCap.Round)
                // Legs
                drawLine(color = secondaryColor, start = hip, end = frontKnee, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = frontKnee, end = frontFoot, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = hip, end = backKnee, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = backKnee, end = backFoot, strokeWidth = 10f, cap = StrokeCap.Round)
            }

            ExerciseAnimation.CRUNCHES -> {
                // Raising head and back up off floor
                val cycle = sin(activeProgress)
                val ratio = abs(cycle) // 0 to 1
                
                val hips = Offset(centerX - 40f, baseLineY)
                val knees = Offset(centerX + 30f, baseLineY - 90f)
                val feet = Offset(centerX + 80f, baseLineY)

                // Shoulder/head lifts off
                val headX = centerX - 120f + (ratio * 40f)
                val headY = baseLineY - 24f - (ratio * 55f)
                val headCenter = Offset(headX, headY)
                val shoulder = Offset(centerX - 80f + (ratio * 35f), baseLineY - 12f - (ratio * 40f))

                // Knees bent
                drawLine(color = secondaryColor, start = hips, end = knees, strokeWidth = 11f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = knees, end = feet, strokeWidth = 11f, cap = StrokeCap.Round)
                // Spine raising
                drawLine(color = secondaryColor, start = hips, end = shoulder, strokeWidth = 12f, cap = StrokeCap.Round)
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                // Hands behind head
                drawLine(color = tertiaryColor, start = shoulder, end = Offset(headX - 10f, headY + 30f), strokeWidth = 8f, cap = StrokeCap.Round)
            }

            ExerciseAnimation.RUSSIAN_TWIST -> {
                // V sit position, arms moving left to right
                val cycle = sin(activeProgress) // -1.0 to 1.0
                
                val hips = Offset(centerX - 20f, baseLineY)
                val headCenter = Offset(centerX - 90f, baseLineY - 100f)
                val knees = Offset(centerX + 60f, baseLineY - 60f)
                val feet = Offset(centerX + 110f, baseLineY - 30f)

                val shoulder = Offset(centerX - 60f, baseLineY - 80f)
                val ballPos = Offset(centerX + (cycle * 50f), baseLineY - 20f)

                // Head
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                // Spine & legs (V position)
                drawLine(color = secondaryColor, start = hips, end = shoulder, strokeWidth = 12f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = hips, end = knees, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = knees, end = feet, strokeWidth = 10f, cap = StrokeCap.Round)
                // Arms clutching ball
                drawLine(color = tertiaryColor, start = shoulder, end = ballPos, strokeWidth = 8f, cap = StrokeCap.Round)
                // Energetic weight/ball
                drawCircle(color = primaryColor, radius = 18f, center = ballPos)
                drawCircle(color = tertiaryColor, radius = 22f, center = ballPos, style = Stroke(3f))
            }

            ExerciseAnimation.FLUTTER_KICKS -> {
                // Lying on back, dynamic legs kicking scissor style
                val cycle = sin(activeProgress)
                val ratio1 = cycle
                val ratio2 = -cycle
                
                val headCenter = Offset(centerX - 120f, baseLineY - 30f)
                val hips = Offset(centerX - 30f, baseLineY - 10f)
                val neck = Offset(centerX - 100f, baseLineY - 15f)

                val footLeft = Offset(centerX + 120f, baseLineY - 50f + (ratio1 * 35f))
                val footRight = Offset(centerX + 120f, baseLineY - 50f + (ratio2 * 35f))

                // Head
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                // Body flat
                drawLine(color = secondaryColor, start = neck, end = hips, strokeWidth = 12f, cap = StrokeCap.Round)
                // Left leg (fluttering)
                drawLine(color = secondaryColor, start = hips, end = footLeft, strokeWidth = 10f, cap = StrokeCap.Round)
                // Right leg (fluttering)
                drawLine(color = tertiaryColor, start = hips, end = footRight, strokeWidth = 10f, cap = StrokeCap.Round)
            }

            ExerciseAnimation.GLUTE_BRIDGE -> {
                // Raising glutes, heels on floor
                val cycle = sin(activeProgress)
                val ratio = (cycle + 1f) / 2f
                
                val headCenter = Offset(centerX - 110f, baseLineY - 15f)
                val shoulder = Offset(centerX - 80f, baseLineY - 20f)
                val feet = Offset(centerX + 80f, baseLineY)
                val knees = Offset(centerX + 60f, baseLineY - 60f)

                // Hip bridge raises
                val hipY = baseLineY - 15f - (ratio * 55f)
                val hip = Offset(centerX - 10f, hipY)

                // Body lines
                drawLine(color = secondaryColor, start = shoulder, end = hip, strokeWidth = 12f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = hip, end = knees, strokeWidth = 12f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = knees, end = feet, strokeWidth = 10f, cap = StrokeCap.Round)
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)

                // Tiny arrows/glow for thrust
                if (!isPaused && ratio > 0.5f) {
                    drawCircle(color = primaryColor.copy(alpha = 0.15f), radius = 40f, center = hip)
                }
            }

            ExerciseAnimation.DONKEY_KICK -> {
                // Table position, kicking one leg up
                val cycle = sin(activeProgress)
                val ratio = (cycle + 1f) / 2f
                
                val headCenter = Offset(centerX + 110f, baseLineY - 80f)
                val chest = Offset(centerX + 70f, baseLineY - 70f)
                val hips = Offset(centerX - 40f, baseLineY - 70f)

                val hands = Offset(centerX + 70f, baseLineY)
                val supportingKnee = Offset(centerX - 30f, baseLineY)

                // Active leg kicks up
                val kickingKnee = Offset(centerX - 50f - (ratio * 20f), baseLineY - 75f + (ratio * 10f))
                val kickingFoot = Offset(centerX - 80f, baseLineY - 110f - (ratio * 40f))

                // Draw Table structure
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                drawLine(color = secondaryColor, start = hips, end = chest, strokeWidth = 11f, cap = StrokeCap.Round)
                drawLine(color = tertiaryColor, start = chest, end = hands, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = hips, end = supportingKnee, strokeWidth = 10f, cap = StrokeCap.Round)
                
                // Kicking Leg and Foot
                drawLine(color = primaryColor, start = hips, end = kickingKnee, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = primaryColor, start = kickingKnee, end = kickingFoot, strokeWidth = 10f, cap = StrokeCap.Round)
            }

            ExerciseAnimation.CALF_RAISE -> {
                // Elevating on toes
                val cycle = sin(activeProgress)
                val ratio = (cycle + 1f) / 2f // 0 to 1
                
                val liftY = -30f * ratio
                val headCenter = Offset(centerX, centerY - 90f + liftY)
                val hip = Offset(centerX, centerY + 10f + liftY)
                
                val heelsLift = -20f * ratio
                val leftAnkle = Offset(centerX - 35f, baseLineY + heelsLift)
                val rightAnkle = Offset(centerX + 35f, baseLineY + heelsLift)

                val leftToe = Offset(centerX - 35f, baseLineY + 20f)
                val rightToe = Offset(centerX + 35f, baseLineY + 20f)

                // Head
                drawCircle(color = primaryColor, radius = 24f, center = headCenter)
                // Spine
                drawLine(color = secondaryColor, start = headCenter, end = hip, strokeWidth = 12f, cap = StrokeCap.Round)
                // Legs to flexing ankles
                drawLine(color = secondaryColor, start = hip, end = leftAnkle, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = secondaryColor, start = hip, end = rightAnkle, strokeWidth = 10f, cap = StrokeCap.Round)
                // Ankle to toe
                drawLine(color = tertiaryColor, start = leftAnkle, end = leftToe, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(color = tertiaryColor, start = rightAnkle, end = rightToe, strokeWidth = 10f, cap = StrokeCap.Round)
            }

            ExerciseAnimation.REST -> {
                // Expanding-contracting glowing breathing visual
                val breathCycle = sin(activeProgress)
                val scale = 1f + (breathCycle * 0.22f) // 0.78 to 1.22
                
                // Central ripple circles
                drawCircle(
                    color = primaryColor.copy(alpha = 0.08f),
                    radius = centerY * 0.8f * scale,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.15f),
                    radius = centerY * 0.55f * scale,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = tertiaryColor,
                    radius = 16f + (breathCycle * 4f),
                    center = Offset(centerX, centerY)
                )

                // Standard outer outline
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.6f),
                    radius = centerY * 0.55f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 4f)
                )
            }

            ExerciseAnimation.GENERIC -> {
                // Pulsating fitness ring
                val scale = 0.9f + sin(activeProgress) * 0.1f
                drawCircle(
                    color = primaryColor,
                    radius = 40f * scale,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 6f)
                )
            }
        }
    }
}
