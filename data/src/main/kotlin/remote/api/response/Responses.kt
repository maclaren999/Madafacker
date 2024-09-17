package remote.api.response

import com.squareup.moshi.Json

data class NameAvailabilityResponse(
    @Json(name = "nameIsAvailable") val nameIsAvailable: Boolean
)