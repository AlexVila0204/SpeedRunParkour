
package io.github.AlexVila0204.speedRunParkour.data

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import io.github.AlexVila0204.speedRunParkour.config.ConfigManager
import io.github.AlexVila0204.speedRunParkour.models.PlayerTime
import java.util.*

class DataManager(private val plugin: SpeedRunParkour) {

    private lateinit var storage: Storage

    fun initialize() {
        try {
            storage = when (plugin.configManager.getStorageType()) {
                ConfigManager.StorageType.YAML -> YamlStorage(plugin)
                ConfigManager.StorageType.SQLITE -> SqliteStorage(plugin)
            }
            storage.initialize()

            plugin.logger.info("Data storage initialized: ${plugin.configManager.getStorageType()}")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize data storage: ${e.message}")
            e.printStackTrace()

            plugin.logger.info("Falling back to YAML storage...")
            storage = YamlStorage(plugin)
            storage.initialize()
        }
    }


    fun saveTime(playerTime: PlayerTime) {
        try {
            storage.saveTime(playerTime)
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save player time: ${e.message}")
            e.printStackTrace()
        }
    }

    fun getBestTime(playerUUID: UUID, courseId: String): PlayerTime? {
        return try {
            storage.getBestTime(playerUUID, courseId)
        } catch (e: Exception) {
            plugin.logger.severe("Failed to get best time: ${e.message}")
            null
        }
    }

    fun getBestPlayerForCourse(courseId: String): PlayerTime? {
        return try {
            storage.getBestPlayerForCourse(courseId)
        } catch (e: Exception) {
            plugin.logger.severe("Failed to get best player for course: ${e.message}")
            null
        }
    }

    fun getPlayerTimes(playerUUID: UUID, courseId: String): List<PlayerTime> {
        return try {
            storage.getPlayerTimes(playerUUID, courseId)
        } catch (e: Exception) {
            plugin.logger.severe("Failed to get player times: ${e.message}")
            emptyList()
        }
    }

    fun getTopPlayersForCourse(courseId: String, limit: Int = 10): List<PlayerTime> {
        return try {
            storage.getTopPlayersForCourse(courseId, limit)
        } catch (e: Exception) {
            plugin.logger.severe("Failed to get top players for course: ${e.message}")
            emptyList()
        }
    }


    fun saveAllData() {
        try {
            storage.saveAllData()
            plugin.logger.info("All data saved successfully")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save all data: ${e.message}")
        }
    }
}