package de.eternalwings.bukkit.sync

import org.bukkit.configuration.Configuration
import org.bukkit.plugin.Plugin

/**
 * Service to be used to work with the sync plugin.
 */
interface SyncService {
    /**
     * Gets the synchronized configuration representing the given configuration.
     */
    fun getSynchronizedConfig(configuration: Configuration, owner: Plugin, name: String): SynchronizedConfig

    /**
     * Gets all the configurations of the given plugin that have been synchronized.
     */
    fun getSynchronizedConfigurationsOf(plugin: Plugin): Collection<SynchronizedConfig>

    /**
     * Returns the general synchronized storage for this plugin.
     */
    fun getSynchronizedStorage(owner: Plugin): SynchronizedStorage {
        return getSynchronizedStorage(owner, DEFAULT_CONFIG_NAME)
    }

    /**
     * Returns a storage of keys that is synchronized across all instances for this plugin
     * with the given name.
     */
    fun getSynchronizedStorage(owner: Plugin, name: String): SynchronizedStorage

    fun getQueueManager(owner: Plugin): QueueManager

    /**
     * Gets the instance watcher. Will return null unless `announce_instance` configuration is
     * set to `true`.
     */
    val instanceWatcher: InstanceWatcher?

    companion object {
        internal const val DEFAULT_CONFIG_NAME = "" // Knowingly empty.
    }
}
