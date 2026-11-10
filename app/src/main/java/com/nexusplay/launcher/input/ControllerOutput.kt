package com.nexusplay.launcher.input

import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.UiText

enum class AxisId { LeftStick, RightStick }

sealed interface OutputStatus {
    data object Ready : OutputStatus
    data class Unavailable(val reason: UiText) : OutputStatus
}

/**
 * Where controller input finally goes. A runtime integration (embedded runtime or input bridge)
 * implements this. Controls only talk to this interface and never to a specific runtime.
 */
interface ControllerOutput {
    val name: UiText
    val status: OutputStatus
    fun setAxis(axis: AxisId, x: Float, y: Float)
}

/**
 * Used while no runtime input bridge exists. It reports itself as unavailable and drops input;
 * nothing in the app may present dropped input as delivered to a game.
 */
class UnavailableOutput(reason: UiText) : ControllerOutput {
    override val name = UiText(R.string.js_bridge_none_name)
    override val status: OutputStatus = OutputStatus.Unavailable(reason)
    override fun setAxis(axis: AxisId, x: Float, y: Float) = Unit
}

object ControllerOutputHub {
    @Volatile
    var active: ControllerOutput = UnavailableOutput(UiText(R.string.js_bridge_none_reason))
}
