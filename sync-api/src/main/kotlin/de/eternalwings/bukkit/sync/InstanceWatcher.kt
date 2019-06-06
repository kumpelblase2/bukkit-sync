package de.eternalwings.bukkit.sync

import java.util.function.Consumer

interface InstanceWatcher {
    /**
     * Returns all instances that are currently available.
     */
    val allInstances: List<InstanceData>

    /**
     * Starts this instance watcher to both register itself in the store as well
     * as listen for events from new/removed instances.
     */
    fun start()

    /**
     * Updates the data related to this instance in the store, such as player count.
     */
    fun update()

    /**
     * Registers a callback to be run when a new instance was registered.
     */
    fun onInstanceFound(consumer: Consumer<InstanceData>)

    /**
     * Registers a callback to be run when an instance was lost/got removed.
     */
    fun onInstanceLost(consumer: Consumer<InstanceData>)

    /**
     * Registers a callback to be run when the data for an existing instance
     * was updated.
     */
    fun onInstanceUpdated(consumer: Consumer<InstanceData>)

    /**
     * Returns the data related to the instance with the given name.
     */
    operator fun get(name: String): InstanceData?

}
