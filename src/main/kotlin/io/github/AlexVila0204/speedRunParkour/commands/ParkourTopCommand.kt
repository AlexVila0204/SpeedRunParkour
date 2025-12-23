
package io.github.AlexVila0204.speedRunParkour.commands

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ParkourTopCommand(private val plugin: SpeedRunParkour) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Component.text("§cThis command can only be used by players!"))
            return true
        }

        if (!sender.hasPermission("parkourtimer.top")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command!"))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(Component.text("§cUsage: /parkourtop <arena_id> [limit]"))
            sender.sendMessage(Component.text("§7Available arenas: ${plugin.arenaManager.getAllArenas().joinToString(", ") { it.id }}"))
            return true
        }

        val arenaId = args[0]
        val limit = if (args.size > 1) {
            args[1].toIntOrNull() ?: 10
        } else 10

        val arena = plugin.arenaManager.getArena(arenaId)
        if (arena == null) {
            sender.sendMessage(Component.text("§cArena not found: $arenaId"))
            return true
        }

        val topPlayers = plugin.dataManager.getTopPlayersForCourse(arenaId, limit.coerceIn(1, 50))

        if (topPlayers.isEmpty()) {
            sender.sendMessage(Component.text("§7No times recorded for arena: §f$arenaId"))
            return true
        }

        sender.sendMessage(Component.text(""))
        sender.sendMessage(Component.text("§6§l━━━ Top $limit for §f$arenaId §6§l━━━"))
        sender.sendMessage(Component.text("§7Arena: §f$arenaId §8(${arena.difficulty})"))
        sender.sendMessage(Component.text(""))

        topPlayers.forEachIndexed { index, playerTime ->
            val position = index + 1
            val medal = when (position) {
                1 -> "§6🥇"
                2 -> "§7🥈"
                3 -> "§c🥉"
                else -> "§e#$position"
            }

            val timeColor = when (position) {
                1 -> "§6"
                2 -> "§7"
                3 -> "§c"
                else -> "§f"
            }

            sender.sendMessage(Component.text("$medal §f${playerTime.playerName} §8- $timeColor${playerTime.getFormattedTime()}"))
        }

        sender.sendMessage(Component.text(""))
        sender.sendMessage(Component.text("§8Use /parkourtop $arenaId <number> to see more results"))

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        return when (args.size) {
            1 -> {
                plugin.arenaManager.getAllArenas().map { it.id }
                    .filter { it.startsWith(args[0], ignoreCase = true) }
                    .toMutableList()
            }
            2 -> {
                listOf("5", "10", "15", "20", "25", "50")
                    .filter { it.startsWith(args[1]) }
                    .toMutableList()
            }
            else -> mutableListOf()
        }
    }
}