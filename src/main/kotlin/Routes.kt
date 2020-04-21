import com.google.gson.annotations.SerializedName
// Simple data class enabling deserialization of data from the MBTA's "/routes" endpoint
data class Routes(
    val data: List<RouteData>
) {
    data class RouteData(
        val type: String,
        val id: String,
        val attributes: Attributes
    ) {
        // [stops] exists to keep track of individual stops on this route. The Set makes use of the automatic
        // hashcode generated for `data class` instances, which includes only the fields in the constructor. This means
        // that a hashcode of this class would specifically exclude this mutable set.
        var stops: MutableSet<Stops.StopData>? = null

        data class Attributes(
            val type: Int,
            // SerializedName used to get around the API's naming scheme
            @SerializedName("short_name") val shortName: String,
            @SerializedName("long_name") val longName: String,
            @SerializedName("direction_names") val directionNames: List<String>,
            @SerializedName("direction_destinations") val directionDestinations: List<String>,
            val description: String
        )
    }
}