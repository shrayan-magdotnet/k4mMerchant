package com.kash4me.ui.fragments.merchant.sub_user_settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.merchant.sub_user_settings.SubUserResponse
import com.kash4me.data.models.merchant.sub_user_settings.add_sub_user.AddSubUserRequest
import com.kash4me.data.models.merchant.sub_user_settings.delete_sub_user.DeleteSubUserResponse
import com.kash4me.data.models.merchant.sub_user_settings.reset_staff_password.ResetStaffPasswordRequest
import com.kash4me.data.models.merchant.sub_user_settings.reset_staff_password.ResetStaffPasswordResponse
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.SubUserSettingsRepository
import com.kash4me.utils.App
import com.kash4me.utils.network.Resource
import kotlinx.coroutines.launch

class SubUserSettingsViewModel : ViewModel() {

    private val repository by lazy {
        val apiInterface = ApiServices.invoke(
            NetworkConnectionInterceptor(App.getContext()!!), NotFoundInterceptor()
        )
        SubUserSettingsRepository(apiInterface)
    }

    fun getSubUsers(): LiveData<Resource<List<SubUserResponse>>> {

        var result = MutableLiveData<Resource<List<SubUserResponse>>>()

        viewModelScope.launch {
            result = repository.getSubUsers().asLiveData()
                    as MutableLiveData<Resource<List<SubUserResponse>>>
        }

        return result

    }

    fun addSubUser(nickname: String, password: String, shopId: Int, userId: String)

            : LiveData<Resource<List<SubUserResponse>?>> {

        var result = MutableLiveData<Resource<List<SubUserResponse>?>>()

        viewModelScope.launch {
            val request = AddSubUserRequest(
                nickName = nickname, password = password, shopId = shopId, userId = userId
            )
            result = repository.addSubUser(request = request)
                .asLiveData() as MutableLiveData<Resource<List<SubUserResponse>?>>
        }

        return result

    }

    fun deleteSubUser(shopId: Int): LiveData<Resource<DeleteSubUserResponse?>> {

        var result = MutableLiveData<Resource<DeleteSubUserResponse?>>()

        viewModelScope.launch {
            result = repository.deleteSubUser(shopId = shopId)
                .asLiveData() as MutableLiveData<Resource<DeleteSubUserResponse?>>
        }

        return result

    }

    fun resetStaffPassword(request: ResetStaffPasswordRequest): LiveData<Resource<ResetStaffPasswordResponse?>>? {

        var result = MutableLiveData<Resource<ResetStaffPasswordResponse?>>()

        viewModelScope.launch {
            result = repository.resetStaffPassword(request = request)
                .asLiveData() as MutableLiveData<Resource<ResetStaffPasswordResponse?>>
        }

        return result

    }

}