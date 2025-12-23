package io.github.AlexVila0204.speedRunParkour.gui

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemFlag
import java.util.*

class MenuManager(private val plugin: SpeedRunParkour) {

    companion object {
        const val MENU_TITLE = "§6Parkour Difficulty Selection"
        const val STATS_TITLE = "§6Your Parkour Statistics"
        const val LEADERBOARD_TITLE = "§dParkour Leaderboards"
        const val MENU_SIZE = 27

        const val DIFFICULTY_MENU = "DIFFICULTY_MENU"
        const val STATS_MENU = "STATS_MENU"
        const val LEADERBOARD_MENU = "LEADERBOARD_MENU"
    }

    private val openMenus = mutableSetOf<UUID>()

    fun openDifficultyMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, MENU_SIZE, Component.text(MENU_TITLE))

        val easyItem = createDifficultyItem(
            Material.LIME_WOOL,
            "§aEasy Difficulty",
            listOf(
                "§7Click to join Easy parkour queue",
                "",
                "§7Available courses: §f${plugin.arenaManager.getArenasByDifficulty("EASY").size}",
                "§7Current queue: §f${getQueueSize("EASY")}",
                "",
                "§eClick to join!"
            ),
            "EASY"
        )
        inventory.setItem(11, easyItem)

        val mediumItem = createDifficultyItem(
            Material.YELLOW_WOOL,
            "§eMedium Difficulty",
            listOf(
                "§7Click to join Medium parkour queue",
                "",
                "§7Available courses: §f${plugin.arenaManager.getArenasByDifficulty("MEDIUM").size}",
                "§7Current queue: §f${getQueueSize("MEDIUM")}",
                "",
                "§eClick to join!"
            ),
            "MEDIUM"
        )
        inventory.setItem(13, mediumItem)

        val hardItem = createDifficultyItem(
            Material.RED_WOOL,
            "§cHard Difficulty",
            listOf(
                "§7Click to join Hard parkour queue",
                "",
                "§7Available courses: §f${plugin.arenaManager.getArenasByDifficulty("HARD").size}",
                "§7Current queue: §f${getQueueSize("HARD")}",
                "",
                "§eClick to join!"
            ),
            "HARD"
        )
        inventory.setItem(15, hardItem)

        val statsItem = createMenuItem(
            Material.BOOK,
            "§6Your Statistics",
            listOf(
                "§7View your parkour times",
                "§7and personal records",
                "",
                "§eClick to view!"
            ),
            "STATS"
        )
        inventory.setItem(22, statsItem)

        val leaderboardItem = createMenuItem(
            Material.NETHER_STAR,
            "§dLeaderboards",
            listOf(
                "§7View server-wide",
                "§7parkour leaderboards",
                "",
                "§eClick to view!"
            ),
            "LEADERBOARD"
        )
        inventory.setItem(4, leaderboardItem)

        val queuePosition = plugin.queueManager.getQueuePosition(player)
        if (queuePosition != null) {
            val leaveQueueItem = createMenuItem(
                Material.BARRIER,
                "§cLeave Queue",
                listOf(
                    "§7You are currently in queue",
                    "§7Position: §f#$queuePosition",
                    "",
                    "§cClick to leave queue"
                ),
                "LEAVE_QUEUE"
            )
            inventory.setItem(18, leaveQueueItem)
        }

        fillEmptySlots(inventory, Material.GRAY_STAINED_GLASS_PANE)

        openMenus.add(player.uniqueId)
        player.openInventory(inventory)
    }

    fun openStatsMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 54, Component.text(STATS_TITLE))

        var slot = 10
        val difficulties = listOf("EASY", "MEDIUM", "HARD")

        for (difficulty in difficulties) {
            val courses = plugin.arenaManager.getArenasByDifficulty(difficulty)
            val color = when (difficulty) {
                "EASY" -> "§a"
                "MEDIUM" -> "§e"
                "HARD" -> "§c"
                else -> "§f"
            }

            val difficultyHeader = createMenuItem(
                Material.PAPER,
                "$color$difficulty Courses",
                listOf("§7Your times in $difficulty difficulty"),
                "INFO"
            )
            inventory.setItem(slot, difficultyHeader)
            slot += 2

            for (course in courses.take(3)) {
                val bestTime = plugin.dataManager.getBestTime(player.uniqueId, course.id)
                val allTimes = plugin.dataManager.getPlayerTimes(player.uniqueId, course.id)

                val courseItem = createMenuItem(
                    Material.CLOCK,
                    "§f${course.id}",
                    listOf(
                        "§7Best Time: §f${bestTime?.getFormattedTime() ?: "No time"}",
                        "§7Total Attempts: §f${allTimes.size}",
                        "§7Difficulty: $color$difficulty"
                    ),
                    "COURSE_STATS"
                )
                inventory.setItem(slot, courseItem)
                slot++

                if (slot >= 43) break
            }

            slot += 7
            if (slot >= 43) break
        }

        val backItem = createMenuItem(
            Material.ARROW,
            "§cBack to Main Menu",
            listOf("§7Return to difficulty selection"),
            "BACK_MAIN"
        )
        inventory.setItem(49, backItem)

        fillEmptySlots(inventory, Material.GRAY_STAINED_GLASS_PANE)

        openMenus.add(player.uniqueId)
        player.openInventory(inventory)
    }

    fun openLeaderboardMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 54, Component.text(LEADERBOARD_TITLE))

        var slot = 10
        val allCourses = plugin.arenaManager.getAllArenas()

        for (course in allCourses.take(21)) {
            val bestPlayer = plugin.dataManager.getBestPlayerForCourse(course.id)
            val color = when (course.difficulty.uppercase()) {
                "EASY" -> "§a"
                "MEDIUM" -> "§e"
                "HARD" -> "§c"
                else -> "§f"
            }

            val courseItem = createMenuItem(
                Material.GOLD_INGOT,
                "§f${course.id}",
                listOf(
                    "§7Difficulty: $color${course.difficulty}",
                    "§7Best Player: §f${bestPlayer?.playerName ?: "No record"}",
                    "§7Best Time: §f${bestPlayer?.getFormattedTime() ?: "No time"}",
                    "",
                    "§6World Record Holder!"
                ),
                "LEADERBOARD_ENTRY"
            )
            inventory.setItem(slot, courseItem)

            slot++
            if (slot % 9 == 8) slot += 2
            if (slot >= 43) break
        }

        val backItem = createMenuItem(
            Material.ARROW,
            "§cBack to Main Menu",
            listOf("§7Return to difficulty selection"),
            "BACK_MAIN"
        )
        inventory.setItem(49, backItem)

        fillEmptySlots(inventory, Material.GRAY_STAINED_GLASS_PANE)

        openMenus.add(player.uniqueId)
        player.openInventory(inventory)
    }

    private fun createDifficultyItem(material: Material, name: String, lore: List<String>, difficulty: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(Component.text(name))
        meta.lore(lore.map { Component.text(it) })

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DESTROYS)

        val persistentDataContainer = meta.persistentDataContainer
        val key = org.bukkit.NamespacedKey(plugin, "difficulty")
        persistentDataContainer.set(key, org.bukkit.persistence.PersistentDataType.STRING, difficulty)

        item.itemMeta = meta
        return item
    }

    private fun createMenuItem(material: Material, name: String, lore: List<String>, action: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(Component.text(name))
        meta.lore(lore.map { Component.text(it) })

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DESTROYS)

        val persistentDataContainer = meta.persistentDataContainer
        val key = org.bukkit.NamespacedKey(plugin, "action")
        persistentDataContainer.set(key, org.bukkit.persistence.PersistentDataType.STRING, action)

        item.itemMeta = meta
        return item
    }

    private fun fillEmptySlots(inventory: Inventory, material: Material) {
        val filler = ItemStack(material)
        val meta = filler.itemMeta
        meta.displayName(Component.text(""))
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DESTROYS)
        filler.itemMeta = meta

        for (i in 0 until inventory.size) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler)
            }
        }
    }

    private fun getQueueSize(difficulty: String): Int {
        return plugin.queueManager.getQueueSizeForDifficulty(difficulty)
    }

    fun isPlayerInMenu(player: Player): Boolean {
        return openMenus.contains(player.uniqueId)
    }

    fun removePlayerFromMenu(player: Player) {
        openMenus.remove(player.uniqueId)
    }
}