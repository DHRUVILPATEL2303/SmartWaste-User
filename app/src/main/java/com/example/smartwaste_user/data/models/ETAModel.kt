package com.example.smartwaste_user.data.models

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<Leg>
)

data class Leg(
    val duration: Duration,
    val distance: Distance
)

data class Duration(
    val text: String,
    val value: Int // duration in seconds
)

data class Distance(
    val text: String,
    val value: Int // distance in meters
)