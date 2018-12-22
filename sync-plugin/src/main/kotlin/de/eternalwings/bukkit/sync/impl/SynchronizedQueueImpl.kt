package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.SynchronizedQueue
import de.eternalwings.bukkit.sync.zookeeper.LimitStorageQueue
import org.apache.curator.framework.CuratorFramework
import java.util.function.Consumer

class SynchronizedQueueImpl(private val path: String, override val name: String, override val maxSize: Int,
                            private val curatorFramework: CuratorFramework,
                            private val fullCallback: Consumer<List<String>>? = null) : SynchronizedQueue {

    private val queue: LimitStorageQueue = LimitStorageQueue(curatorFramework, maxSize, "/$path/queues/$name")

    init {
        if (fullCallback != null) {
            queue.callback = fullCallback
        }
    }

    override fun addToQueue(element: String) {
        queue.addElement(element)
    }

    override fun removeFromQueue(element: String) {
        queue.removeElement(element)
    }

}

