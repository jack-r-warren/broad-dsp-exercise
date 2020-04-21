import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * These tests contact the API server to use behavior that would be more error-prone to mock (specifically, the
 * filtering of stops by routes).
 */
object OnlineMbtaApiTest {

    /**
     * This test ensures the output of the associations method available on the api wrapper is properly formatted.
     * Specifically, this checks that every "route" given on a stop is indeed a route we were told about, and likewise
     * for "stops" given on routes.
     */
    @Test
    fun testAssociations(): Unit = runBlocking {
        // We get the http client used during normal execution directly
        val (routes, stops) = MbtaApi(httpClient).associateRoutesToStops(0, 1)

        for (route in routes)
            route.stops!!.forEach { assertTrue(it in stops) }
        for (stop in stops)
            stop.routes!!.forEach { assertTrue(it in routes) }
    }

    /**
     * This test goes ahead and does a full end-to-end test of the path searching for question 3 by capturing the
     * console output. It checks the davis-to-kendall and ashmont-to-arlington routes given in the prompt document.
     */
    @Test
    fun testSearching(): Unit = runBlocking {
        ByteArrayOutputStream().let { output ->
            System.setOut(PrintStream(output))
            QuestionThree(httpClient).parse(listOf("--source", "davis", "--dest", "kendall"))
            assertEquals(
                listOf(
                    "kendall not found, approximating to Kendall/MIT",
                    "Path from davis to kendall: Red Line"
                ),
                output.toString().lines().filter { it.isNotEmpty() }
            )
        }
        ByteArrayOutputStream().let { output ->
            System.setOut(PrintStream(output))
            QuestionThree(httpClient).main(listOf("--source", "ashmont", "--dest", "arlington"))
            assertEquals(
                listOf(
                    "Path from ashmont to arlington: Red Line, Green Line B"
                ),
                output.toString().lines().filter { it.isNotEmpty() }
            )
        }
    }
}