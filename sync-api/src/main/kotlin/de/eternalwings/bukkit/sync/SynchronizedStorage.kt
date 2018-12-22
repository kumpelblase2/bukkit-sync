package de.eternalwings.bukkit.sync

import java.util.function.BiConsumer

interface SynchronizedStorage {
    fun <T : Any> synchronizeKey(key: String, type: Class<T>, callback: BiConsumer<T?, T?>): SynchronizedKey<T>

    fun <T : Any> synchronizeKey(key: String, defaultValue: T?, type: Class<T>, callback: BiConsumer<T?, T?>): SynchronizedKey<T>

    fun getSynchronizedKeys(): Map<String, SynchronizedKey<*>>
}
