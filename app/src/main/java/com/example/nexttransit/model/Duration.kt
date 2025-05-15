import kotlinx.serialization.Serializable

@Serializable
data class Duration(
    val text: String,
    val value: Long
)