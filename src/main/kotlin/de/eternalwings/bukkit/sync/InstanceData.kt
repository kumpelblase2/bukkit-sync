package de.eternalwings.bukkit.sync

data class InstanceData(var name: String, var plugins: List<String> = emptyList(), var host: String)
