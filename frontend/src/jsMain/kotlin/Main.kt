import androidx.compose.runtime.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
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
    var cursiveImages by remember { mutableStateOf<List<String>?>(null) }

    val scope = rememberCoroutineScope()

    suspend fun checkCursive(word: String) {
        if (word.isEmpty()) {
            cursiveImages = null
            return
        }
        val images = word.map { 
            val hex = it.code.toString(16).uppercase()
            val paddedHex = hex.padStart(4, '0')
            "U$paddedHex.png"
        }
        var allExist = true
        for (img in images) {
            try {
                val resp = kotlinx.browser.window.fetch("characters/$img", json("method" to "HEAD").unsafeCast<org.w3c.fetch.RequestInit>()).await()
                if (!resp.ok) {
                    allExist = false
                    break
                }
            } catch (e: Exception) {
                allExist = false
                break
            }
        }
        cursiveImages = if (allExist) images else null
    }

    fun translate() {
        scope.launch {
            val result = translateWord(input)
            translation = result
            checkCursive(result)
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

        cursiveImages?.let { images ->
            val isRtl = translation.any { it.code in 0x0590..0x08FF } // Hebrew, Arabic, etc.
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(if (isRtl) FlexDirection.RowReverse else FlexDirection.Row)
                    flexWrap(FlexWrap.Wrap)
                    justifyContent(if (isRtl) JustifyContent.FlexEnd else JustifyContent.FlexStart)
                }
            }) {
                images.forEach { img ->
                    Img(src = "characters/$img", alt = img)
                }
            }
        }
    }
}

suspend fun translateWord(text: String): String {
    val response = kotlinx.browser.window.fetch("http://localhost:8000/translate",
        json(
            "method" to "POST",
            "headers" to json("Content-Type" to "application/json"),
            "body" to JSON.stringify(json("text" to text, "target" to "he"))
        ).unsafeCast<org.w3c.fetch.RequestInit>()
    ).await()

    val json = response.text().await()
    return JSON.parse<dynamic>(json).translatedText as String
}
