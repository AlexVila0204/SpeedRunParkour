package io.github.AlexVila0204.speedRunParkour.commands

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ParkourMenuCommand(private val plugin: SpeedRunParkour) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Component.text("§cThis command can only be run by players!"))
            return true
        }

        if (!sender.hasPermission("parkourtimer.queue")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use parkour queues!"))
            return true
        }

        plugin.menuManager.openDifficultyMenu(sender)
        return true
    }
}