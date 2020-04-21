import com.github.ajalt.clikt.core.CliktCommand
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking

/**
 * Class handling the second question, printing routes with the most and least stops and all stops on multiple routes
 */
class QuestionTwo(httpClient: HttpClient) : CliktCommand(
    name = "q2",
    help = "Perform the task for question 2: print out the routes with the most and least stops and stops on " +
            "multiple routes"
) {
    private val api = MbtaApi(httpClient)
    override fun run(): Unit = runBlocking {
        val (routes, stops) = api.associateRoutesToStops(0, 1)

        /*
        To get around the a route's stops and a stop's routes being null, I'm making generous use of Kotlin's null safe
        operators, ?. and ?:

        More information is available here: https://kotlinlang.org/docs/reference/null-safety.html

        Why not just set the field to an empty set to begin with to have it never be nullable?
        The JSON parser recommended for Ktor (the networking library), Gson, is written in Java. It doesn't respect
        Kotlin's null safety or understand its property/field syntax, and it'll set the field to null in Java even if
        we set it to something else in Kotlin. Rather than arguing with the Java library I give in and call the field
        nullable.
         */

        routes.maxBy { route -> route.stops?.size ?: 0 }?.let { route ->
            echo("Route with the most stops: ${route.attributes.longName} (${route.stops?.size ?: 0})")
        } ?: echo("There were no routes found; maximum stops couldn't be calculated")
        routes.minBy { route -> route.stops?.size ?: 0 }?.let { route ->
            echo("Route with the least stops: ${route.attributes.longName} (${route.stops?.size ?: 0})")
        } ?: echo("There were no routes found; minimum stops couldn't be calculated")

        echo("Stops on at least two routes:")
        stops.filter { stop -> stop.routes != null && stop.routes!!.size > 1 }.forEach { stop ->
            echo("    ${stop.attributes.name}: ${stop.routes!!.joinToString { it.attributes.longName }}")
        }
    }
}