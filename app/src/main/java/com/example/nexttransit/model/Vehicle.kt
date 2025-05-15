import kotlinx.serialization.Serializable

@Serializable
data class Vehicle(
    val type: String,
    val icon: String,
    val name: String
)