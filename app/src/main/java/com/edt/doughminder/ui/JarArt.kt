package com.edt.doughminder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import com.edt.doughminder.ui.theme.Coral
import com.edt.doughminder.ui.theme.Cream
import com.edt.doughminder.ui.theme.Ink

enum class JarMood { HAPPY, WORRIED, ANGRY }

/**
 * A little jar of starter with a face. Mood tracks hunger:
 * fed today = happy, 1–2 days = worried, longer = angry.
 */
@Composable
fun JarArt(doughColor: Color, mood: JarMood, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // jar body
        val jarLeft = w * 0.18f
        val jarRight = w * 0.82f
        val jarTop = h * 0.30f
        val jarBottom = h * 0.92f
        drawRoundRect(
            color = Cream,
            topLeft = Offset(jarLeft, jarTop),
            size = Size(jarRight - jarLeft, jarBottom - jarTop),
            cornerRadius = CornerRadius(w * 0.10f),
        )

        // dough with a wavy top line
        val doughTop = h * (if (mood == JarMood.HAPPY) 0.48f else 0.58f)
        val wave = Path().apply {
            moveTo(jarLeft, doughTop + h * 0.03f)
            quadraticBezierTo(w * 0.32f, doughTop - h * 0.03f, w * 0.46f, doughTop + h * 0.02f)
            quadraticBezierTo(w * 0.62f, doughTop + h * 0.06f, w * 0.78f, doughTop)
            lineTo(jarRight, doughTop + h * 0.02f)
            lineTo(jarRight, jarBottom)
            lineTo(jarLeft, jarBottom)
            close()
        }
        // clip dough to jar's rounded rect
        val jarClip = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    Rect(jarLeft, jarTop, jarRight, jarBottom),
                    CornerRadius(w * 0.10f),
                )
            )
        }
        clipPath(jarClip) { drawPath(wave, doughColor) }

        // bubbles
        val bubble = doughColor.darken()
        drawCircle(bubble, radius = w * 0.030f, center = Offset(w * 0.32f, h * 0.78f))
        drawCircle(bubble, radius = w * 0.042f, center = Offset(w * 0.62f, h * 0.84f))
        drawCircle(bubble, radius = w * 0.022f, center = Offset(w * 0.48f, h * 0.88f))

        // face
        val eyeY = doughTop + h * 0.10f
        drawCircle(Ink, radius = w * 0.024f, center = Offset(w * 0.42f, eyeY))
        drawCircle(Ink, radius = w * 0.024f, center = Offset(w * 0.58f, eyeY))
        val mouth = Path()
        when (mood) {
            JarMood.HAPPY -> {
                mouth.moveTo(w * 0.44f, eyeY + h * 0.06f)
                mouth.quadraticBezierTo(w * 0.50f, eyeY + h * 0.10f, w * 0.56f, eyeY + h * 0.06f)
            }
            JarMood.WORRIED -> {
                mouth.moveTo(w * 0.44f, eyeY + h * 0.09f)
                mouth.quadraticBezierTo(w * 0.50f, eyeY + h * 0.055f, w * 0.56f, eyeY + h * 0.09f)
            }
            JarMood.ANGRY -> {
                mouth.moveTo(w * 0.44f, eyeY + h * 0.09f)
                mouth.quadraticBezierTo(w * 0.50f, eyeY + h * 0.055f, w * 0.56f, eyeY + h * 0.09f)
                // eyebrows
                drawLine(Ink, Offset(w * 0.38f, eyeY - h * 0.055f), Offset(w * 0.455f, eyeY - h * 0.03f), strokeWidth = w * 0.016f)
                drawLine(Ink, Offset(w * 0.62f, eyeY - h * 0.055f), Offset(w * 0.545f, eyeY - h * 0.03f), strokeWidth = w * 0.016f)
            }
        }
        drawPath(mouth, Ink, style = Stroke(width = w * 0.016f))

        // mason-jar lid: single slim cap, a touch wider than the jar
        drawRoundRect(
            color = Coral,
            topLeft = Offset(w * 0.16f, h * 0.22f),
            size = Size(w * 0.68f, h * 0.09f),
            cornerRadius = CornerRadius(w * 0.035f),
        )
    }
}

private fun Color.darken(factor: Float = 0.82f) =
    Color(red * factor, green * factor, blue * factor, alpha)
