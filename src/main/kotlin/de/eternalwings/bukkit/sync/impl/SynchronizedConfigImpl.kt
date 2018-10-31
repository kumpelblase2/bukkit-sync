package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.SynchronizedConfig
import de.eternalwings.bukkit.sync.SynchronizedKey
import org.apache.curator.framework.CuratorFramework
import org.bukkit.configuration.Configuration
import java.util.function.BiConsumer

class SynchronizedConfigImpl(curatorFramework: CuratorFramework, private val originalConfiguration: Configuration,
                             private val name: String, zkPathProvider: (String) -> String) :
        SynchronizedKeyCollection(curatorFramework, zkPathProvider), SynchronizedConfig, Configuration by originalConfiguration {

    override fun getName(): String {
        return name
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
