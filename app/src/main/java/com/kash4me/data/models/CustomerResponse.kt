package com.kash4me.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomerResponse(
    val country_name: String?,
    val date_of_birth: String?,
    val mobile_no: String?,
    val nick_name: String?,
    val user_details: UserDetails?,
    val zip_code: String?
) : Parcelable

@Parcelize
data class UserDetails(
    val email: String?,
    val id: Int?,
    val nick_name: String?,
    val unique_id: String?,
) : Parcelable {

    fun isUserDeleted(): Boolean {
        return id == null
    }

}