import com.example.nexttransit.model.PlaceId
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val name: String = "",
    val placeId: PlaceId = "",
)