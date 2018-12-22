package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.InstanceWatcher
import de.eternalwings.bukkit.sync.SyncService
import de.eternalwings.bukkit.sync.SynchronizedConfig
import de.eternalwings.bukkit.sync.SynchronizedStorage
import org.apache.curator.framework.CuratorFramework
import org.bukkit.configuration.Configuration
import org.bukkit.plugin.Plugin

class SyncServiceImpl(private val zookeeper: CuratorFramework, override val instanceWatcher: InstanceWatcher?) : SyncService {
    private var configurationForPluginsMap = emptyMap<String, List<SynchronizedConfig>>()

    override fun getSynchronizedConfig(configuration: Configuration, owner: Plugin, name: String): SynchronizedConfig {
        val configurationListForPlugin = configurationForPluginsMap[owner.name] ?: emptyList()
        val existingConfig = configurationListForPlugin.find { it.name == name }
        if (existingConfig != null) {
            return existingConfig
        }

        val createdConfig = createSynchronizedConfig(configuration, owner, name)
        configurationForPluginsMap += owner.name to (configurationListForPlugin + createdConfig)

        return createdConfig
    }

    private fun createSynchronizedConfig(configuration: Configuration, owner: Plugin, name: String): SynchronizedConfig {
        return SynchronizedConfigImpl(zookeeper, configuration, owner, name) {
            asZookeeperPath(it, owner.name, name, configuration.options().pathSeparator().toString())
        }
    }

    override fun getSynchronizedStorage(owner: Plugin, name: String): SynchronizedStorage {
        return SynchronizedStorageImpl(zookeeper, owner, name) {
            asZookeeperPath(it, owner.name, name)
        }
    }

    override fun getSynchronizedConfigurationsOf(plugin: Plugin): Collection<SynchronizedConfig> {
        return configurationForPluginsMap[plugin.name] ?: emptyList()
    }

    private fun asZookeeperPath(configurationPath: String, pluginName: String, contextName: String,
                                keySeparator: String = "."): String {
        val configName = if (contextName.isEmpty()) "DEFAULT" else contextName
        val fullNamespace = "/" + listOf(pluginName, configName).joinToString("/") + "/"
        return fullNamespace + configurationPath.replace(keySeparator, "/")
    }
}
