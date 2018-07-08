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
import java.util.function.Consumer

class SyncPlugin : JavaPlugin() {
    private val zookeeperBuilder = CuratorFrameworkFactory.builder().retryPolicy(DEFAULT_RETRY_POLICY)
    private lateinit var zookeeper: CuratorFramework
    private lateinit var serviceImpl: SyncServiceImpl
    private lateinit var instanceWatcher: InstanceWatcher

    private val shouldAnnounceInstance: Boolean
        get() = config.getBoolean(INSTANCE_ANNOUNCE_KEY)

    private val instanceData: InstanceData
        get() {
            val pluginNames = server.pluginManager.plugins.map { it.name }
            val instanceName = config.getString(INSTANCE_NAME_CONFIG_KEY)
            val instanceHost = config.getString(INSTANCE_HOST_CONFIG_KEY)
            return InstanceData(instanceName, pluginNames, instanceHost)
        }

    override fun onEnable() {
        logger.info { "Connecting to zookeeper..." }
        config.addDefault(ZOOKEEPER_CONFIG_KEY, "127.0.0.1:2181")
        config.addDefault(NAMESPACE_CONFIG_KEY, NAMESPACE)
        config.addDefault(INSTANCE_ANNOUNCE_KEY, false)
        config.addDefault(INSTANCE_NAME_CONFIG_KEY, "Default Server")
        config.addDefault(INSTANCE_HOST_CONFIG_KEY, "localhost:25565")
        val zookeeperConnection = config.getString(ZOOKEEPER_CONFIG_KEY)
        val namespace = config.getString(NAMESPACE_CONFIG_KEY)
        zookeeper = createZookeeperClient(zookeeperConnection, namespace)

        if (shouldAnnounceInstance) {
            instanceWatcher = InstanceWatcher(zookeeper, instanceData).apply {
                onInstanceFound(Consumer { instance ->
                    logger.info { "Found server instance ${instance.name}." }
                    logger.fine { "Instance information: $instance" }
                })
                onInstanceLost(Consumer { instance ->
                    logger.info { "Lost server instance ${instance.name}." }
                })
                onInstanceUpdated(Consumer { instance ->
                    logger.fine { "Instance ${instance.name} was reconfigured. Information: $instance" }
                })
                start()
            }
        }
        initializeService()
    }

    private fun createZookeeperClient(zookeeperConnection: String?, namespace: String = NAMESPACE): CuratorFramework {
        val builder = zookeeperBuilder.namespace(namespace).connectString(zookeeperConnection)
        val client = builder.build()
        client.unhandledErrorListenable.addListener(UnhandledErrorListener { message, e ->
            logger.severe(message)
            e.printStackTrace()
        })
        client.connectionStateListenable.addListener(ConnectionStateListener { _, newState ->
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
        serviceImpl = SyncServiceImpl(zookeeper, instanceWatcher)
        server.servicesManager.register(SyncService::class.java, serviceImpl, this, Normal)
    }

    companion object {
        const val NAMESPACE = "minecraft"
        const val NAMESPACE_CONFIG_KEY = "namespace"
        const val ZOOKEEPER_CONFIG_KEY = "zookeeper"
        const val INSTANCE_NAME_CONFIG_KEY = "instance"
        const val INSTANCE_HOST_CONFIG_KEY = "host"
        const val INSTANCE_ANNOUNCE_KEY = "announce_instance"
        const val INSTANCES_PATH = "/instances"

        var DEFAULT_RETRY_POLICY: RetryPolicy = ExponentialBackoffRetry(1000, 3)
    }
}
