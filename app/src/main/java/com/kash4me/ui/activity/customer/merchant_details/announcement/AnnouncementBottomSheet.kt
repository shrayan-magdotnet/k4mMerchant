package com.kash4me.ui.activity.customer.merchant_details.announcement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kash4me.databinding.BottomSheetAnnouncementBinding

class AnnouncementBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAnnouncementBinding? = null
    private val mBinding get() = _binding!!

    companion object {

        private var mTitle: String = ""

        fun newInstance(title: String): AnnouncementBottomSheet {
            mTitle = title
            return AnnouncementBottomSheet()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAnnouncementBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tvTitle.text = mTitle

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}