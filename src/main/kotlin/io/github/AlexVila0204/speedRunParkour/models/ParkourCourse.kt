
package io.github.AlexVila0204.speedRunParkour.models

import org.bukkit.Location

data class ParkourCourse(
    val id: String,
    val difficulty: String,
    val startLocation: Location,
    val endLocation: Location,
    val spawnLocation: Location,
    val waitingArea: Location
) {
    fun isStartPlate(location: Location): Boolean {
        return isSameBlock(startLocation, location)
    }

    fun isEndPlate(location: Location): Boolean {
        return isSameBlock(endLocation, location)
    }

    private fun isSameBlock(loc1: Location, loc2: Location): Boolean {
        return loc1.world == loc2.world &&
                loc1.blockX == loc2.blockX &&
                loc1.blockY == loc2.blockY &&
                loc1.blockZ == loc2.blockZ
    }
}