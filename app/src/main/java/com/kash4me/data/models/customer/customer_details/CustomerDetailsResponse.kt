package com.kash4me.data.models.customer.customer_details

import android.os.Parcelable
import com.kash4me.data.models.UserDetails
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomerDetailsResponse(
    val address: String,
    val country_name: String,
    val date_of_birth: String,
    val latitude: Double,
    val longitude: Double,
    val mobile_no: String,
    val nick_name: String,
    val user_details: UserDetails,
    val zip_code: String
) : Parcelable