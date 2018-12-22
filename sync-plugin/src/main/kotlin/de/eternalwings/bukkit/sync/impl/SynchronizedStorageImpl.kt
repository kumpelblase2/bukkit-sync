package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.SynchronizedKey
import de.eternalwings.bukkit.sync.event.StorageKeyUpdated
import org.apache.curator.framework.CuratorFramework
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.function.BiConsumer

class SynchronizedStorageImpl(curatorFramework: CuratorFramework, private val plugin: Plugin, private val context: String,
                              zkPathProvider: (String) -> String) : SynchronizedKeyCollection(curatorFramework, zkPathProvider) {

    override fun <T : Any> synchronizeKey(key: String, type: Class<T>, callback: BiConsumer<T?, T?>): SynchronizedKey<T> {
        return synchronizeKey(key, null, type, callback)
    }

    override fun <T : Any> synchronizeKey(key: String, defaultValue: T?, type: Class<T>,
                                          callback: BiConsumer<T?, T?>): SynchronizedKey<T> {
        if (keyMap.containsKey(key)) {
            return keyMap[key] as SynchronizedKey<T>
        }

        val configurationNode = create(key, defaultValue, type) { oldValue, newValue ->
            if (oldValue != newValue) {
                val event = StorageKeyUpdated(key, context, plugin, oldValue, newValue)
                Bukkit.getServer().pluginManager.callEvent(event)
                callback.accept(oldValue, newValue)
            }
        }
        keyMap += key to configurationNode
        return configurationNode
    }

}
