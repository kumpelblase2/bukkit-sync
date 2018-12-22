package de.eternalwings.bukkit.sync.zookeeper

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCache
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener
import org.apache.zookeeper.CreateMode
import java.util.function.Consumer

class LimitStorageQueue(private val curatorFramework: CuratorFramework, val maxSize: Int, val path: String) {

    private val cache: PathChildrenCache
    var callback: Consumer<List<String>> = Consumer {}

    init {
        curatorFramework.create().creatingParentsIfNeeded().forPath(path)

        cache = PathChildrenCache(curatorFramework, path, false)
        cache.listenable.addListener(PathChildrenCacheListener { _, event ->
            when (event.type) {
                Type.CHILD_ADDED -> {
                    if (cache.currentData.size >= maxSize) {
                        onFull()
                    }
                }
            }
        })

        cache.start()
    }

    private fun onFull() {
        val childrenData = cache.currentData.map {
            val bytes = curatorFramework.data.forPath(it.path)
            val data = String(bytes)
            curatorFramework.delete().inBackground().forPath(it.path)
            return@map data
        }
        callback.accept(childrenData)
    }

    fun addElement(data: String) {
        curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(getPathFor(data), data.toByteArray())
    }

    fun removeElement(data: String) {
        curatorFramework.delete().forPath(getPathFor(data))
    }

    private fun getPathFor(data: String): String {
        return "$path/${data.hashCode()}"
    }

    companion object {
        fun hasQueueWithName(rootPath: String, name: String, curatorFramework: CuratorFramework): Boolean {
            return curatorFramework.checkExists().forPath("$rootPath/$name") != null
        }
    }
}
