package de.eternalwings.bukkit.sync

import org.bukkit.configuration.Configuration
import java.util.function.BiConsumer

interface SynchronizedConfig : Configuration, SynchronizedStorage {
    fun <T : Any> synchronizeKey(key: String, defaultValue: T?, type: Class<T>, autoPersist: Boolean,
                                 callback: BiConsumer<T?, T?>): SynchronizedKey<T>
}
