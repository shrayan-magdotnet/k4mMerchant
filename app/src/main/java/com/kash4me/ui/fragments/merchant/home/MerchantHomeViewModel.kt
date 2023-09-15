package com.kash4me.ui.fragments.merchant.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.local.merchant.MerchantTransactionSummaryEntity
import com.kash4me.data.models.MerchantTransactionSummaryResponse
import com.kash4me.data.models.places_api.DetectCountryResponse
import com.kash4me.repository.MerchantTransactionSummaryRepository
import com.kash4me.repository.google_places.GooglePlacesRepository
import com.kash4me.utils.SingleLiveEvent
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MerchantHomeViewModel
@Inject
constructor(
    private val merchantTransactionSummaryRepository: MerchantTransactionSummaryRepository
) : ViewModel() {

    val merchantTransactionSummaryResponse = SingleLiveEvent<MerchantTransactionSummaryResponse>()
    val errorMessage = SingleLiveEvent<String>()

    fun updateTransactionSummaryCache(token: String): LiveData<Resource<MerchantTransactionSummaryResponse>> {

        var result = MutableLiveData<Resource<MerchantTransactionSummaryResponse>>()

        viewModelScope.launch {
            result = merchantTransactionSummaryRepository.updateTransactionSummaryCache(token)
                .asLiveData() as MutableLiveData<Resource<MerchantTransactionSummaryResponse>>
        }

        return result

    }


    fun getTransactionSummaryFromDb(): LiveData<MerchantTransactionSummaryEntity?> {

        return merchantTransactionSummaryRepository.getTransactionSummaryFromDb()

    }

    fun detectCountry(
        latitude: Double, longitude: Double
    ): LiveData<Resource<DetectCountryResponse>> {

        var result = MutableLiveData<Resource<DetectCountryResponse>>()

        viewModelScope.launch {
            val googlePlacesRepository = GooglePlacesRepository()
            result = googlePlacesRepository.getCountry(latitude = latitude, longitude = longitude)
                .asLiveData() as MutableLiveData<Resource<DetectCountryResponse>>
        }

        return result

    }

}