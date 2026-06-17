import kotlinx.serialization.Serializable

@Serializable
data class TranslateRequest(
    val text: String,
    val source: String = "en",
    val target: String
)

@Serializable
data class TranslateResponse(
    val translatedText: String
)
