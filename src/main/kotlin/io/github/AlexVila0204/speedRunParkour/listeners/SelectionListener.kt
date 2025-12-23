package io.github.AlexVila0204.speedRunParkour.listeners

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class SelectionListener(private val plugin: SpeedRunParkour) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return

        val player = event.player
        val item = player.inventory.itemInMainHand

        if (!plugin.selectionManager.isSelectionTool(item)) return

        val block = event.clickedBlock ?: return
        val location = block.location

        if (event.action !in listOf(Action.LEFT_CLICK_BLOCK, Action.RIGHT_CLICK_BLOCK)) return

        event.isCancelled = true

        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> {
                plugin.selectionManager.setPosition1(player, location)
            }
            Action.RIGHT_CLICK_BLOCK -> {
                plugin.selectionManager.setPosition2(player, location)
            }
            else -> return
        }
    }
}