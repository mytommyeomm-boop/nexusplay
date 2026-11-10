package com.nexusplay.launcher.input

enum class AxisId { LeftStick, RightStick }

sealed interface OutputStatus {
    data object Ready : OutputStatus
    data class Unavailable(val reason: String) : OutputStatus
}

/**
 * Where controller input finally goes. A runtime integration (embedded runtime or input bridge)
 * implements this. Controls only talk to this interface and never to a specific runtime.
 */
interface ControllerOutput {
    val name: String
    val status: OutputStatus
    fun setAxis(axis: AxisId, x: Float, y: Float)
}

/**
 * Used while no runtime input bridge exists. It reports itself as unavailable and drops input;
 * nothing in the app may present dropped input as delivered to a game.
 */
class UnavailableOutput(reason: String) : ControllerOutput {
    override val name = "No input bridge"
    override val status: OutputStatus = OutputStatus.Unavailable(reason)
    override fun setAxis(axis: AxisId, x: Float, y: Float) = Unit
}

object ControllerOutputHub {
    @Volatile
    var active: ControllerOutput =
        UnavailableOutput("No runtime input bridge is installed, so input cannot reach a game yet.")
}
