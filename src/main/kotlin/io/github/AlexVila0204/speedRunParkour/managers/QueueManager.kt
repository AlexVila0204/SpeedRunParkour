package io.github.AlexVila0204.speedRunParkour.managers

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class QueueManager(private val plugin: SpeedRunParkour) {

    private val queues = ConcurrentHashMap<String, Queue<UUID>>()
    private val activePlayers = ConcurrentHashMap<String, UUID>()
    private val playerQueues = ConcurrentHashMap<UUID, String>()
    private val courseStates = ConcurrentHashMap<String, CourseState>()

    enum class CourseState {
        CLOSED,
        WAITING,
        ACTIVE
    }

    fun joinQueue(player: Player, difficulty: String): Boolean {
        val playerUUID = player.uniqueId

        if (playerQueues.containsKey(playerUUID)) {
            player.sendMessage(Component.text("§cYou are already in a queue!"))
            return false
        }

        val courses = plugin.arenaManager.getArenasByDifficulty(difficulty)
        if (courses.isEmpty()) {
            player.sendMessage(Component.text("§cNo courses found for difficulty: $difficulty"))
            return false
        }

        val courseId = courses.first().id
        val courseState = courseStates[courseId] ?: CourseState.WAITING

        when (courseState) {
            CourseState.CLOSED -> {
                player.sendMessage(Component.text("§cThis course is currently closed!"))
                return false
            }
            CourseState.ACTIVE -> {
                val queue = queues[courseId]
                val activePlayer = activePlayers[courseId]?.let { plugin.server.getPlayer(it)?.name }

                player.sendMessage(Component.text("§cQueue is currently being processed!"))
                player.sendMessage(Component.text("§7Currently playing: §f${activePlayer ?: "Unknown"}"))
                player.sendMessage(Component.text("§7Players remaining: ${queue?.size ?: 0}"))
                player.sendMessage(Component.text("§7Wait until all current players finish."))
                return false
            }
            CourseState.WAITING -> {
                val queue = queues.getOrPut(courseId) { LinkedList() }
                queue.offer(playerUUID)
                playerQueues[playerUUID] = courseId

                val course = plugin.arenaManager.getArena(courseId)
                if (course != null) {
                    player.teleport(course.waitingArea)
                    player.sendMessage(Component.text("§aYou have been added to the queue for $difficulty difficulty."))
                    player.sendMessage(Component.text("§7Position in queue: ${queue.size}"))
                    player.sendMessage(Component.text("§7Wait in the waiting area for an admin to start the arena."))
                }
                return true
            }
        }
    }

    fun leaveQueue(player: Player): Boolean {
        val playerUUID = player.uniqueId
        val courseId = playerQueues.remove(playerUUID) ?: return false

        queues[courseId]?.remove(playerUUID)

        val wasActivePlayer = activePlayers[courseId] == playerUUID
        if (wasActivePlayer) {
            activePlayers.remove(courseId)

            if (courseStates[courseId] == CourseState.ACTIVE) {
                processNextInQueue(courseId)
            } else {
                courseStates[courseId] = CourseState.WAITING
            }

            plugin.server.onlinePlayers
                .filter { it.hasPermission("parkourtimer.admin") }
                .forEach { admin ->
                    admin.sendMessage(Component.text("§6§lPlayer Left §8» §f${player.name} §7left course §e$courseId"))
                }
        }

        return true
    }

    fun finishCourse(player: Player) {
        val playerUUID = player.uniqueId
        val courseId = playerQueues[playerUUID]

        if (courseId == null) {
            return
        }

        val activePlayerUUID = activePlayers[courseId]

        if (activePlayers[courseId] == playerUUID) {
            activePlayers.remove(courseId)

            val queue = queues[courseId]
            val hasMorePlayers = !queue.isNullOrEmpty()

            if (courseStates[courseId] == CourseState.ACTIVE) {
                if (hasMorePlayers) {
                    processNextInQueue(courseId)
                } else {
                    courseStates[courseId] = CourseState.WAITING

                    plugin.server.onlinePlayers
                        .filter { it.hasPermission("parkourtimer.admin") }
                        .forEach { admin ->
                            admin.sendMessage(Component.text("§6§lQueue Complete §8» §fAll players finished course §e$courseId"))
                            admin.sendMessage(Component.text("§a§lCourse Available §8» §fQueue is now open for new players!"))
                        }
                }
            }

            plugin.server.onlinePlayers
                .filter { it.hasPermission("parkourtimer.admin") }
                .forEach { admin ->
                    admin.sendMessage(Component.text("§a§lPlayer Finished §8» §f${player.name} §7completed §e$courseId"))
                }
        }

        playerQueues.remove(playerUUID)
    }




    private fun processNextInQueue(courseId: String) {
        val queue = queues[courseId] ?: return

        if (courseStates[courseId] != CourseState.ACTIVE) {
            return
        }

        while (queue.isNotEmpty()) {
            val nextPlayerUUID = queue.poll()
            val nextPlayer = plugin.server.getPlayer(nextPlayerUUID)

            if (nextPlayer != null) {
                playerQueues.remove(nextPlayerUUID)

                plugin.server.scheduler.runTaskLater(plugin, Runnable {
                    startPlayerCourse(nextPlayer, courseId)

                    plugin.server.onlinePlayers
                        .filter { it.hasPermission("parkourtimer.admin") }
                        .forEach { admin ->
                            admin.sendMessage(Component.text("§a§lAuto-started §8» §f${nextPlayer.name} §7is now playing §e$courseId"))
                            admin.sendMessage(Component.text("§7Remaining in queue: §f${queue.size}"))
                        }

                    notifyQueuePositions(courseId)
                }, 80L)

                return
            }
        }

        courseStates[courseId] = CourseState.WAITING

        plugin.server.onlinePlayers
            .filter { it.hasPermission("parkourtimer.admin") }
            .forEach { admin ->
                admin.sendMessage(Component.text("§6§lQueue Complete §8» §fAll players finished course §e$courseId"))
                admin.sendMessage(Component.text("§a§lCourse Available §8» §fQueue is now open for new players!"))
            }
    }

    fun startCourse(courseId: String, admin: Player): Boolean {
        val queue = queues[courseId]

        if (queue.isNullOrEmpty()) {
            admin.sendMessage(Component.text("§cNo players in queue for course: $courseId"))
            return false
        }

        courseStates[courseId] = CourseState.ACTIVE

        val firstPlayerUUID = queue.poll()
        val firstPlayer = plugin.server.getPlayer(firstPlayerUUID)

        if (firstPlayer == null) {
            return startCourse(courseId, admin)
        }

        playerQueues.remove(firstPlayerUUID)
        startPlayerCourse(firstPlayer, courseId)

        admin.sendMessage(Component.text("§a§lQueue Started! §8» §f${firstPlayer.name} §7is now playing §e$courseId"))
        admin.sendMessage(Component.text("§7Remaining in queue: §f${queue.size}"))
        admin.sendMessage(Component.text("§6§lQueue Processing §8» §fPlayers will go by turns automatically"))
        admin.sendMessage(Component.text("§c§lQueue Locked §8» §fNo new players can join until finished"))

        notifyQueuePositions(courseId)
        return true
    }

    fun setCourseState(courseId: String, state: CourseState, admin: Player): Boolean {
        val course = plugin.arenaManager.getArena(courseId)
        if (course == null) {
            admin.sendMessage(Component.text("§cCourse not found: $courseId"))
            return false
        }

        courseStates[courseId] = state

        when (state) {
            CourseState.CLOSED -> {
                admin.sendMessage(Component.text("§cCourse §f$courseId §chas been closed"))

                activePlayers[courseId]?.let { playerUUID ->
                    val player = plugin.server.getPlayer(playerUUID)
                    if (player != null) {
                        plugin.timerManager.stopTimer(player)
                        player.teleport(course.waitingArea)
                        player.sendMessage(Component.text("§cCourse was closed by an admin"))
                    }
                    activePlayers.remove(courseId)
                }
            }
            CourseState.WAITING -> {
                admin.sendMessage(Component.text("§aCourse §f$courseId §ais now open and waiting for players"))

                activePlayers[courseId]?.let { playerUUID ->
                    val player = plugin.server.getPlayer(playerUUID)
                    if (player != null) {
                        plugin.timerManager.stopTimer(player)
                        player.teleport(course.waitingArea)
                        player.sendMessage(Component.text("§6Queue processing was stopped by admin"))
                    }
                    activePlayers.remove(courseId)
                }
            }
            CourseState.ACTIVE -> {
                admin.sendMessage(Component.text("§6Course §f$courseId §6is now processing queue"))
            }
        }

        return true
    }

    fun getQueueInfo(courseId: String): String? {
        val queue = queues[courseId]
        val state = courseStates[courseId] ?: CourseState.WAITING
        val activePlayer = activePlayers[courseId]?.let { plugin.server.getPlayer(it)?.name }

        return """
        §6=== Course: §f$courseId §6===
        §7State: §f${state.name}
        §7Queue Size: §f${queue?.size ?: 0}
        §7Active Player: §f${activePlayer ?: "None"}
        §7Status: ${when(state) {
            CourseState.WAITING -> "§aAccepting players"
            CourseState.ACTIVE -> "§6Processing queue"
            CourseState.CLOSED -> "§cClosed"
        }}
        """.trimIndent()
    }

    private fun startPlayerCourse(player: Player, courseId: String) {
        val course = plugin.arenaManager.getArena(courseId) ?: return

        activePlayers[courseId] = player.uniqueId
        playerQueues[player.uniqueId] = courseId

        player.teleport(course.spawnLocation)
        player.sendMessage(Component.text("§aYou have been teleported to the parkour course!"))
        player.sendMessage(Component.text("§7Step on the pressure plate to start your timer."))

        val remainingInQueue = queues[courseId]?.size ?: 0
        if (remainingInQueue > 0) {
            player.sendMessage(Component.text("§7Players waiting after you: §f$remainingInQueue"))
        }
    }


    private fun notifyQueuePositions(courseId: String) {
        val queue = queues[courseId] ?: return

        queue.forEachIndexed { index, playerUUID ->
            val player = plugin.server.getPlayer(playerUUID)
            player?.sendMessage(Component.text("§7Your position in queue: ${index + 1}"))
        }
    }

    fun getQueuePosition(player: Player): Int? {
        val playerUUID = player.uniqueId
        val courseId = playerQueues[playerUUID] ?: return null
        val queue = queues[courseId] ?: return null

        return queue.indexOf(playerUUID) + 1
    }

    fun isPlayerInCourse(player: Player): Boolean {
        return activePlayers.containsValue(player.uniqueId)
    }

    fun stopCourse(courseId: String, admin: Player): Boolean {
        val course = plugin.arenaManager.getArena(courseId)
        if (course == null) {
            admin.sendMessage(Component.text("§cCourse not found: $courseId"))
            return false
        }

        val currentState = courseStates[courseId] ?: CourseState.WAITING

        if (currentState != CourseState.ACTIVE) {
            admin.sendMessage(Component.text("§cCourse §f$courseId §cis not currently processing a queue!"))
            return false
        }

        val queue = queues[courseId]
        val queueSize = queue?.size ?: 0
        val activePlayerName = activePlayers[courseId]?.let { plugin.server.getPlayer(it)?.name }

        activePlayers[courseId]?.let { playerUUID ->
            val player = plugin.server.getPlayer(playerUUID)
            if (player != null) {
                plugin.timerManager.stopTimer(player)
                player.teleport(course.waitingArea)
                player.sendMessage(Component.text("§c§lQueue Reset! §8» §fThe entire queue was cleared by an admin"))
                player.sendMessage(Component.text("§7Use /parkourqueue to join again if you want to try"))
            }
        }

        if (!queue.isNullOrEmpty()) {
            val playersInQueue = mutableListOf<String>()

            queue.forEach { playerUUID ->
                val player = plugin.server.getPlayer(playerUUID)
                if (player != null) {
                    playersInQueue.add(player.name)
                    player.sendMessage(Component.text("§c§lQueue Reset! §8» §fThe entire queue was cleared by an admin"))
                    player.sendMessage(Component.text("§7Use /parkourqueue to join again if you want to try"))

                    playerQueues.remove(playerUUID)
                }
            }

            admin.sendMessage(Component.text("§7Players removed from queue: §f${playersInQueue.joinToString(", ")}"))
        }

        activePlayers.remove(courseId)
        queues[courseId]?.clear()
        queues.remove(courseId)

        courseStates[courseId] = CourseState.WAITING

        admin.sendMessage(Component.text("§a§lQueue Reset Complete! §8» §fCourse §e$courseId §fhas been completely reset"))
        admin.sendMessage(Component.text("§7- Active player: §f${activePlayerName ?: "None"} §7(removed)"))
        admin.sendMessage(Component.text("§7- Queue size: §f$queueSize §7players (all removed)"))
        admin.sendMessage(Component.text("§7- Status: §aOpen for new players"))

        plugin.server.onlinePlayers
            .filter { it.hasPermission("parkourtimer.admin") && it != admin }
            .forEach { otherAdmin ->
                otherAdmin.sendMessage(Component.text("§c§lQueue Reset §8» §f${admin.name} §7completely reset course §e$courseId"))
                otherAdmin.sendMessage(Component.text("§7All players removed from queue ($queueSize players)"))
            }

        plugin.logger.info("Admin ${admin.name} reset course $courseId queue (removed $queueSize players)")
        return true
    }


    fun getQueueSizeForDifficulty(difficulty: String): Int {
        val courses = plugin.arenaManager.getArenasByDifficulty(difficulty)
        return courses.sumOf { course ->
            queues[course.id]?.size ?: 0
        }
    }

    fun getTotalQueueSize(): Int {
        return queues.values.sumOf { it.size }
    }

    fun getActiveCourseCount(): Int {
        return activePlayers.size
    }

    fun getAllCourseIds(): List<String> {
        return plugin.arenaManager.getAllArenas().map { it.id }
    }
}