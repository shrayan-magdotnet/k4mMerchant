package com.kash4me.ui.fragments.staff.my_transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.kash4me.data.models.staff.StaffTransactionsResponse
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.staff.StaffRepository
import com.kash4me.utils.App
import com.kash4me.utils.network.Resource

class MyTransactionsViewModel : ViewModel() {

    val repository by lazy {
        val apiInterface =
            ApiServices.invoke(
                networkConnectionInterceptor = NetworkConnectionInterceptor(App.getContext()!!),
                notFoundInterceptor = NotFoundInterceptor()
            )
        StaffRepository(apiInterface)
    }

    suspend fun getTransactions(): LiveData<Resource<StaffTransactionsResponse>> {

        return repository.getTransactions().asLiveData()

    }

}