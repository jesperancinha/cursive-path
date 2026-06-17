import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.serialization.kotlinx.json.json as jsonConfig
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun main() {
    embeddedServer(Netty, port = 8000) {
        module()
    }.start(wait = true)
}

fun Application.module() {

    install(ServerContentNegotiation) {
        jsonConfig(Json { ignoreUnknownKeys = true })
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
    }

    val client = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            jsonConfig(Json { ignoreUnknownKeys = true })
        }
    }

    val ltUrl = System.getenv("LT_URL") ?: "http://localhost:5000"
    println("Using LibreTranslate URL: $ltUrl")
    routing {
        post("/translate") {
            val req = call.receive<Map<String, String>>()

            val text = req["text"] ?: ""
            val target = req["target"] ?: "nl"
            runCatching {
                val response: String = client.post("$ltUrl/translate") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "q" to text,
                            "source" to "en",
                            "target" to target,
                            "format" to "text"
                        )
                    )
                }.bodyAsText()
                val json = Json.parseToJsonElement(response).jsonObject
                val translated = json["translatedText"]?.jsonPrimitive?.content ?: ""

                call.respond(mapOf("translatedText" to translated))
            }.onFailure {
                println("Error during translation: ${it.message}")
                println(it.stackTraceToString())
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Translation failed"))
            }


        }
    }
}
