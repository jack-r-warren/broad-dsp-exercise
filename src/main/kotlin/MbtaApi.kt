import com.google.gson.annotations.SerializedName
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class MbtaApi(private val client: HttpClient) {
    private val apiTarget = "https://api-v3.mbta.com"

    /**
     * This contacts the routes API documented here:
     * https://api-v3.mbta.com/docs/swagger/index.html#/Route/ApiWeb_RouteController_index
     *
     * This method isn't opinionated about how the [types] are filtered; that decision is left to the caller. This
     * method will properly request whatever types are given. The integers map to the values given in the API exactly.
     */
    suspend fun getRoutes(vararg types: Int): Routes {
        var url = "$apiTarget/routes"
        if (types.isNotEmpty())
            url += "?filter[type]=${types.joinToString(separator = ",")}"
        return client.get(url)
    }
}

data class Routes(
    val data: List<RouteData>
) {
    data class RouteData(
        val type: String,
        val id: String,
        val attributes: Attributes
    ) {
        data class Attributes(
            val type: Int,
            @SerializedName("short_name") val shortName: String,
            @SerializedName("long_name") val longName: String,
            @SerializedName("direction_names") val directionNames: List<String>,
            @SerializedName("direction_destinations") val directionDestinations: List<String>,
            val description: String
        )
    }
}
