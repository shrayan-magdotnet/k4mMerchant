package com.kash4me.data.models.places_api


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DetectCountryResponse(
    @SerializedName("plus_code")
    val plusCode: PlusCode? = PlusCode(),
    @SerializedName("results")
    val results: List<Result?>? = listOf(),
    @SerializedName("status")
    val status: String? = ""
) {
    @Keep
    data class PlusCode(
        @SerializedName("compound_code")
        val compoundCode: String? = "",
        @SerializedName("global_code")
        val globalCode: String? = ""
    )

    @Keep
    data class Result(
        @SerializedName("address_components")
        val addressComponents: List<AddressComponent?>? = listOf(),
        @SerializedName("formatted_address")
        val formattedAddress: String? = "",
        @SerializedName("geometry")
        val geometry: Geometry? = Geometry(),
        @SerializedName("place_id")
        val placeId: String? = "",
        @SerializedName("plus_code")
        val plusCode: PlusCode? = PlusCode(),
        @SerializedName("types")
        val types: List<String?>? = listOf()
    ) {
        @Keep
        data class AddressComponent(
            @SerializedName("long_name")
            val longName: String? = "",
            @SerializedName("short_name")
            val shortName: String? = "",
            @SerializedName("types")
            val types: List<String?>? = listOf()
        )

        @Keep
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
            @Keep
            data class Bounds(
                @SerializedName("northeast")
                val northeast: Northeast? = Northeast(),
                @SerializedName("southwest")
                val southwest: Southwest? = Southwest()
            ) {
                @Keep
                data class Northeast(
                    @SerializedName("lat")
                    val lat: Double? = 0.0,
                    @SerializedName("lng")
                    val lng: Double? = 0.0
                )

                @Keep
                data class Southwest(
                    @SerializedName("lat")
                    val lat: Double? = 0.0,
                    @SerializedName("lng")
                    val lng: Double? = 0.0
                )
            }

            @Keep
            data class Location(
                @SerializedName("lat")
                val lat: Double? = 0.0,
                @SerializedName("lng")
                val lng: Double? = 0.0
            )

            @Keep
            data class Viewport(
                @SerializedName("northeast")
                val northeast: Northeast? = Northeast(),
                @SerializedName("southwest")
                val southwest: Southwest? = Southwest()
            ) {
                @Keep
                data class Northeast(
                    @SerializedName("lat")
                    val lat: Double? = 0.0,
                    @SerializedName("lng")
                    val lng: Double? = 0.0
                )

                @Keep
                data class Southwest(
                    @SerializedName("lat")
                    val lat: Double? = 0.0,
                    @SerializedName("lng")
                    val lng: Double? = 0.0
                )
            }
        }

        @Keep
        data class PlusCode(
            @SerializedName("compound_code")
            val compoundCode: String? = "",
            @SerializedName("global_code")
            val globalCode: String? = ""
        )
    }
}