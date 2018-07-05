package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.SyncService
import de.eternalwings.bukkit.sync.SynchronizedConfig
import org.apache.curator.framework.CuratorFramework
import org.bukkit.configuration.Configuration

class SyncServiceImpl(private val zookeeper: CuratorFramework) : SyncService {
    override fun getSynchronizedConfig(configuration: Configuration): SynchronizedConfig {
        return SynchronizedConfigImpl(zookeeper, configuration)
    }
}
