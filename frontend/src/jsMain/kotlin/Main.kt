import androidx.compose.runtime.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.attributes.*
import kotlinx.coroutines.*

import kotlin.js.json

fun main() {
    renderComposable(rootElementId = "root") {
        App()
    }
}

@Composable
fun App() {

    var input by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun translate() {
        scope.launch {
            val result = translateWord(input)
            translation = result
        }
    }

    Div {
        H2 { Text("Translator") }

        Input(type = InputType.Text) {
            value(input)
            onInput { input = it.value }

            onKeyDown {
                if (it.key == "Enter") {
                    translate()
                }
            }
        }

        Button(attrs = {
            onClick { translate() }
        }) {
            Text("Translate")
        }

        Hr()

        H3 { Text("Result") }
        P { Text(translation) }
    }
}

suspend fun translateWord(text: String): String {
    val response = kotlinx.browser.window.fetch("http://localhost:8080/translate",
        json(
            "method" to "POST",
            "headers" to json("Content-Type" to "application/json"),
            "body" to JSON.stringify(json("text" to text, "target" to "nl"))
        ).unsafeCast<org.w3c.fetch.RequestInit>()
    ).await()

    val json = response.text().await()
    return JSON.parse<dynamic>(json).translatedText as String
}
