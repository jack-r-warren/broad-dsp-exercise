import com.github.ajalt.clikt.core.CliktCommand
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking

/**
 * Class handling question one, printing out all subway routes in Boston
 */
class QuestionOne(httpClient: HttpClient) : CliktCommand(
    name = "q1",
    help = "Perform the task for question 1: print out the long names of each subway line"
) {
    private val api = MbtaApi(httpClient)
    override fun run(): Unit = runBlocking {
        /*
        Types are filtered directly at the API-level here to reduce the data processing required locally. An alternative
        would be to request all routes and filter the response, but that would send more data over the wire and require
        filtering here.
         */
        echo(api.getRoutes(0, 1).data.joinToString { it.attributes.longName })
    }
}