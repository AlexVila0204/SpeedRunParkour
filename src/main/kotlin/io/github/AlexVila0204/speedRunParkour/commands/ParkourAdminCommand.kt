
package io.github.AlexVila0204.speedRunParkour.commands

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import io.github.AlexVila0204.speedRunParkour.managers.QueueManager
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ParkourAdminCommand(private val plugin: SpeedRunParkour) : CommandExecutor, TabCompleter {

    companion object {
        private val VALID_DIFFICULTIES = listOf("EASY", "MEDIUM", "HARD")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Component.text("§cThis command can only be run by players!"))
            return true
        }

        if (!sender.hasPermission("parkourtimer.admin")) {
            sender.sendMessage(Component.text("§cYou don't have permission to execute this command!"))
            return true
        }

        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "tool", "wand" -> {
                plugin.selectionManager.giveSelectionTool(sender)
            }
            "create" -> {
                if (args.size < 3) {
                    sender.sendMessage(Component.text("§cUsage: /parkour create <arena_id> <difficulty>"))
                    sender.sendMessage(Component.text("§7Valid difficulties: ${VALID_DIFFICULTIES.joinToString(", ")}"))
                    return true
                }
                createArena(sender, args[1], args[2])
            }
            "start" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("§cUsage: /parkour start <arena_id>"))
                    return true
                }
                startArena(sender, args[1])
            }
            "stop" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("§cUsage: /parkour stop <arena_id>"))
                    return true
                }
                stopArena(sender, args[1])
            }
            "open" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("§cUsage: /parkour open <arena_id>"))
                    return true
                }
                openArena(sender, args[1])
            }
            "close" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("§cUsage: /parkour close <arena_id>"))
                    return true
                }
                closeArena(sender, args[1])
            }
            "queue" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("§cUsage: /parkour queue <arena_id>"))
                    return true
                }
                showQueueInfo(sender, args[1])
            }
            "setstart" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("§cUsage: /parkour setstart <arena_id>"))
                    return true
                }
                setStartLocation(sender, args[1])
            }
            "setend" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("§cUsage: /parkour setend <arena_id>"))
                    return true
                }
                setEndLocation(sender, args[1])
            }
            "setspawn" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("§cUsage: /parkour setspawn <arena_id>"))
                    return true
                }
                setSpawnLocation(sender, args[1])
            }
            "setwaiting" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("§cUsage: /parkour setwaiting <arena_id>"))
                    return true
                }
                setWaitingArea(sender, args[1])
            }
            "list" -> {
                listArenas(sender)
            }
            "delete" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("§cUsage: /parkour delete <arena_id>"))
                    return true
                }
                deleteArena(sender, args[1])
            }
            "clear" -> {
                plugin.selectionManager.clearSelection(sender)
            }
            else -> {
                sendHelp(sender)
            }
        }

        return true
    }


    private fun createArena(player: Player, arenaId: String, difficulty: String) {
        if (!VALID_DIFFICULTIES.contains(difficulty.uppercase())) {
            player.sendMessage(Component.text("§cInvalid difficulty! Valid options: ${VALID_DIFFICULTIES.joinToString(", ")}"))
            return
        }

        if (!plugin.selectionManager.hasCompleteSelection(player)) {
            player.sendMessage(Component.text("§cFirst you must select the area with the selection tool (/parkour tool)!"))
            return
        }

        val selection = plugin.selectionManager.getSelection(player)!!
        val center = selection.getCenter()!!

        plugin.arenaManager.createArena(
            arenaId,
            difficulty.uppercase(),
            center,
            center,
            center,
            center
        )

        player.sendMessage(Component.text("§aArena '$arenaId' created with difficulty '${difficulty.uppercase()}'!"))
        player.sendMessage(Component.text("§7Now configure the locations with the following commands:"))
        player.sendMessage(Component.text("§7- /parkour setstart $arenaId"))
        player.sendMessage(Component.text("§7- /parkour setend $arenaId"))
        player.sendMessage(Component.text("§7- /parkour setspawn $arenaId"))
        player.sendMessage(Component.text("§7- /parkour setwaiting $arenaId"))
        player.sendMessage(Component.text("§6Then open the arena: §f/parkour open $arenaId"))
    }

    private fun startArena(admin: Player, arenaId: String) {
        if (plugin.queueManager.startCourse(arenaId, admin)) {
            admin.sendMessage(Component.text("§aArena started successfully!"))
        }
    }

    private fun stopArena(admin: Player, arenaId: String) {
        if (plugin.queueManager.stopCourse(arenaId, admin)) {
            admin.sendMessage(Component.text("§cArena processing stopped successfully!"))
        }
    }


    private fun openArena(admin: Player, arenaId: String) {
        plugin.queueManager.setCourseState(arenaId, QueueManager.CourseState.WAITING, admin)
    }

    private fun closeArena(admin: Player, arenaId: String) {
        plugin.queueManager.setCourseState(arenaId, QueueManager.CourseState.CLOSED, admin)
    }

    private fun showQueueInfo(admin: Player, arenaId: String) {
        val info = plugin.queueManager.getQueueInfo(arenaId)
        if (info != null) {
            admin.sendMessage(Component.text(info))
        } else {
            admin.sendMessage(Component.text("§cArena not found: $arenaId"))
        }
    }

    private fun setStartLocation(player: Player, arenaId: String) {
        val location = player.location
        if (plugin.arenaManager.setStartLocation(arenaId, location)) {
            player.sendMessage(Component.text("§aStart location configured for arena '$arenaId'!"))
        } else {
            player.sendMessage(Component.text("§cArena '$arenaId' not found!"))
        }
    }

    private fun setEndLocation(player: Player, arenaId: String) {
        val location = player.location
        if (plugin.arenaManager.setEndLocation(arenaId, location)) {
            player.sendMessage(Component.text("§aEnd location configured for arena '$arenaId'!"))
        } else {
            player.sendMessage(Component.text("§cArena '$arenaId' not found!"))
        }
    }

    private fun setSpawnLocation(player: Player, arenaId: String) {
        val location = player.location
        if (plugin.arenaManager.setSpawnLocation(arenaId, location)) {
            player.sendMessage(Component.text("§aSpawn location configured for arena '$arenaId'!"))
        } else {
            player.sendMessage(Component.text("§cArena '$arenaId' not found!"))
        }
    }

    private fun setWaitingArea(player: Player, arenaId: String) {
        val location = player.location
        if (plugin.arenaManager.setWaitingArea(arenaId, location)) {
            player.sendMessage(Component.text("§aWaiting area configured for arena '$arenaId'!"))
        } else {
            player.sendMessage(Component.text("§cArena '$arenaId' not found!"))
        }
    }

    private fun listArenas(player: Player) {
        val arenas = plugin.arenaManager.getAllArenas()
        if (arenas.isEmpty()) {
            player.sendMessage(Component.text("§7No arenas configured."))
            return
        }

        player.sendMessage(Component.text("§6=== Configured Arenas ==="))
        arenas.forEach { arena ->
            val queueInfo = plugin.queueManager.getQueueInfo(arena.id)
            val queueSize = if (queueInfo != null) {
                val lines = queueInfo.split("\n")
                val queueLine = lines.find { it.contains("Queue Size:") }
                queueLine?.substringAfter("Queue Size: §f")?.substringBefore("§") ?: "0"
            } else "0"

            player.sendMessage(Component.text("§7- §f${arena.id} §7(${arena.difficulty}) §8- §7Queue: §f$queueSize"))
        }
        player.sendMessage(Component.text("§7Use §f/parkour queue <arena_id> §7for detailed info"))
    }

    private fun deleteArena(player: Player, arenaId: String) {
        if (plugin.arenaManager.deleteArena(arenaId)) {
            player.sendMessage(Component.text("§aArena '$arenaId' deleted!"))
        } else {
            player.sendMessage(Component.text("§cArena '$arenaId' not found!"))
        }
    }

    private fun sendHelp(player: Player) {
        player.sendMessage(Component.text("§6=== Parkour Administration Commands ==="))
        player.sendMessage(Component.text("§e§lArena Setup:"))
        player.sendMessage(Component.text("§7/parkour tool §f- Get selection tool"))
        player.sendMessage(Component.text("§7/parkour create <id> <difficulty> §f- Create arena"))
        player.sendMessage(Component.text("§7/parkour setstart <id> §f- Configure start location"))
        player.sendMessage(Component.text("§7/parkour setend <id> §f- Configure end location"))
        player.sendMessage(Component.text("§7/parkour setspawn <id> §f- Configure spawn location"))
        player.sendMessage(Component.text("§7/parkour setwaiting <id> §f- Configure waiting area"))
        player.sendMessage(Component.text(""))
        player.sendMessage(Component.text("§e§lQueue Management:"))
        player.sendMessage(Component.text("§7/parkour open <id> §f- Open arena for queue"))
        player.sendMessage(Component.text("§7/parkour start <id> §f- Start processing queue"))
        player.sendMessage(Component.text("§7/parkour stop <id> §f- Stop processing queue"))
        player.sendMessage(Component.text("§7/parkour close <id> §f- Close arena"))
        player.sendMessage(Component.text("§7/parkour queue <id> §f- Show queue info"))
        player.sendMessage(Component.text(""))
        player.sendMessage(Component.text("§e§lGeneral:"))
        player.sendMessage(Component.text("§7/parkour list §f- List arenas"))
        player.sendMessage(Component.text("§7/parkour delete <id> §f- Delete arena"))
        player.sendMessage(Component.text("§7/parkour clear §f- Clear selection"))
        player.sendMessage(Component.text(""))
        player.sendMessage(Component.text("§8Valid difficulties: ${VALID_DIFFICULTIES.joinToString(", ")}"))
    }



    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        when (args.size) {
            1 -> {
                return mutableListOf(
                    "tool", "create", "start", "stop", "open", "close", "queue",
                    "setstart", "setend", "setspawn", "setwaiting",
                    "list", "delete", "clear"
                ).filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()
            }

            2 -> {
                return when (args[0].lowercase()) {
                    "start", "stop", "open", "close", "queue", "setstart", "setend", "setspawn", "setwaiting", "delete" -> {
                        plugin.arenaManager.getAllArenas().map { it.id }
                            .filter { it.startsWith(args[1], ignoreCase = true) }
                            .toMutableList()
                    }
                    else -> mutableListOf()
                }
            }

            3 -> {
                return if (args[0].lowercase() == "create") {
                    VALID_DIFFICULTIES
                        .filter { it.startsWith(args[2], ignoreCase = true) }
                        .toMutableList()
                } else mutableListOf()
            }

            else -> return mutableListOf()
        }
    }
}
