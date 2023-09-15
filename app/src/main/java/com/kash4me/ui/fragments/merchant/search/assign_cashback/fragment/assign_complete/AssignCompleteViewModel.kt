package com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.assign_complete

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.repository.MerchantTransactionSummaryRepository
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignCompleteViewModel
@Inject
constructor(val merchantTransactionSummaryRepository: MerchantTransactionSummaryRepository)

    : ViewModel() {

    fun updateTransactionSummaryCache(token: String): LiveData<Resource<Unit>> {

        var result = MutableLiveData<Resource<Unit>>()

        viewModelScope.launch {
            result = merchantTransactionSummaryRepository.updateTransactionSummaryCache(token)
                .asLiveData() as MutableLiveData<Resource<Unit>>
        }

        return result

    }

}