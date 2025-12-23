package io.github.AlexVila0204.speedRunParkour.commands

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ParkourQueueCommand(private val plugin: SpeedRunParkour) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Component.text("§cThis command can only be used by players!"))
            return true
        }

        if (!sender.hasPermission("parkourtimer.queue")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command!"))
            return true
        }
        if (args.isEmpty()) {
            plugin.menuManager.openDifficultyMenu(sender)
            return true
        }

        val difficulty = args[0]
        val availableDifficulties = plugin.arenaManager.getAllArenas().map { it.difficulty }.distinct()

        if (args.size > 1 && args[1].equals("leave", ignoreCase = true)) {
            if (plugin.queueManager.leaveQueue(sender)) {
                sender.sendMessage(Component.text("§aYou have left the queue."))
            } else {
                sender.sendMessage(Component.text("§cYou are not in any queue."))
            }
            return true
        }

        if (!availableDifficulties.any { it.equals(difficulty, ignoreCase = true) }) {
            sender.sendMessage(Component.text("§cInvalid difficulty! Available: ${availableDifficulties.joinToString(", ")}"))
            sender.sendMessage(Component.text("§7Or use §f/parkourqueue §7to open the menu!"))
            return true
        }

        plugin.queueManager.joinQueue(sender, difficulty)
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        if (args.size == 1) {
            val difficulties = plugin.arenaManager.getAllArenas().map { it.difficulty }.distinct().toMutableList()
            difficulties.add("leave")
            return difficulties.filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()
        }
        return mutableListOf()
    }
}