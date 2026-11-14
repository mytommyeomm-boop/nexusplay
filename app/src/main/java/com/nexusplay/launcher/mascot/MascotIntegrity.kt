package com.nexusplay.launcher.mascot

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.AppLanguage
import com.nexusplay.launcher.core.UiText

/** Developer check: are all images present and uncropped, and does every expression and event have valid art? */
object MascotIntegrity {
    class Result(val issues: List<UiText>, val assets: Int, val events: Int)

    fun run(context: Context, language: AppLanguage?, overrides: Map<String, String>): Result {
        val issues = ArrayList<UiText>()
        val options = BitmapFactory.Options().apply { inScaled = false }

        for (a in MascotCatalog.all) {
            val resolvable = try { context.resources.getResourceEntryName(a.res); true } catch (e: Resources.NotFoundException) { false }
            if (!resolvable) { issues += UiText(R.string.lab_issue_missing, a.id); continue }
            val bmp = BitmapFactory.decodeResource(context.resources, a.res, options)
            if (bmp == null) { issues += UiText(R.string.lab_issue_missing, a.id); continue }
            if (touchesEdge(bmp)) issues += UiText(R.string.lab_issue_edge, a.id)
            bmp.recycle()
        }

        val resolver = PoseResolver(emptyMap())
        for (e in MascotExpression.entries) {
            if (resolver.candidates(e, language).isEmpty()) issues += UiText(R.string.lab_issue_no_art, e.name)
            for (id in MascotMapping.defaults[e].orEmpty()) {
                val asset = MascotCatalog.byId(id)
                if (asset == null || !e.accepts(asset)) issues += UiText(R.string.lab_issue_mismatch, id, e.name)
            }
        }
        for (ev in MascotEvent.entries) {
            if (ev.name in overrides) continue // a developer choice, not a mapping error
            val asset = PoseResolver(emptyMap()).assetFor(ev, language)
            if (!ev.expression.accepts(asset) && asset.id != MascotCatalog.neutral.id) {
                issues += UiText(R.string.lab_issue_mismatch, asset.id, ev.name)
            }
        }
        return Result(issues, MascotCatalog.all.size, MascotEvent.entries.size)
    }

    /** Visible pixels on the outermost row or column mean the art was cut off by its own bounds. */
    private fun touchesEdge(bmp: Bitmap): Boolean {
        val w = bmp.width; val h = bmp.height
        for (x in 0 until w) if (Color.alpha(bmp.getPixel(x, 0)) > 8 || Color.alpha(bmp.getPixel(x, h - 1)) > 8) return true
        for (y in 0 until h) if (Color.alpha(bmp.getPixel(0, y)) > 8 || Color.alpha(bmp.getPixel(w - 1, y)) > 8) return true
        return false
    }
}
