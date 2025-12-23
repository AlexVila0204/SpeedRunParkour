package io.github.AlexVila0204.speedRunParkour.models

import java.util.*

data class PlayerTime(
    val playerUUID: UUID,
    val playerName: String,
    val courseId: String,
    val timeMillis: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val minutes = timeMillis / 60000
        val seconds = (timeMillis % 60000) / 1000
        val milliseconds = timeMillis % 1000
        return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds)
    }
}