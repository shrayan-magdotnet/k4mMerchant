package com.kash4me.ui.activity.customer.announcements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.customer.annoucement.AnnouncementsResponse
import com.kash4me.repository.AnnouncementRepository
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(val repository: AnnouncementRepository)

    : ViewModel() {

    fun getAnnouncements(searchQuery: String): LiveData<Resource<AnnouncementsResponse>> {

        var result = MutableLiveData<Resource<AnnouncementsResponse>>()

        viewModelScope.launch {

            result = repository.getAnnouncementsForCustomer(searchQuery = searchQuery).asLiveData()
                    as MutableLiveData<Resource<AnnouncementsResponse>>

        }
        return result

    }


}