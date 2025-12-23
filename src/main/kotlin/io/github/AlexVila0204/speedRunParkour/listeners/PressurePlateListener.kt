package io.github.AlexVila0204.speedRunParkour.listeners

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import io.github.AlexVila0204.speedRunParkour.models.PlayerTime
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action

class PressurePlateListener(private val plugin: SpeedRunParkour) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.PHYSICAL) return

        val block = event.clickedBlock ?: return
        val player = event.player

        if (!isPressurePlate(block.type)) return

        val location = block.location

        val startCourse = findCourseByStartLocation(location)
        if (startCourse != null) {
            handleStartPlate(player, startCourse.id)
            return
        }

        val endCourse = findCourseByEndLocation(location)
        if (endCourse != null) {
            handleEndPlate(player, endCourse.id)
        }
    }

    private fun findCourseByStartLocation(location: org.bukkit.Location) =
        plugin.arenaManager.getAllArenas().find { it.isStartPlate(location) }

    private fun findCourseByEndLocation(location: org.bukkit.Location) =
        plugin.arenaManager.getAllArenas().find { it.isEndPlate(location) }

    private fun handleStartPlate(player: Player, courseId: String) {
        if (!plugin.queueManager.isPlayerInCourse(player)) {
            player.sendMessage(Component.text("§cYou need to join the queue first! Use /parkourqueue <difficulty>"))
            return
        }

        if (plugin.timerManager.isTimerActive(player)) {
            player.sendMessage(Component.text("§cYou already have an active timer!"))
            return
        }

        plugin.timerManager.startTimer(player, courseId)
        player.sendMessage(Component.text("§aTimer started! Good luck!"))
    }

    private fun handleEndPlate(player: Player, courseId: String) {
        val currentCourse = plugin.timerManager.getCurrentCourse(player)
        if (currentCourse != courseId) {
            player.sendMessage(Component.text("§cYou don't have an active timer for this course!"))
            return
        }

        val timeMillis = plugin.timerManager.stopTimer(player)
        if (timeMillis == null) {
            player.sendMessage(Component.text("§cNo active timer found!"))
            return
        }

        val course = plugin.arenaManager.getArenaById(courseId)
        if (course == null) {
            player.sendMessage(Component.text("§cError: Course not found!"))
            return
        }

        val playerTime = PlayerTime(
            player.uniqueId,
            player.name,
            courseId,
            timeMillis
        )
        plugin.dataManager.saveTime(playerTime)

        val formattedTime = playerTime.getFormattedTime()
        player.sendMessage(Component.text("§a§lCOMPLETED! §8» §fTime: §e$formattedTime"))

        val bestTime = plugin.dataManager.getBestTime(player.uniqueId, courseId)
        if (bestTime == null || timeMillis < bestTime.timeMillis) {
            player.sendMessage(Component.text("§6§lNEW PERSONAL BEST! §8» §fGreat job!"))
        }

        val bestPlayer = plugin.dataManager.getBestPlayerForCourse(courseId)
        if (bestPlayer == null || timeMillis < bestPlayer.timeMillis) {
            player.sendMessage(Component.text("§5§lNEW WORLD RECORD! §8» §fIncredible!"))
            plugin.server.broadcast(Component.text("§5§lNEW WORLD RECORD! §8» §f${player.name} §7set a new record on §e${courseId}§7: §a$formattedTime"))
        }

        plugin.queueManager.finishCourse(player)

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            player.teleport(course.waitingArea)
            player.sendMessage(Component.text("§a§lCourse Completed §8» §fReturning to waiting area"))
        }, 60L)
    }



    private fun isPressurePlate(material: Material): Boolean {
        return when (material) {
            Material.STONE_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE,
            Material.SPRUCE_PRESSURE_PLATE,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.ACACIA_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.CHERRY_PRESSURE_PLATE,
            Material.BAMBOO_PRESSURE_PLATE,
            Material.MANGROVE_PRESSURE_PLATE,
            Material.CRIMSON_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE -> true
            else -> false
        }
    }
}