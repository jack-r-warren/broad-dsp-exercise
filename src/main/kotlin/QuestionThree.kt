import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * Class handling the third question, finding a path between two given stops
 */
class QuestionThree(httpClient: HttpClient) : CliktCommand(
    name = "q3",
    help = "Perform the task for question 3: given a source stop and a destination stop, determine what lines to get" +
            "from the source to the destination"
) {
    private val source by option(help = "The name of the stop to start from (case insensitive)").required()
    private val dest by option(help = "The name of the stop to end at (case insensitive)").required()
    private val api = MbtaApi(httpClient)

    /**
     * Short function to help find stops, attempting to approximate if a shorter name is used (like Kendall vs
     * Kendall/MIT)
     */
    private fun findStop(stops: Collection<Stops.StopData>, name: String): Stops.StopData? {
        val nameLowercase = name.toLowerCase()
        return stops.firstOrNull { it.attributes.name.toLowerCase() == nameLowercase }
            ?: stops.firstOrNull { it.attributes.name.toLowerCase().contains(nameLowercase) }?.also {
                echo("$name not found, approximating to ${it.attributes.name}")
            }
    }

    override fun run(): Unit = runBlocking {
        val (routes, stops) = api.associateRoutesToStops(0, 1)

        val sourceStop: Stops.StopData = findStop(stops, source)
                ?: throw IllegalArgumentException("Source $source not found as a stop name!")

        val acceptableDestRoutes: Set<Routes.RouteData> = findStop(stops, dest)?.routes
                ?: throw IllegalArgumentException("Destination $dest not found as a stop name!")

        // Make an adjacency list of each route to the routes it can reach
        val adjacencyList: Map<Routes.RouteData, Set<Routes.RouteData>> =
            routes.associateWith { route ->
                route.stops?.fold(mutableSetOf<Routes.RouteData>()) { setSoFar, stop ->
                    // For each stop on this route, add any of the routes it is on to our accumulator
                    stop.routes?.forEach { setSoFar.add(it) }
                    setSoFar
                }?.apply {
                    // At the end, remove this route from the accumulator
                    remove(route)
                } ?: mutableSetOf()
            }

        // Use a simple breadth-first search to find a path from source to dest, if one exists
        val queue: Queue<List<Routes.RouteData>> =
            LinkedList()
        val visited = sourceStop.routes ?: mutableSetOf()
        sourceStop.routes?.map { listOf(it) }?.let { queue.addAll(it) }

        while (queue.isNotEmpty()) queue.remove().let { currentPath ->
            currentPath.lastOrNull()?.let { currentLastRoute ->
                if (currentLastRoute in acceptableDestRoutes) {
                    // If we're ending at an acceptable end route now, print and exit
                    echo("Path from $source to $dest: ${currentPath.joinToString { it.attributes.longName }}")
                    return@runBlocking
                } else {
                    // If we need to keep going, add the current route to what we've visited
                    visited.add(currentLastRoute)
                    // ... and add everything this route connects to that we haven't seen to the queue of things
                    // to visit
                    adjacencyList[currentLastRoute]?.forEach {
                        if (it !in visited) queue.add(buildList {
                            addAll(currentPath)
                            add(it)
                        })
                    }
                }
            }
        }

        echo("No path from $source to $dest could be found")
    }
}