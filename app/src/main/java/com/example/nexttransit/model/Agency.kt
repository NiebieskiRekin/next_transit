import kotlinx.serialization.Serializable

@Serializable
data class Agency(
    val phone: String?=null,
    val url: String?=null,
    val name: String
)