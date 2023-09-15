package com.kash4me.ui.fragments.staff.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.staff.StaffDetailsResponse
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.staff.StaffRepository
import com.kash4me.utils.App
import com.kash4me.utils.network.Resource
import kotlinx.coroutines.launch

class StaffProfileViewModel : ViewModel() {

    val repository by lazy {
        val apiInterface =
            ApiServices.invoke(
                networkConnectionInterceptor = NetworkConnectionInterceptor(App.getContext()!!),
                notFoundInterceptor = NotFoundInterceptor()
            )
        StaffRepository(apiInterface)
    }

    var profileDetails: StaffDetailsResponse? = null

    fun getStaffDetails(): LiveData<Resource<StaffDetailsResponse>> {

        var result = MutableLiveData<Resource<StaffDetailsResponse>>()

        viewModelScope.launch {
            result = repository.getStaffDetails()
                .asLiveData() as MutableLiveData<Resource<StaffDetailsResponse>>
        }

        return result

    }

}