package io.github.AlexVila0204.speedRunParkour.data

import io.github.AlexVila0204.speedRunParkour.models.PlayerTime
import java.util.*

interface Storage {
    fun initialize()
    fun saveTime(playerTime: PlayerTime)
    fun getBestTime(playerUUID: UUID, courseId: String): PlayerTime?
    fun getBestPlayerForCourse(courseId: String): PlayerTime?
    fun getPlayerTimes(playerUUID: UUID, courseId: String): List<PlayerTime>
    fun getTopPlayersForCourse(courseId: String, limit: Int = 10): List<PlayerTime>
    fun saveAllData()
}
