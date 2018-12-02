package de.eternalwings.bukkit.sync.event

import org.bukkit.configuration.Configuration
import org.bukkit.event.HandlerList
import org.bukkit.plugin.Plugin

class ConfigurationKeyUpdated<T>(path: String, context: String, plugin: Plugin, val configuration: Configuration?, oldValue: T?,
                                 newValue: T?) : StorageKeyUpdated<T>(path, context, plugin, oldValue, newValue) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
