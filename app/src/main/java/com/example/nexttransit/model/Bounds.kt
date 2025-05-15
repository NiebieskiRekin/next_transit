import kotlinx.serialization.Serializable

@Serializable
data class Bounds(
    val northeast: CoordinatesPoint,
    val southwest: CoordinatesPoint,
)