package de.eternalwings.bukkit.sync.impl

import com.google.gson.Gson
import de.eternalwings.bukkit.sync.SynchronizedConfig
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api.CuratorEventType.EXISTS
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.curator.framework.recipes.cache.NodeCacheListener
import org.slf4j.LoggerFactory

class SyncedConfigurationKey<T : Any>(private val curatorFramework: CuratorFramework, private val zookeeperPathString: String,
                                      private val defaultValue: T, private val callback: (T?) -> Unit) {

    private val logger = LoggerFactory.getLogger(SynchronizedConfig::class.java)
    private val nodeCache = NodeCache(curatorFramework, zookeeperPathString)
    private val gson = Gson()
    private val type = defaultValue.javaClass

    init {
        curatorFramework.checkExists().creatingParentsIfNeeded().inBackground { _, event ->
            if (event.type == EXISTS) {
                if (event.resultCode != 0) {
                    logger.info("Creating configuration at $zookeeperPathString because it didn't exist.")
                    curatorFramework.create().orSetData().inBackground().forPath(zookeeperPathString, deserialize(defaultValue))
                } else {
                    callback(serialize(curatorFramework.data.forPath(zookeeperPathString)))
                }
            }
        }.forPath(zookeeperPathString)

        nodeCache.listenable.addListener(NodeCacheListener {
            val data = nodeCache.currentData.data
            val newValue = serialize(data)
            callback(newValue)
        })

        nodeCache.start(true)
    }

    fun synchronizeValue(value: T?) {
        curatorFramework.setData().inBackground().forPath(zookeeperPathString, deserialize(value))
    }

    fun getCurrentValue(): T? {
        return serialize(nodeCache.currentData.data)
    }

    private fun serialize(data: ByteArray): T? {
        return gson.fromJson(String(data), type)
    }

    private fun deserialize(value: T?): ByteArray {
        return gson.toJson(value).toByteArray()
    }
}
