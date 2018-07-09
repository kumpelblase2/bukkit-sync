package de.eternalwings.bukkit.sync

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

class PluginChangeListener(private val instanceWatcher: InstanceWatcher) : Listener {
    @EventHandler
    fun onPluginEnabled(event: PluginEnableEvent) {
        refreshPluginsForThisInstance()
    }

    @EventHandler
    fun onPluginDisabled(event: PluginDisableEvent) {
        refreshPluginsForThisInstance()
    }

    private fun refreshPluginsForThisInstance() {
        instanceWatcher.thisInstance.plugins = Bukkit.getServer().pluginManager.plugins.map { it.name }
        instanceWatcher.update()
    }
}
