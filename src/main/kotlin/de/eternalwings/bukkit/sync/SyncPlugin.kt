package de.eternalwings.bukkit.sync

import de.eternalwings.bukkit.sync.impl.SyncServiceImpl
import org.apache.curator.RetryPolicy
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.api.UnhandledErrorListener
import org.apache.curator.retry.ExponentialBackoffRetry
import org.bukkit.plugin.ServicePriority.Normal
import org.bukkit.plugin.java.JavaPlugin

class SyncPlugin : JavaPlugin() {
    private val zookeeperBuilder = CuratorFrameworkFactory.builder().namespace(NAMESPACE).retryPolicy(DEFAULT_RETRY_POLICY)
    lateinit var zookeeper: CuratorFramework
    lateinit var serviceImpl: SyncServiceImpl

    override fun onEnable() {
        logger.info("Connecting to zookeeper...")
        config.addDefault("zookeeper.ip", "127.0.0.1:2181")
        val zookeeperConnection = this.config.getString("zookeeper.ip")

        val builder = zookeeperBuilder.connectString(zookeeperConnection)
        val client = builder.build()
        client.start()
        client.unhandledErrorListenable.addListener(UnhandledErrorListener { message, e -> println(message); println(e) })
        zookeeper = client
        logger.info("Connection to zookeeper established.")

        initializeService()
    }

    private fun initializeService() {
        serviceImpl = SyncServiceImpl(zookeeper)
        server.servicesManager.register(SyncService::class.java, serviceImpl, this, Normal)
    }

    companion object {
        const val NAMESPACE = "mcsync"

        var DEFAULT_RETRY_POLICY: RetryPolicy = ExponentialBackoffRetry(1000, 3)
    }
}
