package io.github.AlexVila0204.speedRunParkour.listeners

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val plugin: SpeedRunParkour) : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        if (plugin.timerManager.isTimerActive(player)) {
            plugin.timerManager.stopTimer(player)
        }

        plugin.queueManager.leaveQueue(player)
        plugin.menuManager.removePlayerFromMenu(player)
    }

}