package io.github.AlexVila0204.speedRunParkour.config

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import io.github.AlexVila0204.speedRunParkour.models.ParkourCourse
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import java.awt.DisplayMode

class ConfigManager(private val plugin: SpeedRunParkour){
    private var config : FileConfiguration = plugin.config

    fun loadConfig(){
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        config = plugin.config
    }

    fun reloadConfig(){
        plugin.reloadConfig()
        config = plugin.config
    }

    fun getDisplayMode(): DisplayMode {
        val mode = config.getString("display.mode", "ACTION_BAR") ?: "ACTION_BAR"
        return try {
            DisplayMode.valueOf(mode.uppercase())
        } catch (e: IllegalArgumentException) {
            DisplayMode.ACTION_BAR
        }
    }


    fun getParkourCourses(): List<ParkourCourse> {
        val courses = mutableListOf<ParkourCourse>()
        val coursesSection = config.getConfigurationSection("courses") ?: return courses

        for (courseId in coursesSection.getKeys(false)) {
            val courseSection = coursesSection.getConfigurationSection(courseId) ?: continue

            val difficulty = courseSection.getString("difficulty", "MEDIUM")
            val startLocation = getLocationFromConfig(courseSection, "start")
            val endLocation = getLocationFromConfig(courseSection, "end")
            val spawnLocation = getLocationFromConfig(courseSection, "spawn")
            val waitingArea = getLocationFromConfig(courseSection, "waiting_area")

            if (startLocation != null && endLocation != null && spawnLocation != null && waitingArea != null) {
                courses.add(ParkourCourse(courseId, difficulty!!, startLocation, endLocation, spawnLocation, waitingArea))
            }
        }
        return courses
    }


    private fun getLocationFromConfig(section: org.bukkit.configuration.ConfigurationSection, path: String): Location? {
        val locSection = section.getConfigurationSection(path) ?: return null

        val worldName = locSection.getString("world") ?: return null
        val world = plugin.server.getWorld(worldName) ?: return null

        val x = locSection.getDouble("x")
        val y = locSection.getDouble("y")
        val z = locSection.getDouble("z")
        val yaw = locSection.getDouble("yaw", 0.0).toFloat()
        val pitch = locSection.getDouble("pitch", 0.0).toFloat()

        return Location(world, x, y, z, yaw, pitch)
    }

    fun getStorageType(): StorageType {
        val type = config.getString("storage.type", "YAML") ?: "YAML"
        return try {
            StorageType.valueOf(type.uppercase())
        } catch (e: IllegalArgumentException) {
            StorageType.YAML
        }
    }


    enum class DisplayMode {
        ACTION_BAR, BOSS_BAR, SCOREBOARD
    }

    enum class StorageType {
        YAML, SQLITE
    }
}