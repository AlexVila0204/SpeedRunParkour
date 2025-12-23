package io.github.AlexVila0204.speedRunParkour.placeholders

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class ParkourPlaceholders(private val plugin: SpeedRunParkour) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "parkour"

    override fun getAuthor(): String = plugin.description.authors.joinToString(", ")

    override fun getVersion(): String = plugin.description.version

    override fun persist(): Boolean = true

    override fun canRegister(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) return null

        val args = params.split("_")
        if (args.size < 3) return null

        return when (args[0]) {
            "current" -> {
                if (args[1] == "time" && player.isOnline) {
                    val courseId = args.drop(2).joinToString("_")
                    val onlinePlayer = player.player ?: return "0:00.000"

                    val currentTime = plugin.timerManager.getCurrentTime(onlinePlayer)
                    val currentCourse = plugin.timerManager.getCurrentCourse(onlinePlayer)

                    if (currentTime != null && currentCourse == courseId) {
                        formatTime(currentTime)
                    } else {
                        "0:00.000"
                    }
                } else null
            }
            "best" -> {
                when (args[1]) {
                    "time" -> {
                        val courseId = args.drop(2).joinToString("_")
                        val bestTime = plugin.dataManager.getBestTime(player.uniqueId, courseId)
                        bestTime?.getFormattedTime() ?: "No time"
                    }
                    "player" -> {
                        val courseId = args.drop(2).joinToString("_")
                        val bestPlayer = plugin.dataManager.getBestPlayerForCourse(courseId)
                        bestPlayer?.playerName ?: "No record"
                    }
                    else -> null
                }
            }
            "queue" -> {
                when (args[1]) {
                    "position" -> {
                        if (player.isOnline) {
                            val onlinePlayer = player.player ?: return "0"
                            plugin.queueManager.getQueuePosition(onlinePlayer)?.toString() ?: "0"
                        } else "0"
                    }
                    "size" -> {
                        if (args.size >= 3) {
                            val difficulty = args.drop(2).joinToString("_").uppercase()
                            getQueueSize(difficulty).toString()
                        } else "0"
                    }
                    else -> null
                }
            }
            "stats" -> {
                when (args[1]) {
                    "total" -> {
                        when (args[2]) {
                            "attempts" -> {
                                val allCourses = plugin.arenaManager.getAllArenas()
                                allCourses.sumOf { course ->
                                    plugin.dataManager.getPlayerTimes(player.uniqueId, course.id).size
                                }.toString()
                            }
                            "completed" -> {
                                val allCourses = plugin.arenaManager.getAllArenas()
                                allCourses.count { course ->
                                    plugin.dataManager.getBestTime(player.uniqueId, course.id) != null
                                }.toString()
                            }
                            else -> null
                        }
                    }
                    "difficulty" -> {
                        if (args.size >= 4) {
                            val difficulty = args[2].uppercase()
                            when (args[3]) {
                                "completed" -> {
                                    val courses = plugin.arenaManager.getArenasByDifficulty(difficulty)
                                    courses.count { course ->
                                        plugin.dataManager.getBestTime(player.uniqueId, course.id) != null
                                    }.toString()
                                }
                                "best" -> {
                                    val courses = plugin.arenaManager.getArenasByDifficulty(difficulty)
                                    val bestTime = courses.mapNotNull { course ->
                                        plugin.dataManager.getBestTime(player.uniqueId, course.id)
                                    }.minByOrNull { it.timeMillis }
                                    bestTime?.getFormattedTime() ?: "No time"
                                }
                                else -> null
                            }
                        } else null
                    }
                    else -> null
                }
            }
            "leaderboard" -> {
                if (args.size >= 4) {
                    val courseId = args.drop(2).dropLast(1).joinToString("_")
                    val position = args.last().toIntOrNull() ?: return null

                    val topPlayers = getTopPlayersForCourse(courseId)
                    if (position <= topPlayers.size && position > 0) {
                        val playerTime = topPlayers[position - 1]
                        when (args[1]) {
                            "name" -> playerTime.playerName
                            "time" -> playerTime.getFormattedTime()
                            else -> null
                        }
                    } else {
                        when (args[1]) {
                            "name" -> "No player"
                            "time" -> "No time"
                            else -> null
                        }
                    }
                } else null
            }
            else -> null
        }
    }

    private fun formatTime(timeMillis: Long): String {
        val minutes = timeMillis / 60000
        val seconds = (timeMillis % 60000) / 1000
        val milliseconds = timeMillis % 1000
        return String.format("%d:%02d.%03d", minutes, seconds, milliseconds)
    }

    private fun getQueueSize(difficulty: String): Int {
        return plugin.queueManager.getQueueSizeForDifficulty(difficulty)
    }

    private fun getTopPlayersForCourse(courseId: String): List<io.github.AlexVila0204.speedRunParkour.models.PlayerTime> {
        val allPlayers = plugin.server.offlinePlayers
        val allTimes = allPlayers.mapNotNull { player ->
            plugin.dataManager.getBestTime(player.uniqueId, courseId)
        }.sortedBy { it.timeMillis }

        return allTimes.take(10)
    }
}