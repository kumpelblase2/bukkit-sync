package de.eternalwings.bukkit.sync.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.plugin.Plugin

open class StorageKeyUpdated<T>(val path: String, val context: String, val plugin: Plugin, val oldValue: T?, val newValue: T?) :
        Event() {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
