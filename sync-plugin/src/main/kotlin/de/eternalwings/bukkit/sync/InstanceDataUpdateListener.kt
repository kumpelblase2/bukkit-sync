package de.eternalwings.bukkit.sync

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

class InstanceDataUpdateListener(private val instanceWatcher: InstanceWatcherImpl) : Listener {
    @EventHandler
    fun onPluginEnabled(event: PluginEnableEvent) {
        refreshPluginsForThisInstance()
    }

    @EventHandler
    fun onPluginDisabled(event: PluginDisableEvent) {
        refreshPluginsForThisInstance()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // Join event is thrown after the player joined
        // thus the player list is already updated.
        refreshPlayerAmountForThisInstance()
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        // Since this event is thrown before the online players are updated,
        // we specifically have to remove one.
        refreshPlayerAmountForThisInstance(-1)
    }

    private fun refreshPlayerAmountForThisInstance(change: Int = 0) {
        instanceWatcher.thisInstance.playerCount = Bukkit.getServer().onlinePlayers.size + change
        instanceWatcher.update()
    }

    private fun refreshPluginsForThisInstance() {
        instanceWatcher.thisInstance.plugins = Bukkit.getServer().pluginManager.plugins.map { it.name }
        instanceWatcher.update()
    }
}
