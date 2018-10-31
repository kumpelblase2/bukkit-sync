package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.SynchronizedKey
import org.apache.curator.framework.CuratorFramework
import java.util.function.BiConsumer

class SynchronizedStorageImpl(curatorFramework: CuratorFramework, zkPathProvider: (String) -> String) :
        SynchronizedKeyCollection(curatorFramework, zkPathProvider) {

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
                callback.accept(oldValue, newValue)
            }
        }
        keyMap += key to configurationNode
        return configurationNode
    }

}
