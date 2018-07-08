package de.eternalwings.bukkit.sync

import org.bukkit.configuration.Configuration
import org.bukkit.plugin.Plugin

interface SyncService {
    fun getSynchronizedConfig(configuration: Configuration, owner: Plugin): SynchronizedConfig {
        return getSynchronizedConfig(configuration, owner, DEFAULT_CONFIG_NAME)
    }

    fun getSynchronizedConfig(configuration: Configuration, owner: Plugin, name: String): SynchronizedConfig

    fun getSynchronizedConfigurationsOf(plugin: Plugin): Collection<SynchronizedConfig>

    companion object {
        const val DEFAULT_CONFIG_NAME = "" // Knowingly empty.
    }
}
