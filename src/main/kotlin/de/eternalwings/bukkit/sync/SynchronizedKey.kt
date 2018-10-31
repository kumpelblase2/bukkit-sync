package de.eternalwings.bukkit.sync

interface SynchronizedKey<T : Any> {
    fun synchronizeValue(value: T?)

    fun getCurrentValue(): T?
}
