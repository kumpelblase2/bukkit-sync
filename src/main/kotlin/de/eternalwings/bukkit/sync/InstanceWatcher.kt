package de.eternalwings.bukkit.sync

import com.google.gson.Gson
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCache
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode.BUILD_INITIAL_CACHE
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CHILD_ADDED
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CHILD_REMOVED
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CHILD_UPDATED
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener
import org.apache.curator.framework.recipes.nodes.GroupMember
import java.util.function.Consumer

class InstanceWatcher(private val zookeeper: CuratorFramework, val thisInstance: InstanceData) {

    private val membership: GroupMember =
            GroupMember(zookeeper, SyncPlugin.INSTANCES_PATH, thisInstance.name, serialize(thisInstance))
    // Even though the membership uses the same mechanism, we can't actually create our own listeners on it
    // So instead we have to duplicate this...
    private val pathChildrenCache = PathChildrenCache(zookeeper, SyncPlugin.INSTANCES_PATH, true)
    private var onInstanceFind: List<Consumer<InstanceData>> = emptyList()
    private var onInstanceLost: List<Consumer<InstanceData>> = emptyList()
    private var onInstanceUpdated: List<Consumer<InstanceData>> = emptyList()

    val allInstances: List<InstanceData>
        get() {
            val instances = membership.currentMembers.values
            return instances.map { deserialize(it) }
        }

    fun start() {
        membership.start()
        pathChildrenCache.start(BUILD_INITIAL_CACHE)
        pathChildrenCache.listenable.addListener(PathChildrenCacheListener { client, event ->
            when (event.type) {
                CHILD_ADDED -> runInstanceChange(onInstanceFind, event.data.data)
                CHILD_REMOVED -> runInstanceChange(onInstanceLost, event.data.data)
                CHILD_UPDATED -> runInstanceChange(onInstanceUpdated, event.data.data)
                else -> {
                }
            }
        })
    }

    fun update() {
        membership.setThisData(serialize(thisInstance))
    }

    fun onInstanceFound(consumer: Consumer<InstanceData>) {
        onInstanceFind += consumer
    }

    fun onInstanceLost(consumer: Consumer<InstanceData>) {
        onInstanceLost += consumer
    }

    fun onInstanceUpdated(consumer: Consumer<InstanceData>) {
        onInstanceUpdated += consumer
    }

    private fun runInstanceChange(consumers: List<Consumer<InstanceData>>, byteData: ByteArray) {
        val instanceData = deserialize(byteData)
        if (instanceData.name != thisInstance.name) {
            consumers.forEach { it.accept(instanceData) }
        }
    }

    operator fun get(name: String): InstanceData? {
        val data = membership.currentMembers[name] ?: return null
        return deserialize(data)
    }

    companion object {
        private val gson = Gson()

        fun serialize(instanceData: InstanceData): ByteArray {
            return gson.toJson(instanceData).toByteArray()
        }

        fun deserialize(data: ByteArray): InstanceData {
            return gson.fromJson(String(data), InstanceData::class.java)
        }
    }
}
