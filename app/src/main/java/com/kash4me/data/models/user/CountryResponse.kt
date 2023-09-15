package com.kash4me.data.models.user


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kash4me.utils.extensions.getEmptyIfNull
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountryResponse(
    @SerializedName("country_lists")
    val countryLists: List<Country?>? = listOf()
) : Parcelable {

    @Parcelize
    data class Country(
        @SerializedName("iso")
        val iso: String? = "",
        @SerializedName("iso3")
        val iso3: String? = "",
        @SerializedName("name")
        val name: String? = ""
    ) : Parcelable {

        override fun toString(): String {
            return name.getEmptyIfNull()
        }

    }
}