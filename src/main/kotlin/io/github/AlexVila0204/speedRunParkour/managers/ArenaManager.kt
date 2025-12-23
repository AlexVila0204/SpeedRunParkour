package io.github.AlexVila0204.speedRunParkour.managers

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import io.github.AlexVila0204.speedRunParkour.models.ParkourCourse
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ArenaManager(private val plugin: SpeedRunParkour) {

    private val arenas = mutableMapOf<String, ParkourCourse>()
    private lateinit var arenasFile: File
    private lateinit var arenasConfig: YamlConfiguration

    fun initialize() {
        arenasFile = File(plugin.dataFolder, "arenas.yml")
        if (!arenasFile.exists()) {
            arenasFile.createNewFile()
        }
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile)
        loadArenas()
    }

    fun createArena(id: String, difficulty: String, startLocation: Location, endLocation: Location, spawnLocation: Location, waitingArea: Location) {
        val arena = ParkourCourse(id, difficulty, startLocation, endLocation, spawnLocation, waitingArea)
        arenas[id] = arena
        saveArena(arena)
    }

    fun setStartLocation(arenaId: String, location: Location): Boolean {
        val arena = arenas[arenaId] ?: return false
        val newArena = arena.copy(startLocation = location)
        arenas[arenaId] = newArena
        saveArena(newArena)
        return true
    }

    fun setEndLocation(arenaId: String, location: Location): Boolean {
        val arena = arenas[arenaId] ?: return false
        val newArena = arena.copy(endLocation = location)
        arenas[arenaId] = newArena
        saveArena(newArena)
        return true
    }

    fun setSpawnLocation(arenaId: String, location: Location): Boolean {
        val arena = arenas[arenaId] ?: return false
        val newArena = arena.copy(spawnLocation = location)
        arenas[arenaId] = newArena
        saveArena(newArena)
        return true
    }

    fun setWaitingArea(arenaId: String, location: Location): Boolean {
        val arena = arenas[arenaId] ?: return false
        val newArena = arena.copy(waitingArea = location)
        arenas[arenaId] = newArena
        saveArena(newArena)
        return true
    }

    fun deleteArena(arenaId: String): Boolean {
        if (arenas.remove(arenaId) != null) {
            arenasConfig.set("arenas.$arenaId", null)
            saveConfig()
            return true
        }
        return false
    }

    fun getArena(arenaId: String): ParkourCourse? {
        return arenas[arenaId]
    }

    fun getArenaById(arenaId: String): ParkourCourse? {
        return arenas[arenaId]
    }

    fun getAllArenas(): List<ParkourCourse> {
        return arenas.values.toList()
    }

    fun getArenasByDifficulty(difficulty: String): List<ParkourCourse> {
        return arenas.values.filter { it.difficulty.equals(difficulty, ignoreCase = true) }
    }

    private fun loadArenas() {
        val arenasSection = arenasConfig.getConfigurationSection("arenas") ?: return

        for (arenaId in arenasSection.getKeys(false)) {
            val arenaSection = arenasSection.getConfigurationSection(arenaId) ?: continue

            val difficulty = arenaSection.getString("difficulty") ?: continue
            val startLocation = getLocationFromSection(arenaSection, "start") ?: continue
            val endLocation = getLocationFromSection(arenaSection, "end") ?: continue
            val spawnLocation = getLocationFromSection(arenaSection, "spawn") ?: continue
            val waitingArea = getLocationFromSection(arenaSection, "waiting_area")

            val finalWaitingArea = waitingArea ?: spawnLocation

            arenas[arenaId] = ParkourCourse(arenaId, difficulty, startLocation, endLocation, spawnLocation, finalWaitingArea)
        }

        plugin.logger.info("Loaded ${arenas.size} arenas from arenas.yml")
    }

    private fun saveArena(arena: ParkourCourse) {
        val section = arenasConfig.createSection("arenas.${arena.id}")
        section.set("difficulty", arena.difficulty)

        saveLocationToSection(section, "start", arena.startLocation)
        saveLocationToSection(section, "end", arena.endLocation)
        saveLocationToSection(section, "spawn", arena.spawnLocation)
        saveLocationToSection(section, "waiting_area", arena.waitingArea)

        saveConfig()
    }

    private fun saveLocationToSection(section: org.bukkit.configuration.ConfigurationSection, path: String, location: Location) {
        section.set("$path.world", location.world?.name)
        section.set("$path.x", location.x)
        section.set("$path.y", location.y)
        section.set("$path.z", location.z)
        section.set("$path.yaw", location.yaw.toDouble())
        section.set("$path.pitch", location.pitch.toDouble())
    }

    private fun getLocationFromSection(section: org.bukkit.configuration.ConfigurationSection, path: String): Location? {
        val worldName = section.getString("$path.world") ?: return null
        val world = plugin.server.getWorld(worldName) ?: return null

        val x = section.getDouble("$path.x")
        val y = section.getDouble("$path.y")
        val z = section.getDouble("$path.z")
        val yaw = section.getDouble("$path.yaw", 0.0).toFloat()
        val pitch = section.getDouble("$path.pitch", 0.0).toFloat()

        return Location(world, x, y, z, yaw, pitch)
    }

    private fun saveConfig() {
        try {
            arenasConfig.save(arenasFile)
        } catch (e: Exception) {
            plugin.logger.severe("Could not save arenas.yml: ${e.message}")
        }
    }
}