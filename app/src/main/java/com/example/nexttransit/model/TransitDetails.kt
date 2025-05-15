
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransitDetails(
    @SerialName("arrival_stop")
    val arrivalStop: Stop,
    @SerialName("arrival_time")
    val arrivalTime: TimePoint,
    @SerialName("departure_stop")
    val departureStop: Stop,
    @SerialName("departure_time")
    val departureTime: TimePoint,
    val headsign: String,
    val line: Line?=null,
    @SerialName("num_stops")
    val numStops: Int,
    @SerialName("trip_short_name")
    val tripShortName: String="",
)