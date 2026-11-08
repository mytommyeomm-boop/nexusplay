package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonGradient
import com.nexusplay.launcher.ui.theme.NeonPurple
import com.nexusplay.launcher.ui.theme.TextPrimary
import com.nexusplay.launcher.ui.theme.TextSecondary

/** Startup screen: spinning neon ring around the logo and a progress bar. Shown once per launch. */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
    }
    val transition = rememberInfiniteTransition(label = "splash")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "spin"
    )

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(156.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 4.dp.toPx()
                    val inset = stroke
                    rotate(spin) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(Color.Transparent, NeonCyan, NeonPurple, Color.Transparent)
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - inset * 2, size.height - inset * 2),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
                LogoMark(76.dp)
            }
            Spacer(Modifier.height(28.dp))
            BasicText(
                text = "NEXUSPLAY",
                style = TextStyle(
                    brush = Brush.horizontalGradient(listOf(TextPrimary, NeonCyan, NeonPurple)),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 7.sp
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "PC game launcher",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(36.dp))
            Box(
                Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .drawBehind {
                        drawRect(NeonGradient, size = Size(size.width * progress.value, size.height))
                    }
            )
        }
    }
}
