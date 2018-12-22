package de.eternalwings.bukkit.sync.impl

import de.eternalwings.bukkit.sync.QueueManager
import de.eternalwings.bukkit.sync.SynchronizedQueue
import de.eternalwings.bukkit.sync.zookeeper.LimitStorageQueue
import org.apache.curator.framework.CuratorFramework
import org.bukkit.plugin.Plugin
import java.util.function.Consumer

class QueueManagerImpl(private val plugin: Plugin, private val curatorFramework: CuratorFramework) : QueueManager {
    override fun hasQueue(name: String): Boolean {
        return LimitStorageQueue.hasQueueWithName(plugin.name, name, curatorFramework)
    }

    override fun createQueue(name: String, maxSize: Int): SynchronizedQueue =
            SynchronizedQueueImpl(plugin.name, name, maxSize, curatorFramework)

    override fun createQueue(name: String, maxSize: Int, fullCallback: Consumer<List<String>>): SynchronizedQueue =
            SynchronizedQueueImpl(plugin.name, name, maxSize, curatorFramework, fullCallback)

}
