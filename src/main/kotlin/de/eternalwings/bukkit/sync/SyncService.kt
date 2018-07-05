package de.eternalwings.bukkit.sync

import org.bukkit.configuration.Configuration

interface SyncService {
    fun getSynchronizedConfig(configuration: Configuration): SynchronizedConfig
}
