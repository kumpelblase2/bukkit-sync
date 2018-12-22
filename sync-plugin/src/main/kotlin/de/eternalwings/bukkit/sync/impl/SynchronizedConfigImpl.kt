package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.SynchronizedConfig
import de.eternalwings.bukkit.sync.SynchronizedKey
import de.eternalwings.bukkit.sync.event.ConfigurationKeyUpdated
import org.apache.curator.framework.CuratorFramework
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.plugin.Plugin
import java.util.function.BiConsumer

class SynchronizedConfigImpl(curatorFramework: CuratorFramework, private val originalConfiguration: Configuration,
                             private val plugin: Plugin, private val context: String, zkPathProvider: (String) -> String) :
        SynchronizedKeyCollection(curatorFramework, zkPathProvider), SynchronizedConfig, Configuration by originalConfiguration {

    override fun getName(): String {
        return plugin.name
    }

    override fun <T : Any> synchronizeKey(key: String, type: Class<T>, callback: BiConsumer<T?, T?>): SynchronizedKey<T> {
        return synchronizeKey(key, originalConfiguration.get(key) as T?, type, callback)
    }

    override fun <T : Any> synchronizeKey(key: String, defaultValue: T?, type: Class<T>,
                                          callback: BiConsumer<T?, T?>): SynchronizedKey<T> {
        return synchronizeKey(key, defaultValue, type, true, callback)
    }

    override fun <T : Any> synchronizeKey(key: String, defaultValue: T?, type: Class<T>, autoPersist: Boolean,
                                          callback: BiConsumer<T?, T?>): SynchronizedKey<T> {
        if (keyMap.containsKey(key)) {
            return keyMap[key] as SynchronizedKey<T>
        }
        val configurationNode = create(key, defaultValue, type) { oldValue, newValue ->
            if (oldValue != newValue) {
                val event = ConfigurationKeyUpdated(key, context, plugin, originalConfiguration, oldValue, newValue)
                Bukkit.getServer().pluginManager.callEvent(event)

                callback.accept(oldValue, newValue)
                if (autoPersist) {
                    originalConfiguration.set(key, newValue)
                }
            }
        }
        keyMap += key to configurationNode
        return configurationNode
    }

    override fun set(key: String?, value: Any?) {
        key?.let {
            val currentValue = get(key)
            if (currentValue == value) {
                return@let
            }

            val syncedConfigurationKey = keyMap[it]
            if (syncedConfigurationKey != null) {
                (syncedConfigurationKey as SyncedConfigurationKey<Any>).synchronizeValue(value)
            } else {
                originalConfiguration.set(key, value)
            }
        }
    }

    override fun get(givenKey: String?, default: Any?): Any? {
        val key = givenKey ?: return originalConfiguration.get(givenKey, default)
        val syncedConfigurationKey = keyMap[key]
        return syncedConfigurationKey?.getCurrentValue() ?: default
    }
}
