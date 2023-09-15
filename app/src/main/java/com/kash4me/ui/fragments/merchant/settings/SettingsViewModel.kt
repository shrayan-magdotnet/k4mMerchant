package com.kash4me.ui.fragments.merchant.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.customer.delete.DeleteCustomerResponse
import com.kash4me.data.models.merchant.delete.DeleteMerchantResponse
import com.kash4me.data.models.user.notification_settings.NotificationSettingsResponse
import com.kash4me.data.models.user.notification_settings.UpdateNotificationSettingsRequest
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserRepository
import com.kash4me.utils.App
import com.kash4me.utils.network.Resource
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val mUserRepository: UserRepository by lazy {

        val apiServices = ApiServices.invoke(
            NetworkConnectionInterceptor(App.getContext()!!), NotFoundInterceptor()
        )

        UserRepository(apiServices = apiServices)

    }

    var mSavedEmailSettings: Boolean? = null

    fun getNotificationSettings(): LiveData<Resource<NotificationSettingsResponse>> {

        var result = MutableLiveData<Resource<NotificationSettingsResponse>>()

        viewModelScope.launch {
            result = mUserRepository.getNotificationSettings()
                .asLiveData() as MutableLiveData<Resource<NotificationSettingsResponse>>
        }

        return result

    }

    fun updateNotificationSettings(isChecked: Boolean): LiveData<Resource<NotificationSettingsResponse>> {

        var result = MutableLiveData<Resource<NotificationSettingsResponse>>()

        viewModelScope.launch {
            val request = UpdateNotificationSettingsRequest(emailSettings = isChecked)
            result = mUserRepository.updateNotificationSettings(request = request)
                .asLiveData() as MutableLiveData<Resource<NotificationSettingsResponse>>
        }

        return result

    }

    fun deleteMerchantAccount(): LiveData<Resource<DeleteMerchantResponse>> {

        var result = MutableLiveData<Resource<DeleteMerchantResponse>>()

        viewModelScope.launch {
            result = mUserRepository.deleteMerchantAccount()
                .asLiveData() as MutableLiveData<Resource<DeleteMerchantResponse>>
        }

        return result

    }

    fun deleteCustomerAccount(): LiveData<Resource<DeleteCustomerResponse>> {

        var result = MutableLiveData<Resource<DeleteCustomerResponse>>()

        viewModelScope.launch {
            result = mUserRepository.deleteCustomerAccount()
                .asLiveData() as MutableLiveData<Resource<DeleteCustomerResponse>>
        }

        return result

    }

}