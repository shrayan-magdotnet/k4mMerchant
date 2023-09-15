package com.kash4me.data.models.user.feedback


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FeedbackResponse(
    @SerializedName("detail")
    val detail: String? = ""
)