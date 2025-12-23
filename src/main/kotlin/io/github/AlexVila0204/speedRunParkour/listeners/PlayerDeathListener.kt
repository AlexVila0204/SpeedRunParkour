
package io.github.AlexVila0204.speedRunParkour.listeners

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.concurrent.ConcurrentHashMap

class PlayerDeathListener(private val plugin: SpeedRunParkour) : Listener {

    private val lavaContactTime = ConcurrentHashMap<Player, Long>()

    private val lavaContactDelay = 1500L

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val currentCourse = plugin.timerManager.getCurrentCourse(player)

        lavaContactTime.remove(player)

        if (currentCourse != null) {
            val course = plugin.arenaManager.getArenaById(currentCourse)

            if (course != null) {
                plugin.timerManager.stopTimer(player)

                plugin.queueManager.finishCourse(player)

                player.sendMessage(Component.text("§c§lTimer Cancelled! §8» §fYou died during the parkour"))
                player.sendMessage(Component.text("§6§lReturning §8» §fGoing back to waiting area..."))

                plugin.server.scheduler.runTaskLater(plugin, Runnable {
                    player.teleport(course.waitingArea)
                    player.sendMessage(Component.text("§a§lWaiting Area §8» §fUse /parkourqueue to rejoin if you want to try again"))
                }, 1L)

                event.drops.clear()
                event.droppedExp = 0
            }
        }
    }


    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val currentCourse = plugin.timerManager.getCurrentCourse(player) ?: return

        val playerLocation = player.location
        val blockBelow = playerLocation.clone().subtract(0.0, 1.0, 0.0).block
        val blockAt = playerLocation.block

        val isTouchingLava = blockBelow.type == Material.LAVA || blockAt.type == Material.LAVA

        if (isTouchingLava) {
            val currentTime = System.currentTimeMillis()

            if (!lavaContactTime.containsKey(player)) {
                lavaContactTime[player] = currentTime
                return
            }

            val contactStartTime = lavaContactTime[player]!!
            if (currentTime - contactStartTime >= lavaContactDelay) {
                val course = plugin.arenaManager.getArenaById(currentCourse)

                if (course != null) {
                    plugin.timerManager.stopTimer(player)

                    plugin.queueManager.finishCourse(player)

                    player.sendMessage(Component.text("§c§lTimer Cancelled! §8» §fYou stayed too long in the lava!"))
                    player.sendMessage(Component.text("§6§lReturning §8» §fGoing back to waiting area..."))
                    player.fireTicks = 0
                    player.teleport(course.waitingArea)
                    player.sendMessage(Component.text("§a§lWaiting Area §8» §fUse /parkourqueue to rejoin if you want to try again"))

                    lavaContactTime.remove(player)
                }
            }

        } else {
            lavaContactTime.remove(player)
        }
    }
}