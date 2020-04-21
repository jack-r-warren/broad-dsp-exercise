import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) =
    HttpClient(CIO) {
        install(JsonFeature) {
            // The API insists on sending "application/vnd.api+json", so we compensate by having the JsonFeature
            // intercept those responses in addition to its default of "application/json"
            acceptContentTypes = acceptContentTypes + ContentType.parse("application/vnd.api+json")
        }
    }.let { client -> CommandRunner.subcommands(
        QuestionOne(client)
    ).main(args) }

object CommandRunner : CliktCommand(
    // Supplying the jar name like this makes the help output a bit more readable
    name = "java -jar \"Broad DSP Exercise.jar\"",
    help = "An executable answering individual questions from the Broad Institute coding challenge: " +
            "Supply one of the below commands to see the associated output"
) {
    override fun run() = Unit
}

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