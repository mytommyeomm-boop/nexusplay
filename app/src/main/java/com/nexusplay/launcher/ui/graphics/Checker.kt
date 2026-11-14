package com.nexusplay.launcher.ui.graphics

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Checkerboard behind transparent art, so cropping and stray pixels are easy to spot. */
fun Modifier.checkerBackground(): Modifier = this.drawBehind {
    val s = 10.dp.toPx()
    var y = 0f
    var row = 0
    while (y < size.height) {
        var x = 0f
        var col = row % 2
        while (x < size.width) {
            drawRect(if (col % 2 == 0) Color(0xFF3A3F47) else Color(0xFF2C3036), Offset(x, y), Size(s, s))
            x += s
            col++
        }
        y += s
        row++
    }
}
