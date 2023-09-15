package com.kash4me.data.models.places_api


import com.google.gson.annotations.SerializedName

data class ReverseGeocodeResponse(
    @SerializedName("results")
    val results: List<Result?>? = listOf(),
    @SerializedName("status")
    val status: String? = ""
) {
    data class Result(
        @SerializedName("address_components")
        val addressComponents: List<AddressComponent?>? = listOf(),
        @SerializedName("formatted_address")
        val formattedAddress: String? = "",
        @SerializedName("geometry")
        val geometry: Geometry? = Geometry(),
        @SerializedName("place_id")
        val placeId: String? = "",
        @SerializedName("postcode_localities")
        val postcodeLocalities: List<String?>? = listOf(),
        @SerializedName("types")
        val types: List<String?>? = listOf()
    ) {
        data class AddressComponent(
            @SerializedName("long_name")
            val longName: String? = "",
            @SerializedName("short_name")
            val shortName: String? = "",
            @SerializedName("types")
            val types: List<String?>? = listOf()
        )

        data class Geometry(
            @SerializedName("bounds")
            val bounds: Bounds? = Bounds(),
            @SerializedName("location")
            val location: Location? = Location(),
            @SerializedName("location_type")
            val locationType: String? = "",
            @SerializedName("viewport")
            val viewport: Viewport? = Viewport()
        ) {
            data class Bounds(
                @SerializedName("northeast")
                val northeast: Northeast? = Northeast(),
                @SerializedName("southwest")
                val southwest: Southwest? = Southwest()
            ) {
                data class Northeast(
                    @SerializedName("lat")
                    val lat: Double? = 0.0,
                    @SerializedName("lng")
                    val lng: Double? = 0.0
                )

                data class Southwest(
                    @SerializedName("lat")
                    val lat: Double? = 0.0,
                    @SerializedName("lng")
                    val lng: Double? = 0.0
                )
            }

            data class Location(
                @SerializedName("lat")
                val lat: Double? = 0.0,
                @SerializedName("lng")
                val lng: Double? = 0.0
            )

            data class Viewport(
                @SerializedName("northeast")
                val northeast: Northeast? = Northeast(),
                @SerializedName("southwest")
                val southwest: Southwest? = Southwest()
            ) {
                data class Northeast(
                    @SerializedName("lat")
                    val lat: Double? = 0.0,
                    @SerializedName("lng")
                    val lng: Double? = 0.0
                )

                data class Southwest(
                    @SerializedName("lat")
                    val lat: Double? = 0.0,
                    @SerializedName("lng")
                    val lng: Double? = 0.0
                )
            }
        }
    }
}