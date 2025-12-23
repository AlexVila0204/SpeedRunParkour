package io.github.AlexVila0204.speedRunParkour.data

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import io.github.AlexVila0204.speedRunParkour.models.PlayerTime
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.*

class YamlStorage(private val plugin: SpeedRunParkour) : Storage {

    private lateinit var dataFile: File
    private lateinit var dataConfig: FileConfiguration

    override fun initialize() {
        try {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }

            dataFile = File(plugin.dataFolder, "playerdata.yml")
            if (!dataFile.exists()) {
                dataFile.createNewFile()
            }

            dataConfig = YamlConfiguration.loadConfiguration(dataFile)
            plugin.logger.info("YAML storage initialized successfully")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize YAML storage: ${e.message}")
            throw e
        }
    }

    override fun saveTime(playerTime: PlayerTime) {
        try {
            val path = "players.${playerTime.playerUUID}.courses.${playerTime.courseId}.times"
            val times = dataConfig.getList(path) ?: mutableListOf<Map<String, Any>>()

            val timeData = mapOf(
                "playerName" to playerTime.playerName,
                "timeMillis" to playerTime.timeMillis,
                "timestamp" to playerTime.timestamp
            )

            @Suppress("UNCHECKED_CAST")
            val timesList = times as MutableList<Map<String, Any>>
            timesList.add(timeData)

            timesList.sortBy { it["timeMillis"] as Long }

            dataConfig.set(path, timesList)
            saveFile()
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save time to YAML: ${e.message}")
            throw e
        }
    }

    override fun getBestTime(playerUUID: UUID, courseId: String): PlayerTime? {
        try {
            val path = "players.$playerUUID.courses.$courseId.times"
            val times = dataConfig.getList(path) ?: return null

            if (times.isEmpty()) return null

            @Suppress("UNCHECKED_CAST")
            val timesList = times as List<Map<String, Any>>
            val bestTime = timesList.minByOrNull { it["timeMillis"] as Long } ?: return null

            return PlayerTime(
                playerUUID,
                bestTime["playerName"] as String,
                courseId,
                bestTime["timeMillis"] as Long,
                bestTime["timestamp"] as Long
            )
        } catch (e: Exception) {
            plugin.logger.severe("Failed to get best time from YAML: ${e.message}")
            return null
        }
    }

    override fun getBestPlayerForCourse(courseId: String): PlayerTime? {
        try {
            var globalBestTime: PlayerTime? = null

            val playersSection = dataConfig.getConfigurationSection("players") ?: return null

            for (playerUUIDString in playersSection.getKeys(false)) {
                val playerUUID = UUID.fromString(playerUUIDString)
                val playerBestTime = getBestTime(playerUUID, courseId)

                if (playerBestTime != null) {
                    if (globalBestTime == null || playerBestTime.timeMillis < globalBestTime.timeMillis) {
                        globalBestTime = playerBestTime
                    }
                }
            }

            return globalBestTime
        } catch (e: Exception) {
            plugin.logger.severe("Failed to get best player for course from YAML: ${e.message}")
            return null
        }
    }

    override fun getPlayerTimes(playerUUID: UUID, courseId: String): List<PlayerTime> {
        try {
            val path = "players.$playerUUID.courses.$courseId.times"
            val times = dataConfig.getList(path) ?: return emptyList()

            @Suppress("UNCHECKED_CAST")
            val timesList = times as List<Map<String, Any>>

            return timesList.map { timeData ->
                PlayerTime(
                    playerUUID,
                    timeData["playerName"] as String,
                    courseId,
                    timeData["timeMillis"] as Long,
                    timeData["timestamp"] as Long
                )
            }.sortedBy { it.timeMillis }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to get player times from YAML: ${e.message}")
            return emptyList()
        }
    }
    override fun getTopPlayersForCourse(courseId: String, limit: Int): List<PlayerTime> {
        val playerBestTimes = mutableMapOf<UUID, PlayerTime>()
        val courseSection = dataConfig.getConfigurationSection("courses.$courseId.times") ?: return emptyList()

        for (key in courseSection.getKeys(false)) {
            val timeSection = courseSection.getConfigurationSection(key) ?: continue
            val playerUUID = timeSection.getString("player_uuid")?.let { UUID.fromString(it) } ?: continue
            val playerName = timeSection.getString("player_name") ?: continue
            val timeMillis = timeSection.getLong("time_millis")
            val timestamp = timeSection.getLong("timestamp")

            val playerTime = PlayerTime(playerUUID, playerName, courseId, timeMillis, timestamp)

            val existingBest = playerBestTimes[playerUUID]
            if (existingBest == null || timeMillis < existingBest.timeMillis) {
                playerBestTimes[playerUUID] = playerTime
            }
        }

        return playerBestTimes.values
            .sortedBy { it.timeMillis }
            .take(limit)
    }

    override fun saveAllData() {
        try {
            saveFile()
            plugin.logger.info("All YAML data saved successfully")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save YAML data: ${e.message}")
        }
    }

    private fun saveFile() {
        try {
            dataConfig.save(dataFile)
        } catch (e: IOException) {
            plugin.logger.severe("Could not save data to ${dataFile.name}: ${e.message}")
            throw e
        }
    }
}