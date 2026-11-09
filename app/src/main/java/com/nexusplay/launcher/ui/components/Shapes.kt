package com.nexusplay.launcher.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

enum class Corner { TopLeft, TopRight, BottomRight, BottomLeft }

/** Rectangle with any combination of 45-degree cut corners: the deck's signature silhouette. */
class ChamferShape(
    private val cut: Dp,
    private val topLeft: Boolean = false,
    private val topRight: Boolean = false,
    private val bottomRight: Boolean = false,
    private val bottomLeft: Boolean = false
) : Shape {

    fun path(size: Size, density: Density): Path {
        val c = with(density) { cut.toPx() }.coerceAtMost(minOf(size.width, size.height) / 2f)
        return Path().apply {
            moveTo(if (topLeft) c else 0f, 0f)
            lineTo(if (topRight) size.width - c else size.width, 0f)
            if (topRight) lineTo(size.width, c)
            lineTo(size.width, if (bottomRight) size.height - c else size.height)
            if (bottomRight) lineTo(size.width - c, size.height)
            lineTo(if (bottomLeft) c else 0f, size.height)
            if (bottomLeft) lineTo(0f, size.height - c)
            lineTo(0f, if (topLeft) c else 0f)
            close()
        }
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(path(size, density))
}
