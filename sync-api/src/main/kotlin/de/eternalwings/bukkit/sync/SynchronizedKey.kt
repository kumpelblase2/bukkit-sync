package de.eternalwings.bukkit.sync

interface SynchronizedKey<T : Any> {
    /**
     * Updates the value of this key to get given value.
     */
    fun synchronizeValue(value: T?)

    /**
     * Returns the current value for this key.
     */
    fun getCurrentValue(): T?
}
