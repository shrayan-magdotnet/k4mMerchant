package com.kash4me.data.models.user


import com.google.gson.annotations.SerializedName

data class InfoBoxResponse(
    @SerializedName("count")
    val count: Int? = 0,
    @SerializedName("next")
    val next: String? = "",
    @SerializedName("previous")
    val previous: String? = "",
    @SerializedName("results")
    val results: List<Result?>? = listOf()
) {
    data class Result(
        @SerializedName("message")
        val message: String? = "",
        @SerializedName("created_at")
        val createdAt: String? = "",
        @SerializedName("id")
        val id: Int? = 0,
        @SerializedName("title")
        val title: String? = ""
    )
}