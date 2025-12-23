
package io.github.AlexVila0204.speedRunParkour.data

import io.github.AlexVila0204.speedRunParkour.SpeedRunParkour
import io.github.AlexVila0204.speedRunParkour.models.PlayerTime
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

class SqliteStorage(private val plugin: SpeedRunParkour) : Storage {

    private lateinit var connection: Connection
    private val dbFile = File(plugin.dataFolder, "speedrunparkour.db")

    override fun initialize() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        try {
            Class.forName("org.sqlite.JDBC")
            connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
            createTables()
            plugin.logger.info("SQLite database initialized successfully")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize SQLite database: ${e.message}")
            throw e
        }
    }

    private fun createTables() {
        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS player_times (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_uuid TEXT NOT NULL,
                player_name TEXT NOT NULL,
                course_id TEXT NOT NULL,
                time_millis INTEGER NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent()

        val createIndexSQL = """
            CREATE INDEX IF NOT EXISTS idx_player_course 
            ON player_times(player_uuid, course_id, time_millis)
        """.trimIndent()

        try {
            connection.createStatement().execute(createTableSQL)
            connection.createStatement().execute(createIndexSQL)
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to create database tables: ${e.message}")
            throw e
        }
    }

    override fun saveTime(playerTime: PlayerTime) {
        val sql = """
            INSERT INTO player_times (player_uuid, player_name, course_id, time_millis, timestamp)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        try {
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, playerTime.playerUUID.toString())
                statement.setString(2, playerTime.playerName)
                statement.setString(3, playerTime.courseId)
                statement.setLong(4, playerTime.timeMillis)
                statement.setLong(5, playerTime.timestamp)
                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to save time: ${e.message}")
            throw e
        }
    }

    override fun getBestTime(playerUUID: UUID, courseId: String): PlayerTime? {
        val sql = """
            SELECT player_uuid, player_name, course_id, time_millis, timestamp
            FROM player_times
            WHERE player_uuid = ? AND course_id = ?
            ORDER BY time_millis ASC
            LIMIT 1
        """.trimIndent()

        try {
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, playerUUID.toString())
                statement.setString(2, courseId)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return PlayerTime(
                            UUID.fromString(resultSet.getString("player_uuid")),
                            resultSet.getString("player_name"),
                            resultSet.getString("course_id"),
                            resultSet.getLong("time_millis"),
                            resultSet.getLong("timestamp")
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to get best time: ${e.message}")
        }

        return null
    }

    override fun getBestPlayerForCourse(courseId: String): PlayerTime? {
        val sql = """
            SELECT player_uuid, player_name, course_id, time_millis, timestamp
            FROM player_times
            WHERE course_id = ?
            ORDER BY time_millis ASC
            LIMIT 1
        """.trimIndent()

        try {
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, courseId)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return PlayerTime(
                            UUID.fromString(resultSet.getString("player_uuid")),
                            resultSet.getString("player_name"),
                            resultSet.getString("course_id"),
                            resultSet.getLong("time_millis"),
                            resultSet.getLong("timestamp")
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to get best player for course: ${e.message}")
        }

        return null
    }

    override fun getPlayerTimes(playerUUID: UUID, courseId: String): List<PlayerTime> {
        val sql = """
            SELECT player_uuid, player_name, course_id, time_millis, timestamp
            FROM player_times
            WHERE player_uuid = ? AND course_id = ?
            ORDER BY time_millis ASC
        """.trimIndent()

        val times = mutableListOf<PlayerTime>()

        try {
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, playerUUID.toString())
                statement.setString(2, courseId)

                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        times.add(PlayerTime(
                            UUID.fromString(resultSet.getString("player_uuid")),
                            resultSet.getString("player_name"),
                            resultSet.getString("course_id"),
                            resultSet.getLong("time_millis"),
                            resultSet.getLong("timestamp")
                        ))
                    }
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to get player times: ${e.message}")
        }

        return times
    }
    override fun getTopPlayersForCourse(courseId: String, limit: Int): List<PlayerTime> {
        val sql = """
            SELECT player_uuid, player_name, course_id, time_millis, timestamp
            FROM player_times
            WHERE course_id = ?
            GROUP BY player_uuid
            HAVING time_millis = MIN(time_millis)
            ORDER BY time_millis ASC
            LIMIT ?
        """.trimIndent()

        val times = mutableListOf<PlayerTime>()

        try {
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, courseId)
                statement.setInt(2, limit)

                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        times.add(PlayerTime(
                            UUID.fromString(resultSet.getString("player_uuid")),
                            resultSet.getString("player_name"),
                            resultSet.getString("course_id"),
                            resultSet.getLong("time_millis"),
                            resultSet.getLong("timestamp")
                        ))
                    }
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to get top players for course: ${e.message}")
        }

        return times
    }

    override fun saveAllData() {
        try {
            if (!connection.autoCommit) {
                connection.commit()
            }
        } catch (e: SQLException) {
            plugin.logger.warning("Failed to commit SQLite transactions: ${e.message}")
        }
    }
}