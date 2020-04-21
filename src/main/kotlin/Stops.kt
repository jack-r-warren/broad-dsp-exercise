// Simple data class enabling deserialization of data from the MBTA's "/stops" endpoint
data class Stops(
    val data: List<StopData>
) {
    data class StopData(
        val type: String,
        val id: String,
        val attributes: Attributes
    ) {
        // [routes] exists to keep track of individual routes this stop exists on. The Set is similar to that seen
        // above, in that it makes use of the automatic hashcode for Route that excludes this mutable field.
        var routes: MutableSet<Routes.RouteData>? = null

        data class Attributes(
            val name: String
        )
    }
}