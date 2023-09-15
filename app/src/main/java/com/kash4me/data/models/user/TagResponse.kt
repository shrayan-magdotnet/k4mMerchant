package com.kash4me.data.models.user


import com.google.gson.annotations.SerializedName
import com.kash4me.utils.extensions.getEmptyIfNull

data class TagResponse(
    @SerializedName("count")
    val count: Int? = 0,
    @SerializedName("next")
    val next: Any? = Any(),
    @SerializedName("previous")
    val previous: Any? = Any(),
    @SerializedName("results")
    val results: List<Result?>? = listOf()
) {
    data class Result(
        @SerializedName("id")
        val id: Int? = 0,
        @SerializedName("name")
        val name: String? = ""
    ) {
        override fun toString(): String {
            return name.getEmptyIfNull()
        }
    }
}