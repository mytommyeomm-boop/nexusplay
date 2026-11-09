package com.nexusplay.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.TitaniumLight

/** Chamfered cartridge outline with a play wedge. Matches the launcher icon. */
@Composable
fun LogoMark(dimension: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(dimension)) {
        val s = size.minDimension
        val cut = s * 0.28f
        val inset = s * 0.04f
        val plate = Path().apply {
            moveTo(inset + cut, inset)
            lineTo(s - inset, inset)
            lineTo(s - inset, s - inset - cut)
            lineTo(s - inset - cut, s - inset)
            lineTo(inset, s - inset)
            lineTo(inset, inset + cut)
            close()
        }
        drawPath(plate, TitaniumLight, style = Stroke(width = s * 0.07f, join = StrokeJoin.Miter))
        val play = Path().apply {
            moveTo(s * 0.40f, s * 0.30f)
            lineTo(s * 0.40f, s * 0.70f)
            lineTo(s * 0.72f, s * 0.50f)
            close()
        }
        drawPath(play, Lime)
    }
}
