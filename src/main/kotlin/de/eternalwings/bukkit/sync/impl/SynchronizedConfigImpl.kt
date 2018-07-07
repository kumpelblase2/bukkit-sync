package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.SynchronizedConfig
import org.apache.curator.framework.CuratorFramework
import org.bukkit.configuration.Configuration
import java.util.function.BiConsumer

class SynchronizedConfigImpl(private val curatorFramework: CuratorFramework, private val originalConfiguration: Configuration) : SynchronizedConfig,
        Configuration by originalConfiguration {

    private var synchronizedKeys: Map<String, SyncedConfigurationKey<*>> = emptyMap()

    override fun <T : Any> synchronizeKey(configurationKey: String, defaultValue: T?, type: Class<T>, callback: BiConsumer<T?, T?>) {
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

    override fun set(p0: String?, p1: Any?) {
        p0?.let {
            val currentValue = this.get(p0)
            if (currentValue == p1) {
                return@let
            }

            val syncedConfigurationKey = synchronizedKeys[it]
            if (syncedConfigurationKey != null) {
                (syncedConfigurationKey as SyncedConfigurationKey<Any>).synchronizeValue(value)
            } else {
                originalConfiguration.set(p0, p1)
            }
        }
    }

    override fun get(givenKey: String?, default: Any?): Any? {
        val key = givenKey ?: return originalConfiguration.get(givenKey, default)
        val syncedConfigurationKey = synchronizedKeys[key]
        return syncedConfigurationKey?.getCurrentValue() ?: default
    }

    private fun asZookeeperPath(configurationPath: String): String {
        return "/" + configurationPath.replace(this.originalConfiguration.options().pathSeparator().toString(), "/")
    }
}
