package com.kash4me.data.models.user.feedback


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FeedbackRequest(
    @SerializedName("content")
    val content: String? = ""
)