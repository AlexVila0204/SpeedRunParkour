package io.github.AlexVila0204.speedRunParkour.commands

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ParkourReloadCommand(private val plugin: SpeedRunParkour) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("parkourtimer.reload")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command!"))
            return true
        }

        try {
            plugin.configManager.reloadConfig()
            plugin.arenaManager.initialize()

            sender.sendMessage(Component.text("§aSpeedRunParkour configuration reloaded successfully!"))
            plugin.logger.info("Configuration reloaded by ${sender.name}")
        } catch (e: Exception) {
            sender.sendMessage(Component.text("§cFailed to reload configuration: ${e.message}"))
            plugin.logger.severe("Failed to reload configuration: ${e.message}")
        }

        return true
    }
}