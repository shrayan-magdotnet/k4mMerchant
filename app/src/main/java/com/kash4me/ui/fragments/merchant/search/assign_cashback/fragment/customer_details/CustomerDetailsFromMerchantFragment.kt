package com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.customer_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.kash4me.data.models.UserDetails
import com.kash4me.data.models.merchant.CustomerDetailsResponseDto
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentCustomerDetailsBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.CustomerDetailsFromMerchantRepository
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.merchant.search.assign_cashback.AssignCashbackActivity
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatAsCurrency
import timber.log.Timber

class CustomerDetailsFromMerchantFragment : Fragment() {

    private var customerDetails: CustomerDetailsResponseDto? = null
    private val userDetails: UserDetails? by lazy {
        (activity as AssignCashbackActivity).intent.getParcelableExtra("customer_details")
    }

    val sessionManager by lazy { SessionManager(requireContext()) }
    val progressDialog by lazy { CustomProgressDialog(requireContext()) }

    private var _binding: FragmentCustomerDetailsBinding? = null
    private val mBinding get() = _binding!!

    private val viewModel: CustomerDetailsFromMerchantViewModel by lazy {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(requireContext().applicationContext),
                NotFoundInterceptor()
            )
        val merchantCustomerListRepository = CustomerDetailsFromMerchantRepository(apiInterface)
        ViewModelProvider(
            this,
            CustomerDetailsFromMerchantViewModelFactory(merchantCustomerListRepository)
        )[CustomerDetailsFromMerchantViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerDetailsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        initVM()

        mBinding.btnAssignCashBack.setOnClickListener {
            val selectedCustomerDetails = this.customerDetails
            if (selectedCustomerDetails == null) {
                val errorDialog =
                    ErrorDialog.getInstance(message = "Something went wrong when trying to assign cashback")
                errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                return@setOnClickListener
            }
            val action = CustomerDetailsFromMerchantFragmentDirections
                .actionAssignCashBackFragmentToPurchaseAmountFragment()
            findNavController().navigate(action)
        }

        mBinding.tvRevertCashBack.setOnClickListener {
            findNavController().navigate(R.id.action_assignCashBackFragment_to_revertCashbackFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        (activity as AssignCashbackActivity).setSupportActionBar(mBinding.toolbarLayout.toolbar)
        (activity as AssignCashbackActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AssignCashbackActivity).supportActionBar?.title = userDetails?.nick_name
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener {
            (activity as AssignCashbackActivity).onBackPressed()
        }
    }


    private fun initVM() {

//        makeFilterOptions(
//            lat = CurrentLocation.lat.toString(),
//            lng = CurrentLocation.lng.toString(),
//        )

        getCustomerDetails()

    }

    private fun getCustomerDetails() {


        ((activity) as AssignCashbackActivity).customDialogClass.show()

        viewModel.getCustomerDetailsFromMerchant(
            token = (activity as AssignCashbackActivity).token,
            customerId = (activity as AssignCashbackActivity).customerId
        )

        viewModel.customerDetailsFromMerchantResponse.observe(viewLifecycleOwner) {
            customerDetails = it
            setData(it)
        }


        viewModel.errorMessage.observe(viewLifecycleOwner) {
            val errorDialog = ErrorDialog.getInstance(message = it)
            errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
            ((activity) as AssignCashbackActivity).customDialogClass.hide()
        }
    }


    private fun setData(it: CustomerDetailsResponseDto) {

        Timber.d("response -> $it")

        mBinding.tvCustomerCode.text = it.userDetails?.uniqueId

        mBinding.apply {
            tvTotalCashbackAmount.text = "$" + it.totalEarned?.formatAsCurrency()
            tvProcessingAmount.text = "$" + it.processingAmount?.formatAsCurrency()
            tvRecentCashback.text = "$" + it.cashbackAmount?.formatAsCurrency()
            tvTotalSpent.text = "$" + it.amountSpent?.formatAsCurrency()
            tvYetToSpend.text = "$" + it.amountLeft?.formatAsCurrency()
        }

//        val qrImage = ImageUtils().decodeImageFromBase64(
//            context = requireContext(),
//            base64String = it.qrImage
//        )
//        mBinding.ivQrCode.setImageBitmap(qrImage)

        ((activity) as AssignCashbackActivity).customDialogClass.hide()


    }


}