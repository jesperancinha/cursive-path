package org.jesperancinha.cursive.path

import androidx.compose.runtime.*
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import kotlinx.coroutines.*
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.RequestInit

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
    var letterHeight by remember { mutableStateOf(50) }
    var heightPercentages by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }

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
                val resp = window.fetch("characters/$img", json("method" to "HEAD").unsafeCast<RequestInit>()).await()
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

    fun translate(wordToTranslate: String) {
        scope.launch {
            val result = translateWord(wordToTranslate)
            translation = result
            checkCursive(result)

            val url = window.location.let {
                "${it.origin}${it.pathname}?word=${js("encodeURIComponent")(wordToTranslate)}"
            }
            window.history.pushState(null, "", url)
        }
    }

    LaunchedEffect(Unit) {
        val params = URLSearchParams(window.location.search)
        val wordParam = params.get("word")
        if (!wordParam.isNullOrEmpty()) {
            input = wordParam
            translate(wordParam)
        }

        try {
            val resp = window.fetch("character_heights.csv").await()
            if (resp.ok) {
                val text = resp.text().await()
                val map = text.split("\n")
                    .filter { it.contains(",") }
                    .associate {
                        val parts = it.split(",")
                        parts[0].trim() to (parts[1].trim().toDoubleOrNull() ?: 100.0)
                    }
                heightPercentages = map
            }
        } catch (e: Exception) {
            console.error("Failed to load character heights: ${e.message}")
        }
    }

    Div {
        H2 { Text("Translator") }

        Input(type = InputType.Text) {
            value(input)
            onInput { input = it.value }

            onKeyDown {
                if (it.key == "Enter") {
                    translate(input)
                }
            }
        }

        Button(attrs = {
            onClick { translate(input) }
        }) {
            Text("Translate")
        }

        Hr()

        Div({
            style {
                marginTop(10.px)
                marginBottom(10.px)
            }
        }) {
            Label(forId = "height-slider") {
                Text("Letter Height: ")
            }
            Input(type = InputType.Range) {
                id("height-slider")
                min("5")
                max(MAX_CHARACTER_HEIGHT)
                value(letterHeight)
                onInput { letterHeight = it.value?.toInt() ?: 50 }
            }
            Input(type = InputType.Number) {
                style {
                    marginLeft(10.px)
                    width(50.px)
                }
                min("5")
                max(MAX_CHARACTER_HEIGHT)
                value(letterHeight)
                onInput { letterHeight = it.value?.toInt() ?: 50 }
            }
            Text(" px")
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
                    val percentage = heightPercentages[img] ?: 100.0
                    val calculatedHeight = letterHeight * (percentage / 100.0)
                    Img(src = "characters/$img", alt = img, attrs = {
                        style {
                            height(calculatedHeight.px)
                        }
                    })
                }
            }
        }
    }
}

suspend fun translateWord(text: String): String {
    val host = window.location.hostname
    try {
        val response = window.fetch(
            "http://$host:8000/translate",
            json(
                "method" to "POST",
                "headers" to json("Content-Type" to "application/json"),
                "body" to JSON.stringify(json("text" to text, "target" to "he"))
            ).unsafeCast<RequestInit>()
        ).await()

        val textResponse = response.text().await()
        val json = JSON.parse<dynamic>(textResponse)
        if (response.ok) {
            return json.translatedText as String
        } else {
            val error = json.error ?: "Unknown error"
            return "Error: $error (Status: ${response.status})"
        }
    } catch (e: Exception) {
        return "Error: ${e.message}"
    }
}

private const val MAX_CHARACTER_HEIGHT = "1000"
