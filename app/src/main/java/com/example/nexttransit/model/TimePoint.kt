import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimePoint(
    val text: String,
    @SerialName("time_zone")
    val timeZone: String,
    val value: Long,
)
