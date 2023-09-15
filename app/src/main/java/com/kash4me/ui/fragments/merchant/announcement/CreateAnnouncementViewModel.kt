package com.kash4me.ui.fragments.merchant.announcement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.merchant.announcement.create_or_update.CreateOrUpdateAnnouncementRequest
import com.kash4me.data.models.merchant.announcement.create_or_update.CreateOrUpdateAnnouncementResponse
import com.kash4me.data.models.merchant.announcement.get.GetAnnouncementResponse
import com.kash4me.repository.AnnouncementRepository
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAnnouncementViewModel
@Inject
constructor(val repository: AnnouncementRepository) : ViewModel() {

    fun getYourAnnouncement(): LiveData<Resource<GetAnnouncementResponse>> {

        var result = MutableLiveData<Resource<GetAnnouncementResponse>>()

        viewModelScope.launch {

            result = repository.getYourAnnouncement().asLiveData()
                    as MutableLiveData<Resource<GetAnnouncementResponse>>

        }
        return result

    }

    fun createOrUpdateYourAnnouncement(announcement: String, announcedAt: String, expiresOn: String)

            : LiveData<Resource<CreateOrUpdateAnnouncementResponse>> {

        var result = MutableLiveData<Resource<CreateOrUpdateAnnouncementResponse>>()

        viewModelScope.launch {

            val request = CreateOrUpdateAnnouncementRequest(
                message = announcement, startDate = announcedAt, endDate = expiresOn
            )

            result = repository.createOrUpdateYourAnnouncement(request = request).asLiveData()
                    as MutableLiveData<Resource<CreateOrUpdateAnnouncementResponse>>

        }

        return result

    }

}