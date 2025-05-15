
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val bounds: Bounds,
    val copyrights: String,
    val legs: List<Leg>,
    @SerialName("overview_polyline")
    val overviewPolyline: OverviewPolyline,
    val summary: String,
    val warnings: List<String>,
    @SerialName("waypoint_order")
    val waypointOrder: List<String>
)