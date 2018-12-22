package de.eternalwings.bukkit.sync

interface SynchronizedQueue {
    val maxSize: Int
    val name: String

    fun addToQueue(element: String)

    fun removeFromQueue(element: String)
}
