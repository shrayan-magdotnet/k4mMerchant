package com.kash4me.ui.activity.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.user.feedback.FeedbackRequest
import com.kash4me.data.models.user.feedback.FeedbackResponse
import com.kash4me.repository.UserRepository
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(private val repository: UserRepository) : ViewModel() {

    fun sendFeedback(content: String): LiveData<Resource<FeedbackResponse>> {

        var result = MutableLiveData<Resource<FeedbackResponse>>()

        viewModelScope.launch {
            val request = FeedbackRequest(content = content)
            result = repository.sendFeedback(request = request)
                .asLiveData() as MutableLiveData<Resource<FeedbackResponse>>
        }

        return result

    }

}