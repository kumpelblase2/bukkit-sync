package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.SynchronizedKey
import de.eternalwings.bukkit.sync.SynchronizedStorage
import org.apache.curator.framework.CuratorFramework

abstract class SynchronizedKeyCollection(private val curatorFramework: CuratorFramework,
                                         private val zkPathProvider: (String) -> String) : SynchronizedStorage {
    override fun getSynchronizedKeys(): Map<String, SynchronizedKey<*>> {
        return keyMap
    }

    protected var keyMap: Map<String, SynchronizedKey<*>> = emptyMap()

    protected fun <T : Any> create(key: String, defaultValue: T?, type: Class<T>,
                                   callback: (T?, T?) -> Unit): SynchronizedKey<T> {
        return SyncedConfigurationKey(curatorFramework, zkPathProvider(key), type, defaultValue, callback)
    }
}
