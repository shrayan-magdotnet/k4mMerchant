package com.kash4me.ui.activity.change_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.SuccessResponse
import com.kash4me.data.models.user.change_password.ChangePasswordRequest
import com.kash4me.repository.ChangePasswordRepository
import com.kash4me.utils.network.Resource
import kotlinx.coroutines.launch


class ChangePasswordViewModel(private val repository: ChangePasswordRepository) : ViewModel() {

    fun changePassword(
        token: String,
        oldPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<Resource<SuccessResponse>> {

        var result = MutableLiveData<Resource<SuccessResponse>>()

        viewModelScope.launch {

            val request = ChangePasswordRequest(
                newPassword1 = newPassword,
                newPassword2 = confirmNewPassword,
                oldPassword = oldPassword
            )

            result = repository.changePassword(
                token = token,
                request = request
            ).asLiveData() as MutableLiveData<Resource<SuccessResponse>>

        }

        return result

    }


}
