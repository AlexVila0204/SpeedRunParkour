package io.github.AlexVila0204.speedRunParkour.listeners

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*
import org.bukkit.persistence.PersistentDataType

class MenuListener(private val plugin: SpeedRunParkour) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = event.inventory

        val title = inventory.viewers.firstOrNull()?.openInventory?.title()?.toString()
        val isOurMenu = title != null && (
                title.contains("Parkour Difficulty Selection") ||
                        title.contains("Your Parkour Statistics") ||
                        title.contains("Parkour Leaderboards")
                )

        val isTrackedPlayer = plugin.menuManager.isPlayerInMenu(player)

        if (!isOurMenu && !isTrackedPlayer) return

        event.isCancelled = true

        val clickedItem = event.currentItem ?: return
        val meta = clickedItem.itemMeta ?: return
        val persistentDataContainer = meta.persistentDataContainer

        val difficultyKey = org.bukkit.NamespacedKey(plugin, "difficulty")
        val difficulty = persistentDataContainer.get(difficultyKey, PersistentDataType.STRING)

        if (difficulty != null) {
            player.closeInventory()
            handleDifficultySelection(player, difficulty)
            return
        }

        val actionKey = org.bukkit.NamespacedKey(plugin, "action")
        val action = persistentDataContainer.get(actionKey, PersistentDataType.STRING)

        when (action) {
            "STATS" -> {
                plugin.menuManager.openStatsMenu(player)
            }
            "LEADERBOARD" -> {
                plugin.menuManager.openLeaderboardMenu(player)
            }
            "LEAVE_QUEUE" -> {
                player.closeInventory()
                plugin.queueManager.leaveQueue(player)
            }
            "BACK_MAIN" -> {
                plugin.menuManager.openDifficultyMenu(player)
            }
            "INFO", "COURSE_STATS", "LEADERBOARD_ENTRY" -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = event.inventory

        val title = inventory.viewers.firstOrNull()?.openInventory?.title()?.toString()
        val isOurMenu = title != null && (
                title.contains("Parkour Difficulty Selection") ||
                        title.contains("Your Parkour Statistics") ||
                        title.contains("Parkour Leaderboards")
                )

        if (isOurMenu || plugin.menuManager.isPlayerInMenu(player)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryMoveItem(event: InventoryMoveItemEvent) {
        val destination = event.destination

        for (viewer in destination.viewers) {
            if (viewer is Player) {
                val title = viewer.openInventory.title()?.toString()
                val isOurMenu = title != null && (
                        title.contains("Parkour Difficulty Selection") ||
                                title.contains("Your Parkour Statistics") ||
                                title.contains("Parkour Leaderboards")
                        )

                if (isOurMenu || plugin.menuManager.isPlayerInMenu(viewer)) {
                    event.isCancelled = true
                    break
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryPickup(event: InventoryPickupItemEvent) {
        val inventory = event.inventory

        for (viewer in inventory.viewers) {
            if (viewer is Player && plugin.menuManager.isPlayerInMenu(viewer)) {
                event.isCancelled = true
                break
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        plugin.menuManager.removePlayerFromMenu(player)
    }

    private fun handleDifficultySelection(player: Player, difficulty: String) {
        val success = plugin.queueManager.joinQueue(player, difficulty)

        if (success) {
            player.sendMessage(Component.text("§aSuccessfully joined the $difficulty difficulty queue!"))
            val position = plugin.queueManager.getQueuePosition(player)
            if (position != null && position > 1) {
                player.sendMessage(Component.text("§7You are position §f#$position §7in the queue."))
            }
        }
    }
}