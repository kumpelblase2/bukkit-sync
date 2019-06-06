package de.eternalwings.bukkit.sync

import org.bukkit.configuration.Configuration
import java.util.function.BiConsumer

/**
 * A configuration that will be synchronized through the central zookeeper store. By default
 * no keys of the configuration will be included; they have to specifically marked using
 * [synchronizeKey].
 */
interface SynchronizedConfig : Configuration, SynchronizedStorage {
    /**
     * Marks the given key as 'synchronized' and thus will be read and written from/to zookeeper
     * and mirrored into the local configuration file.
     *
     * @param key the key to synchronize
     * @param defaultValue the default value to use if none is present
     * @param type the type of data to expect
     * @param autoPersist automatically persist changes to local file configuration
     * @param callback callback when changes occur (old value, new value)
     */
    fun <T : Any> synchronizeKey(key: String, defaultValue: T?, type: Class<T>, autoPersist: Boolean,
                                 callback: BiConsumer<T?, T?>): SynchronizedKey<T>
}
