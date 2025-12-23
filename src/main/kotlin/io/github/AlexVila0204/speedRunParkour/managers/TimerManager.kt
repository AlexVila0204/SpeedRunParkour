package io.github.AlexVila0204.speedRunParkour.managers

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import io.github.AlexVila0204.speedRunParkour.config.ConfigManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TimerManager(private val plugin: SpeedRunParkour) {

    private val activeTimers = ConcurrentHashMap<UUID, TimerData>()
    private val bossBars = ConcurrentHashMap<UUID, BossBar>()

    fun startTimer(player: Player, courseId: String) {
        val playerUUID = player.uniqueId

        stopTimer(player)

        val startTime = System.currentTimeMillis()
        val displayTask = createDisplayTask(player, startTime, courseId)

        activeTimers[playerUUID] = TimerData(courseId, startTime, displayTask)

        if (plugin.configManager.getDisplayMode() == ConfigManager.DisplayMode.BOSS_BAR) {
            val bossBar = Bukkit.createBossBar("Parkour Timer", BarColor.GREEN, BarStyle.SOLID)
            bossBar.addPlayer(player)
            bossBars[playerUUID] = bossBar
        }
    }

    fun stopTimer(player: Player): Long? {
        val playerUUID = player.uniqueId
        val timerData = activeTimers.remove(playerUUID) ?: return null

        timerData.displayTask.cancel()

        bossBars.remove(playerUUID)?.let { bossBar ->
            bossBar.removePlayer(player)
        }

        return System.currentTimeMillis() - timerData.startTime
    }

    fun isTimerActive(player: Player): Boolean {
        return activeTimers.containsKey(player.uniqueId)
    }

    fun getCurrentTime(player: Player): Long? {
        val timerData = activeTimers[player.uniqueId] ?: return null
        return System.currentTimeMillis() - timerData.startTime
    }

    fun getCurrentCourse(player: Player): String? {
        return activeTimers[player.uniqueId]?.courseId
    }

    fun clearAllTimers() {
        activeTimers.values.forEach { it.displayTask.cancel() }
        activeTimers.clear()

        bossBars.values.forEach { bossBar ->
            bossBar.removeAll()
        }
        bossBars.clear()
    }

    private fun createDisplayTask(player: Player, startTime: Long, courseId: String): BukkitTask {
        return Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val currentTime = System.currentTimeMillis() - startTime
            val formattedTime = formatTime(currentTime)

            when (plugin.configManager.getDisplayMode()) {
                ConfigManager.DisplayMode.ACTION_BAR -> {
                    player.sendActionBar(Component.text("§6Parkour Timer: §f$formattedTime"))
                }
                ConfigManager.DisplayMode.BOSS_BAR -> {
                    bossBars[player.uniqueId]?.setTitle("Parkour Timer: $formattedTime")
                }
                ConfigManager.DisplayMode.SCOREBOARD -> {
                    player.sendActionBar(Component.text("§6Parkour Timer: §f$formattedTime"))
                }
            }
        }, 0L, 1L)
    }

    private fun formatTime(timeMillis: Long): String {
        val minutes = timeMillis / 60000
        val seconds = (timeMillis % 60000) / 1000
        val milliseconds = timeMillis % 1000
        return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds)
    }

    private data class TimerData(
        val courseId: String,
        val startTime: Long,
        val displayTask: BukkitTask
    )
}