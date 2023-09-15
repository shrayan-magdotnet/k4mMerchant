package com.kash4me.data.models.merchant

import com.google.gson.annotations.SerializedName

data class TagsDetail(
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("tag_id")
    val tagId: String? = ""
)