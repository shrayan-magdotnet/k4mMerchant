package com.kash4me.repository

import com.kash4me.data.models.customer.annoucement.AnnouncementsResponse
import com.kash4me.data.models.merchant.announcement.create_or_update.CreateOrUpdateAnnouncementRequest
import com.kash4me.data.models.merchant.announcement.create_or_update.CreateOrUpdateAnnouncementResponse
import com.kash4me.data.models.merchant.announcement.get.GetAnnouncementResponse
import com.kash4me.network.ApiServices
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AnnouncementRepository @Inject constructor(
    val apiServices: ApiServices,
    val sessionManager: SessionManager
) {

    suspend fun getYourAnnouncement(): Flow<Resource<GetAnnouncementResponse>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getYourAnnouncement(token = token)
        }.flowOn(Dispatchers.IO)

    }

    suspend fun getAnnouncementsForCustomer(searchQuery: String): Flow<Resource<AnnouncementsResponse>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getAnnouncementsForCustomer(token = token, searchQuery = searchQuery)
        }.flowOn(Dispatchers.IO)

    }

    suspend fun createOrUpdateYourAnnouncement(request: CreateOrUpdateAnnouncementRequest)

            : Flow<Resource<CreateOrUpdateAnnouncementResponse>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.createOrUpdateYourAnnouncement(token = token, request = request)
        }.flowOn(Dispatchers.IO)

    }

}