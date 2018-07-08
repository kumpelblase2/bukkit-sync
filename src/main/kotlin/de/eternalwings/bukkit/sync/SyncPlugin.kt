package de.eternalwings.bukkit.sync

import de.eternalwings.bukkit.sync.impl.SyncServiceImpl
import org.apache.curator.RetryPolicy
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.api.UnhandledErrorListener
import org.apache.curator.framework.state.ConnectionState.CONNECTED
import org.apache.curator.framework.state.ConnectionState.LOST
import org.apache.curator.framework.state.ConnectionStateListener
import org.apache.curator.retry.ExponentialBackoffRetry
import org.bukkit.plugin.ServicePriority.Normal
import org.bukkit.plugin.java.JavaPlugin

class SyncPlugin : JavaPlugin() {
    private val zookeeperBuilder = CuratorFrameworkFactory.builder().retryPolicy(DEFAULT_RETRY_POLICY)
    private lateinit var zookeeper: CuratorFramework
    private lateinit var serviceImpl: SyncServiceImpl

    override fun onEnable() {
        logger.info { "Connecting to zookeeper..." }
        config.addDefault(ZOOKEEPER_CONFIG_KEY, "127.0.0.1:2181")
        config.addDefault(NAMESPACE_CONFIG_KEY, NAMESPACE)
        val zookeeperConnection = config.getString(ZOOKEEPER_CONFIG_KEY)
        val namespace = config.getString(NAMESPACE_CONFIG_KEY)
        zookeeper = createZookeeperClient(zookeeperConnection, namespace)

        initializeService()
    }

    private fun createZookeeperClient(zookeeperConnection: String?, namespace: String = NAMESPACE): CuratorFramework {
        val builder = zookeeperBuilder.namespace(namespace).connectString(zookeeperConnection)
        val client = builder.build()
        client.unhandledErrorListenable.addListener(UnhandledErrorListener { message, e ->
            logger.severe(message)
            e.printStackTrace()
        })
        client.connectionStateListenable.addListener(ConnectionStateListener { client, newState ->
            if (newState == CONNECTED) {
                logger.info { "Connected to zookeeper." }
            } else if (newState == LOST) {
                logger.warning { "Lost connection to zookeeper." }
            }
        })
        client.start()
        return client
    }

    private fun initializeService() {
        serviceImpl = SyncServiceImpl(zookeeper)
        server.servicesManager.register(SyncService::class.java, serviceImpl, this, Normal)
    }

    companion object {
        const val NAMESPACE = "minecraft"
        const val NAMESPACE_CONFIG_KEY = "namespace"
        const val ZOOKEEPER_CONFIG_KEY = "zookeeper"

        var DEFAULT_RETRY_POLICY: RetryPolicy = ExponentialBackoffRetry(1000, 3)
    }
}
