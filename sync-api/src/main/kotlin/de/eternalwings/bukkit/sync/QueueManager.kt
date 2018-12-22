package de.eternalwings.bukkit.sync

import java.util.function.Consumer

interface QueueManager {
    fun hasQueue(name: String): Boolean

    fun createQueue(name: String, maxSize: Int) : SynchronizedQueue

    fun createQueue(name: String, maxSize: Int, fullCallback: Consumer<List<String>>): SynchronizedQueue
}
