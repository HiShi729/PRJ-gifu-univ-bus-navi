package com.example.prj_gifu_univ_bus_navi.model

import kotlinx.serialization.Serializable

@Serializable
data class UserGraphNodeInput(
    val name: String,
    val connectedNodeId: String,
    val minutesToConnectedNode: Int,
    val isSelectableAsStart: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val coordinateSource: UserNodeCoordinateSource = UserNodeCoordinateSource.NONE,
)

@Serializable
data class UserEdgeOverride(
    val baseEdgeId: String,
    val minutes: Int,
)

@Serializable
data class UserTravelTimeProfile(
    val calibrationEdgeId: String,
    val standardMinutes: Int,
    val measuredMinutes: Int,
    val timeScaleFactor: Double,
)
