package de.eternalwings.bukkit.sync

import java.util.function.Consumer

interface InstanceWatcher {
    val allInstances: List<InstanceData>

    fun start()

    fun update()

    fun onInstanceFound(consumer: Consumer<InstanceData>)

    fun onInstanceLost(consumer: Consumer<InstanceData>)

    fun onInstanceUpdated(consumer: Consumer<InstanceData>)

    operator fun get(name: String): InstanceData?

}
