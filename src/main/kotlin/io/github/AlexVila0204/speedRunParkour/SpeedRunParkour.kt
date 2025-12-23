
package io.github.AlexVila0204.speedRunParkour

import io.github.AlexVila0204.speedRunParkour.commands.ParkourAdminCommand
import io.github.AlexVila0204.speedRunParkour.commands.ParkourQueueCommand
import io.github.AlexVila0204.speedRunParkour.commands.ParkourReloadCommand
import io.github.AlexVila0204.speedRunParkour.commands.ParkourTopCommand
import io.github.AlexVila0204.speedRunParkour.config.ConfigManager
import io.github.AlexVila0204.speedRunParkour.data.DataManager
import io.github.AlexVila0204.speedRunParkour.gui.MenuManager
import io.github.AlexVila0204.speedRunParkour.listeners.MenuListener
import io.github.AlexVila0204.speedRunParkour.listeners.PlayerDeathListener
import io.github.AlexVila0204.speedRunParkour.listeners.PressurePlateListener
import io.github.AlexVila0204.speedRunParkour.listeners.SelectionListener
import io.github.AlexVila0204.speedRunParkour.managers.ArenaManager
import io.github.AlexVila0204.speedRunParkour.managers.QueueManager
import io.github.AlexVila0204.speedRunParkour.managers.TimerManager
import io.github.AlexVila0204.speedRunParkour.placeholders.ParkourPlaceholders
import io.github.AlexVila0204.speedRunParkour.selection.SelectionManager
import io.github.AlexVila0204.speedRunParkour.listeners.PlayerQuitListener
import org.bukkit.plugin.java.JavaPlugin

class SpeedRunParkour : JavaPlugin() {

    lateinit var configManager: ConfigManager
    lateinit var selectionManager: SelectionManager
    lateinit var arenaManager: ArenaManager
    lateinit var timerManager: TimerManager
    lateinit var queueManager: QueueManager
    lateinit var dataManager: DataManager
    lateinit var menuManager: MenuManager

    override fun onEnable() {
        logger.info("Enabling SpeedRunParkour...")

        configManager = ConfigManager(this)
        selectionManager = SelectionManager(this)
        arenaManager = ArenaManager(this)
        timerManager = TimerManager(this)
        queueManager = QueueManager(this)
        dataManager = DataManager(this)
        menuManager = MenuManager(this)


        configManager.loadConfig()
        arenaManager.initialize()
        dataManager.initialize()

        server.pluginManager.registerEvents(SelectionListener(this), this)
        server.pluginManager.registerEvents(PressurePlateListener(this), this)
        server.pluginManager.registerEvents(MenuListener(this), this)
        server.pluginManager.registerEvents(PlayerDeathListener(this), this)
        server.pluginManager.registerEvents(PlayerQuitListener(this), this)


        getCommand("parkour")?.setExecutor(ParkourAdminCommand(this))
        getCommand("parkourqueue")?.setExecutor(ParkourQueueCommand(this))
        getCommand("parkourreload")?.setExecutor(ParkourReloadCommand(this))
        getCommand("parkourtop")?.setExecutor(ParkourTopCommand(this))


        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            ParkourPlaceholders(this).register()
            logger.info("PlaceholderAPI integration enabled successfully!")
        } else {
            logger.warning("PlaceholderAPI not found! Placeholders will not work.")
        }

        logger.info("SpeedRunParkour enabled successfully!")
    }

    override fun onDisable() {
        if (::dataManager.isInitialized) {
            dataManager.saveAllData()
        }

        if (::timerManager.isInitialized) {
            timerManager.clearAllTimers()
        }

        logger.info("SpeedRunParkour disabled successfully!")
    }
}