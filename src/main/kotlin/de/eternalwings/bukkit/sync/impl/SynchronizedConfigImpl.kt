package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.SynchronizedConfig
import org.apache.curator.framework.CuratorFramework
import org.bukkit.configuration.Configuration
import java.util.function.BiConsumer

class SynchronizedConfigImpl(private val curatorFramework: CuratorFramework, private val originalConfiguration: Configuration,
                             private val name: String, private val pluginName: String) : SynchronizedConfig,
        Configuration by originalConfiguration {
    private var synchronizedKeys: Map<String, SyncedConfigurationKey<*>> = emptyMap()

    override fun getName(): String {
        return name
    }

    override fun <T : Any> synchronizeKey(configurationKey: String, type: Class<T>, callback: BiConsumer<T?, T?>) {
        this.synchronizeKey(configurationKey, originalConfiguration.get(configurationKey) as T?, type, callback)
    }

    override fun <T : Any> synchronizeKey(configurationKey: String, defaultValue: T?, type: Class<T>,
                                          callback: BiConsumer<T?, T?>) {
        this.synchronizeKey(configurationKey, defaultValue, type, true, callback)
    }

    override fun <T : Any> synchronizeKey(configurationKey: String, defaultValue: T?, type: Class<T>, autoPersist: Boolean,
                                          callback: BiConsumer<T?, T?>) {
        val configurationNode = SyncedConfigurationKey(curatorFramework, asZookeeperPath(configurationKey), type, defaultValue) {
            val currentValue = originalConfiguration.get(configurationKey) as T?
            if (currentValue != it) {
                callback.accept(currentValue, it)
                if (autoPersist) {
                    originalConfiguration.set(configurationKey, it)
                }
            }
        }
        synchronizedKeys += configurationKey to configurationNode
    }

    override fun set(key: String?, value: Any?) {
        key?.let {
            val currentValue = this.get(key)
            if (currentValue == value) {
                return@let
            }

            val syncedConfigurationKey = synchronizedKeys[it]
            if (syncedConfigurationKey != null) {
                (syncedConfigurationKey as SyncedConfigurationKey<Any>).synchronizeValue(value)
            } else {
                originalConfiguration.set(key, value)
            }
        }
    }

    override fun get(givenKey: String?, default: Any?): Any? {
        val key = givenKey ?: return originalConfiguration.get(givenKey, default)
        val syncedConfigurationKey = synchronizedKeys[key]
        return syncedConfigurationKey?.getCurrentValue() ?: default
    }

    private fun asZookeeperPath(configurationPath: String): String {
        val configName = if (name.isEmpty()) "DEFAULT" else name
        val fullNamespace = "/" + listOf(pluginName, configName).joinToString("/") + "/"
        return fullNamespace + configurationPath.replace(this.originalConfiguration.options().pathSeparator().toString(), "/")
    }
}
