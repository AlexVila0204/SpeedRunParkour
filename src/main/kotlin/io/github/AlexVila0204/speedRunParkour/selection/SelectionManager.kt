package io.github.AlexVila0204.speedRunParkour.selection

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemFlag
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SelectionManager(private val plugin: SpeedRunParkour) {

    private val playerSelections = ConcurrentHashMap<UUID, Selection>()

    companion object {
        const val SELECTION_TOOL_NAME = "§6§lParkour Selection Tool"
        val SELECTION_TOOL_MATERIAL = Material.GOLDEN_AXE
    }

    fun giveSelectionTool(player: Player) {
        val tool = ItemStack(SELECTION_TOOL_MATERIAL)
        val meta = tool.itemMeta
        meta.displayName(Component.text(SELECTION_TOOL_NAME))
        meta.lore(listOf(
            Component.text("§e§l◆ §7Left click to set position 1"),
            Component.text("§e§l◆ §7Right click to set position 2"),
            Component.text("§e§l◆ §7Use /parkour create <id> <difficulty>"),
            Component.text(""),
            Component.text("§8Parkour Arena Selection Tool")
        ))

        val persistentDataContainer = meta.persistentDataContainer
        val key = org.bukkit.NamespacedKey(plugin, "parkour_tool")
        persistentDataContainer.set(key, org.bukkit.persistence.PersistentDataType.STRING, "selection_tool")

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)

        tool.itemMeta = meta

        player.inventory.addItem(tool)
        player.sendMessage(Component.text("§a§lParkour Tool §8» §fYou received the parkour selection tool!"))
    }

    fun setPosition1(player: Player, location: Location) {
        val selection = playerSelections.getOrPut(player.uniqueId) { Selection() }
        selection.pos1 = location.clone()

        player.sendMessage(Component.text("§a§lPosition 1 §8» §f${formatLocation(location)}"))

        if (selection.isComplete()) {
            player.sendMessage(Component.text("§6§lSelection Complete! §8» §fArea: §e${selection.getVolume()} blocks"))
        }
    }

    fun setPosition2(player: Player, location: Location) {
        val selection = playerSelections.getOrPut(player.uniqueId) { Selection() }
        selection.pos2 = location.clone()

        player.sendMessage(Component.text("§a§lPosition 2 §8» §f${formatLocation(location)}"))

        if (selection.isComplete()) {
            player.sendMessage(Component.text("§6§lSelection Complete! §8» §fArea: §e${selection.getVolume()} blocks"))
        }
    }

    fun getSelection(player: Player): Selection? {
        return playerSelections[player.uniqueId]
    }

    fun hasCompleteSelection(player: Player): Boolean {
        return playerSelections[player.uniqueId]?.isComplete() ?: false
    }

    fun clearSelection(player: Player) {
        playerSelections.remove(player.uniqueId)
        player.sendMessage(Component.text("§a§lSelection Cleared! §8» §fYou can now make a new selection"))
    }

    fun isSelectionTool(item: ItemStack?): Boolean {
        if (item == null || item.type != SELECTION_TOOL_MATERIAL) return false

        val meta = item.itemMeta ?: return false

        val persistentDataContainer = meta.persistentDataContainer
        val key = org.bukkit.NamespacedKey(plugin, "parkour_tool")
        val toolType = persistentDataContainer.get(key, org.bukkit.persistence.PersistentDataType.STRING)

        if (toolType == "selection_tool") return true

        val displayName = meta.displayName()
        return displayName != null && Component.text(SELECTION_TOOL_NAME).equals(displayName)
    }

    private fun formatLocation(location: Location): String {
        return "§e${location.blockX}§7, §e${location.blockY}§7, §e${location.blockZ}"
    }

    data class Selection(
        var pos1: Location? = null,
        var pos2: Location? = null
    ) {
        fun isComplete(): Boolean = pos1 != null && pos2 != null

        fun getVolume(): Int {
            val p1 = pos1 ?: return 0
            val p2 = pos2 ?: return 0

            val dx = kotlin.math.abs(p2.blockX - p1.blockX) + 1
            val dy = kotlin.math.abs(p2.blockY - p1.blockY) + 1
            val dz = kotlin.math.abs(p2.blockZ - p1.blockZ) + 1

            return dx * dy * dz
        }

        fun getCenter(): Location? {
            val p1 = pos1 ?: return null
            val p2 = pos2 ?: return null

            val centerX = (p1.blockX + p2.blockX) / 2.0 + 0.5
            val centerY = (p1.blockY + p2.blockY) / 2.0
            val centerZ = (p1.blockZ + p2.blockZ) / 2.0 + 0.5

            return Location(p1.world, centerX, centerY, centerZ)
        }

        fun contains(location: Location): Boolean {
            val p1 = pos1 ?: return false
            val p2 = pos2 ?: return false

            if (location.world != p1.world) return false

            val minX = kotlin.math.min(p1.blockX, p2.blockX)
            val maxX = kotlin.math.max(p1.blockX, p2.blockX)
            val minY = kotlin.math.min(p1.blockY, p2.blockY)
            val maxY = kotlin.math.max(p1.blockY, p2.blockY)
            val minZ = kotlin.math.min(p1.blockZ, p2.blockZ)
            val maxZ = kotlin.math.max(p1.blockZ, p2.blockZ)

            return location.blockX in minX..maxX &&
                    location.blockY in minY..maxY &&
                    location.blockZ in minZ..maxZ
        }
    }
}