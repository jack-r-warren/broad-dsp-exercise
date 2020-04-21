import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType

// This main method is just placed at the top-level, rather than in a dummy wrapper class (Kotlin's compiler does
// that for us). It just redirects to the [CommandRunner] further down in this file
fun main(args: Array<String>) = CommandRunner.subcommands(
    QuestionOne(httpClient),
    QuestionTwo(httpClient),
    QuestionThree(httpClient)
).main(args)

// We abstract this out to a raw top-level value so we can get to it for testing
val httpClient = HttpClient(CIO) {
    install(JsonFeature) {
        // The API insists on sending "application/vnd.api+json", so we compensate by having the JsonFeature
        // intercept those responses in addition to its default of "application/json"
        acceptContentTypes = acceptContentTypes + ContentType.parse("application/vnd.api+json")
    }
}


// A top-level command runner that serves simply to call one of the question-specific command line parsers
// (This is handled automatically by Clikt so we just do nothing here)
object CommandRunner : CliktCommand(
    // Supplying the jar name like this makes the help output a bit more readable
    name = "java -jar \"Broad DSP Exercise.jar\"",
    help = "An executable answering individual questions from the Broad Institute coding challenge: " +
            "Supply one of the below commands to see the associated output or pass -h to a command to see help"
) {
    // This object just delegates, so we don't need it to do anything upon being called
    override fun run() = Unit
}

