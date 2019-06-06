package de.eternalwings.bukkit.sync

import java.util.function.BiConsumer

/**
 * Represents an arbitrary storage of keys to values that are synchronized using zookeeper.
 */
interface SynchronizedStorage {
    /**
     * Marks the given key as 'synchronized' and thus will be read and written from/to zookeeper.
     *
     * @param key the key to synchronize
     * @param type the type of data to expect
     * @param callback callback when changes occur (old value, new value)
     */
    fun <T : Any> synchronizeKey(key: String, type: Class<T>, callback: BiConsumer<T?, T?>): SynchronizedKey<T>

    /**
     * Marks the given key as 'synchronized' and thus will be read and written from/to zookeeper.
     *
     * @param key the key to synchronize
     * @param defaultValue default value to assign if there's no value yet
     * @param type the type of data to expect
     * @param callback callback when changes occur (old value, new value)
     */
    fun <T : Any> synchronizeKey(key: String, defaultValue: T?, type: Class<T>, callback: BiConsumer<T?, T?>): SynchronizedKey<T>

    /**
     * Gets all the keys that are synchronized under this storage.
     */
    fun getSynchronizedKeys(): Map<String, SynchronizedKey<*>>
}
