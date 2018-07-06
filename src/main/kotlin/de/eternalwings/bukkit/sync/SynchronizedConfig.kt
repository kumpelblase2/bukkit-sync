package de.eternalwings.bukkit.sync

import org.bukkit.configuration.Configuration
import java.util.function.BiConsumer

interface SynchronizedConfig : Configuration {
    fun <T : Any> synchronizeKey(configurationKey: String, defaultValue: T?, type: Class<T>, callback: BiConsumer<T?, T?>)

    fun <T : Any> synchronizeKey(configurationKey: String, defaultValue: T?, type: Class<T>, autoPersist: Boolean, callback: BiConsumer<T?, T?>)
}
