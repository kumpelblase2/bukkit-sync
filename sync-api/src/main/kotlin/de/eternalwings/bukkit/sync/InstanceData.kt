package de.eternalwings.bukkit.sync

/**
 * Represents data in the store for this instance.
 */
data class InstanceData(
        /**
         * The name of this instance.
         */
        var name: String,
        /**
         * The host configured in the configuration for this instance.
         */
        var host: String,
        /**
         * List of enabled plugin names of the instance.
         */
        var plugins: List<String> = emptyList(),
        /**
         * The current amount of players on the instace.
         */
        var playerCount: Int = 0,
        /**
         * The maximum amount of allowed players on the instance.
         */
        var maxPlayers: Int = 1
)
