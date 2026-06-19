package org.jesperancinha.cursive.path

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
import com.hazelcast.core.*
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

    val hazelcastInstance: HazelcastInstance = Hazelcast.newHazelcastInstance()
    val translationCache = hazelcastInstance.getMap<String, String>("translations")

    val ltUrl = System.getenv("LT_URL") ?: "http://localhost:5000"
    println("Using LibreTranslate URL: $ltUrl")
    routing {
        post("/translate") {
            val req = call.receive<Map<String, String>>()

            val text = req["text"] ?: ""
            val target = req["target"] ?: "nl"
            val source = "en"
            val cacheKey = "$source:$target:$text"

            val cachedResponse = translationCache[cacheKey]
            if (cachedResponse != null) {
                call.respond(mapOf("translatedText" to cachedResponse))
                return@post
            }

            runCatching {
                println("Making a real request to LibreTranslate for text: $text")
                val response = client.post("$ltUrl/translate") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "q" to text,
                            "source" to source,
                            "target" to target,
                            "format" to "text"
                        )
                    )
                }

                if (response.status.isSuccess()) {
                    val responseText = response.bodyAsText()
                    val json = Json.parseToJsonElement(responseText).jsonObject
                    val translated = json["translatedText"]?.jsonPrimitive?.content ?: ""
                    translationCache[cacheKey] = translated
                    call.respond(mapOf("translatedText" to translated))
                } else {
                    val errorBody = response.bodyAsText()
                    println("LibreTranslate error response: ${response.status}")
                    println("Error body: $errorBody")
                    call.respond(response.status, mapOf("error" to "LibreTranslate returned ${response.status}"))
                }
            }.onFailure {
                println("Error during translation: ${it.message}")
                println(it.stackTraceToString())
                println("""
                    NOTE: 
                    - Ensure LibreTranslate is running and accessible at $ltUrl. 
                    - Also check if the LT_URL environment variable is set correctly.
                    - If you are using Docker, ensure that the container can access the host's network or that LT_URL points to the correct address. 
                    - For example, if running LibreTranslate in Docker on the same machine, you might need to use 'host.docker.internal' instead of 'localhost' in LT_URL. 
                    - If you are running LibreTranslate in a container, ensure that the container's network settings allow it to communicate with the Ktor server.
                    - Check if you didn't surpass you daily request limit for the free tier of LibreTranslate, which may be 1000 requests per day
                    """.trimIndent())
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Translation failed"))
            }


        }
    }
}
