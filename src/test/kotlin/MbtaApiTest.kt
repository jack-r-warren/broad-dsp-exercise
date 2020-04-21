import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

object MbtaApiTest {

    /**
     * Use a mock client to respond to `/routes` queries with a snippet from a manual query made with the online
     * documentation at https://api-v3.mbta.com/docs/swagger/index.html#/Route/ApiWeb_RouteController_index
     *
     * This tests that deserialization works as expected without needlessly hitting the API endpoint.
     */
    @Test
    fun testGetRoutes(): Unit = runBlocking {
        val mockClient = HttpClient(MockEngine) {
            install(JsonFeature)
            engine {
                addHandler { request ->
                    if (request.url.fullPath.startsWith("/routes"))
                        respond(
                            """
{
  "data": [
    {
      "attributes": {
        "color": "DA291C",
        "description": "Rapid Transit",
        "direction_destinations": [
          "Ashmont/Braintree",
          "Alewife"
        ],
        "direction_names": [
          "South",
          "North"
        ],
        "fare_class": "Rapid Transit",
        "long_name": "Red Line",
        "short_name": "",
        "sort_order": 10010,
        "text_color": "FFFFFF",
        "type": 1
      },
      "id": "Red",
      "links": {
        "self": "/routes/Red"
      },
      "relationships": {
        "line": {
          "data": {
            "id": "line-Red",
            "type": "line"
          }
        },
        "route_patterns": {}
      },
      "type": "route"
    }
  ]
}
                        """.trimIndent(),
                            HttpStatusCode.OK,
                            headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        )
                    else respondBadRequest()
                }
            }
        }

        assertEquals(
            Routes(
                listOf(
                    Routes.RouteData(
                        type = "route",
                        id = "Red",
                        attributes = Routes.RouteData.Attributes(
                            type = 1,
                            shortName = "",
                            longName = "Red Line",
                            directionNames = listOf("South", "North"),
                            directionDestinations = listOf("Ashmont/Braintree", "Alewife"),
                            description = "Rapid Transit"
                        )
                    )
                )
            ),
            // passing the type parameter to the mock is useless but helps ensure we don't face errors forming it
            MbtaApi(mockClient).getRoutes(1)
        )
    }

    /**
     * Similar to the above test, this mocks a "/stops" api request with a snippet from a manual run of the API from
     * the online documentation: https://api-v3.mbta.com/docs/swagger/index.html#/Stop/ApiWeb_StopController_index
     */
    @Test
    fun testGetStops(): Unit = runBlocking {
        val mockClient = HttpClient(MockEngine) {
            install(JsonFeature)
            engine {
                addHandler { request ->
                    if (request.url.fullPath.startsWith("/stops"))
                        respond(
                            """
{
  "data": [
    {
      "attributes": {
        "address": "Alewife Brook Pkwy and Cambridge Park Dr, Cambridge, MA 02140",
        "at_street": null,
        "description": null,
        "latitude": 42.395428,
        "location_type": 1,
        "longitude": -71.142483,
        "municipality": "Cambridge",
        "name": "Alewife",
        "on_street": null,
        "platform_code": null,
        "platform_name": null,
        "vehicle_type": null,
        "wheelchair_boarding": 1
      },
      "id": "place-alfcl",
      "links": {
        "self": "/stops/place-alfcl"
      },
      "relationships": {
        "child_stops": {},
        "facilities": {
          "links": {
            "related": "/facilities/?filter[stop]=place-alfcl"
          }
        },
        "parent_station": {
          "data": null
        },
        "recommended_transfers": {},
        "zone": {
          "data": null
        }
      },
      "type": "stop"
    }
  ]
}
                        """.trimIndent(),
                            HttpStatusCode.OK,
                            headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        )
                    else respondBadRequest()
                }
            }
        }

        assertEquals(
            Stops(
                listOf(
                    Stops.StopData(
                        type = "stop",
                        id = "place-alfcl",
                        attributes = Stops.StopData.Attributes(
                            name = "Alewife"
                        )
                    )
                )
            ),
            // passing the route parameter to the mock is useless but helps ensure we don't face errors forming it
            MbtaApi(mockClient).getStops("Red")
        )
    }
}