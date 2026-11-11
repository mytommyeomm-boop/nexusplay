package com.nexusplay.launcher.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Custom stroke icon set so the app has its own look and needs no icon library. */
private fun icon(name: String, block: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = null,
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) { block() }.build()

private fun PathBuilder.line(x1: Float, y1: Float, x2: Float, y2: Float) {
    moveTo(x1, y1); lineTo(x2, y2)
}

private fun PathBuilder.box(l: Float, t: Float, r: Float, b: Float) {
    moveTo(l, t); lineTo(r, t); lineTo(r, b); lineTo(l, b); close()
}

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcTo(r, r, 0f, true, true, cx + r, cy)
    arcTo(r, r, 0f, true, true, cx - r, cy)
    close()
}

object NeonIcons {
    val Home: ImageVector by lazy {
        icon("Home") {
            moveTo(3f, 11f); lineTo(12f, 3f); lineTo(21f, 11f)
            moveTo(5f, 9.5f); lineTo(5f, 20f); lineTo(10f, 20f); lineTo(10f, 14f)
            lineTo(14f, 14f); lineTo(14f, 20f); lineTo(19f, 20f); lineTo(19f, 9.5f)
        }
    }

    val Library: ImageVector by lazy {
        icon("Library") {
            moveTo(6.5f, 7f); lineTo(17.5f, 7f); lineTo(21f, 16f); lineTo(19f, 18f)
            lineTo(15f, 14f); lineTo(9f, 14f); lineTo(5f, 18f); lineTo(3f, 16f); close()
            line(8f, 9.5f, 8f, 12.5f); line(6.5f, 11f, 9.5f, 11f)
            line(15f, 10f, 15.01f, 10f); line(17f, 12f, 17.01f, 12f)
        }
    }

    val Import: ImageVector by lazy {
        icon("Import") {
            line(12f, 4f, 12f, 15f)
            moveTo(7f, 10f); lineTo(12f, 15f); lineTo(17f, 10f)
            line(4f, 19f, 20f, 19f)
        }
    }

    val Folder: ImageVector by lazy {
        icon("Folder") {
            moveTo(3f, 6f); lineTo(9f, 6f); lineTo(11f, 9f); lineTo(21f, 9f)
            lineTo(21f, 19f); lineTo(3f, 19f); close()
        }
    }

    val Sliders: ImageVector by lazy {
        icon("Sliders") {
            line(4f, 6f, 20f, 6f); line(4f, 12f, 20f, 12f); line(4f, 18f, 20f, 18f)
            line(9f, 3.5f, 9f, 8.5f); line(15f, 9.5f, 15f, 14.5f); line(8f, 15.5f, 8f, 20.5f)
        }
    }

    val Cpu: ImageVector by lazy {
        icon("Cpu") {
            box(7f, 7f, 17f, 17f)
            line(10f, 3f, 10f, 7f); line(14f, 3f, 14f, 7f)
            line(10f, 17f, 10f, 21f); line(14f, 17f, 14f, 21f)
            line(3f, 10f, 7f, 10f); line(3f, 14f, 7f, 14f)
            line(17f, 10f, 21f, 10f); line(17f, 14f, 21f, 14f)
        }
    }

    val Disk: ImageVector by lazy {
        icon("Disk") {
            box(4f, 6f, 20f, 11f); box(4f, 13f, 20f, 18f)
            line(8f, 8.5f, 8.01f, 8.5f); line(8f, 15.5f, 8.01f, 15.5f)
        }
    }

    val Layers: ImageVector by lazy {
        icon("Layers") {
            moveTo(12f, 3f); lineTo(21f, 8f); lineTo(12f, 13f); lineTo(3f, 8f); close()
            moveTo(3f, 12f); lineTo(12f, 17f); lineTo(21f, 12f)
            moveTo(3f, 16f); lineTo(12f, 21f); lineTo(21f, 16f)
        }
    }

    val Bolt: ImageVector by lazy {
        icon("Bolt") {
            moveTo(13f, 2f); lineTo(4f, 14f); lineTo(11f, 14f); lineTo(10f, 22f)
            lineTo(20f, 9f); lineTo(13f, 9f); close()
        }
    }

    val Backup: ImageVector by lazy {
        icon("Backup") {
            box(3f, 5f, 21f, 9f)
            moveTo(5f, 9f); lineTo(5f, 19f); lineTo(19f, 19f); lineTo(19f, 9f)
            line(10f, 13f, 14f, 13f)
        }
    }

    val Key: ImageVector by lazy {
        icon("Key") {
            circle(8f, 15f, 4f)
            line(11f, 12f, 20f, 3f); line(16f, 7f, 19f, 10f)
        }
    }

    val Info: ImageVector by lazy {
        icon("Info") {
            circle(12f, 12f, 9f)
            line(12f, 11f, 12f, 16f); line(12f, 8f, 12.01f, 8f)
        }
    }

    val Joystick: ImageVector by lazy {
        icon("Joystick") {
            circle(12f, 12f, 9f)
            circle(12f, 12f, 3.5f)
            line(12f, 3f, 12f, 5.5f); line(12f, 18.5f, 12f, 21f)
            line(3f, 12f, 5.5f, 12f); line(18.5f, 12f, 21f, 12f)
        }
    }

    val Globe: ImageVector by lazy {
        icon("Globe") {
            circle(12f, 12f, 9f)
            line(3f, 12f, 21f, 12f)
            moveTo(12f, 3f)
            arcTo(5f, 9f, 0f, false, false, 12f, 21f)
            arcTo(5f, 9f, 0f, false, false, 12f, 3f)
        }
    }

    val Close: ImageVector by lazy {
        icon("Close") { line(6f, 6f, 18f, 18f); line(18f, 6f, 6f, 18f) }
    }

    val Mascot: ImageVector by lazy {
        icon("Mascot") {
            circle(12f, 13.5f, 7.5f)
            moveTo(5.5f, 9f); lineTo(5f, 3.5f); lineTo(10f, 6f)
            moveTo(18.5f, 9f); lineTo(19f, 3.5f); lineTo(14f, 6f)
            line(9f, 13f, 9.01f, 13f); line(15f, 13f, 15.01f, 13f)
            moveTo(10f, 16f); lineTo(12f, 17.2f); lineTo(14f, 16f)
        }
    }
}
