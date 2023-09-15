package com.kash4me.ui.fragments.staff.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kash4me.databinding.FragmentStaffProfileBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.network.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class StaffProfileFragment : Fragment() {

    private var _binding: FragmentStaffProfileBinding? = null
    private val mBinding get() = _binding!!

    private val mProgressDialog by lazy { CustomProgressDialog(context = requireContext()) }

    private val mViewModel by lazy {
        ViewModelProvider(this)[StaffProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffProfileBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.getStaffDetails().observe(viewLifecycleOwner) {

            Timber.d("Response -> $it")

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    mViewModel.profileDetails = it.value
                    showData()
                    Timber.d("Success -> ${it.value}")
                }
            }

        }

        val sessionManager = SessionManager(context = requireContext())
        mBinding.btnLogOut.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    sessionManager.logoutUser(packageContext = requireContext())
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()

        showData()

    }

    private fun showData() {
        mBinding.apply {
            tvName.text = mViewModel.profileDetails?.name
            tvCode.text = mViewModel.profileDetails?.userId
            tvUsernameBody.text = mViewModel.profileDetails?.name
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}