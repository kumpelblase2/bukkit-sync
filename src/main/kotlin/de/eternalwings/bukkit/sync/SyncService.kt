package de.eternalwings.bukkit.sync

import org.bukkit.configuration.Configuration
import org.bukkit.plugin.Plugin

interface SyncService {
    fun getSynchronizedConfig(configuration: Configuration, owner: Plugin, name: String): SynchronizedConfig

    fun getSynchronizedConfigurationsOf(plugin: Plugin): Collection<SynchronizedConfig>

    fun getSynchronizedStorage(owner: Plugin): SynchronizedStorage {
        return getSynchronizedStorage(owner, DEFAULT_CONFIG_NAME)
    }

    fun getSynchronizedStorage(owner: Plugin, name: String): SynchronizedStorage

    val instanceWatcher: InstanceWatcher?

    companion object {
        const val DEFAULT_CONFIG_NAME = "" // Knowingly empty.
    }
}
