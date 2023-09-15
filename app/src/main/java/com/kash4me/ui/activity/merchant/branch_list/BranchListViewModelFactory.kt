package com.kash4me.ui.activity.merchant.branch_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.MerchantBranchListRepository

class BranchListViewModelFactory(private val merchantBranchListRepository: MerchantBranchListRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BranchListViewModel(merchantBranchListRepository) as T
    }
}