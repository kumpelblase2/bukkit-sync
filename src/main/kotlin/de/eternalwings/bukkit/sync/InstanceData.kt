package de.eternalwings.bukkit.sync

data class InstanceData(var name: String, var host: String, var plugins: List<String> = emptyList(), var playerCount: Int = 0,
                        var maxPlayers: Int = 1)
