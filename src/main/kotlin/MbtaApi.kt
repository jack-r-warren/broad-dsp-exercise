import io.ktor.client.HttpClient
import io.ktor.client.request.get

/**
 * This class represent a logical wrapper over the API for the MBTA. It can be supplied with an HttpClient so that
 * the API's behavior may be more easily mocked.
 */
class MbtaApi(private val client: HttpClient) {
    private val apiTarget = "https://api-v3.mbta.com"

    /**
     * This contacts the routes API documented here:
     * https://api-v3.mbta.com/docs/swagger/index.html#/Route/ApiWeb_RouteController_index
     *
     * This method isn't opinionated about how the [types] are filtered; that decision is left to the caller. This
     * method will properly request whatever types are given. The integers map to the values given in the API exactly,
     * to stay in-line with Google's spec that the MBTA is following:
     * https://developers.google.com/transit/gtfs/reference#routestxt
     *
     * This method uses Kotlin type inference to know that the result of `client.get` must be a `Routes`, so it
     * attempts to use the client's JsonFeature to deserialize the response if the Content-Type is acceptable.
     */
    suspend fun getRoutes(vararg types: Int): Routes {
        var url = "$apiTarget/routes"
        if (types.isNotEmpty())
            url += "?filter[type]=${types.joinToString(separator = ",")}"
        return client.get(url)
    }

    /**
     * This contacts the stops API documented here:
     * https://api-v3.mbta.com/docs/swagger/index.html#/Stop/ApiWeb_StopController_index
     *
     * Similar to [getRoutes], this method isn't opinionated about how the [routes] are filtered; that decision is
     * left to the caller.
     *
     * Similar to [geRoutes], this method uses Kotlin type inference to convert the JSON response to a Stops object.
     */
    suspend fun getStops(vararg routes: String): Stops {
        var url = "$apiTarget/stops"
        if (routes.isNotEmpty())
            url += "?filter[route]=${routes.joinToString(separator = ",")}"
        return client.get(url)
    }

    /**
     * This method returns a pair of routes and stops, each with their respective mutable fields set up correctly.
     * Specifically, for each RouteData it sets the stops field to the correct StopData objects, and for each
     * StopData it sets the routes field to the correctRouteData objects.
     *
     * The [routeTypes] it accepts are the exact same as passed to [getRoutes].
     *
     * The basic algorithm for doing this is to iterate over all routes, requesting the stops of each and storing the
     * stops such that any stops we've previously received aren't overwritten. This allows us to return only one
     * instance of each stop and route, with each having correct references to the singular instances of the other.
     */
    suspend fun associateRoutesToStops(vararg routeTypes: Int): Pair<Set<Routes.RouteData>, Set<Stops.StopData>> {
        val routes: Set<Routes.RouteData> = getRoutes(*routeTypes).data.toSet()
        // We'll keep track of stops by their IDs so we can accumulate all stops but not overwrite any of our
        // associations
        val stops: MutableMap<String, Stops.StopData> = mutableMapOf()
        for (route in routes) {
            getStops(route.id).data
                .map { newStop ->
                    // Use putIfAbsent (which returns null if it modifies `stops`) and the elvis operator to make sure
                    // that whatever we pass along to the forEach below is the de-duped stop that is currently in the
                    // `stops` map
                    // In other words, we pass along any matching stop we've stored, falling back to the newStop if it
                    // is actually new
                    stops.putIfAbsent(newStop.id, newStop) ?: newStop
                }
                .forEach { stop ->
                    stop.routes = (stop.routes ?: mutableSetOf()).apply { add(route) }
                    route.stops = (route.stops ?: mutableSetOf()).apply { add(stop) }
                }
        }
        return routes to stops.values.toSet()
    }
}

