package com.kash4me.ui.activity.customer.customer_profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.kash4me.data.models.customer.customer_details.CustomerDetailsResponse
import com.kash4me.data.models.customer.update_profile.CustomerProfileUpdateRequest
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityCustomerProfileOldBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.ui.activity.splash.SplashViewModel
import com.kash4me.ui.activity.splash.SplashViewModelFactory
import com.kash4me.ui.activity.splash.customerDetailsResponse
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.LocationUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.TextDrawable
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.getNameInitials
import com.kash4me.utils.toast
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class CustomerProfileOldActivity : AppCompatActivity() {
    private var binding: ActivityCustomerProfileOldBinding? = null
    private val mBinding get() = binding!!
    private val sessionManager: SessionManager by lazy { SessionManager(applicationContext) }
    private lateinit var viewModel: SplashViewModel
    private var customDialogClass: CustomProgressDialog = CustomProgressDialog(this)
    private val autoCompleteRequestCode = 1
    private lateinit var place: Place

    private val requestState = MutableStateFlow<CustomerProfileUpdateRequest?>(null)
    private val shouldUpdateButtonBeEnabled = MutableLiveData<Boolean>().apply { value = false }

    companion object {
        private const val TAG = "CustomerProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerProfileOldBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        initView()
        setupToolbar()
        updateButtonStateObserver()

    }

    private fun updateButtonStateObserver() {
        shouldUpdateButtonBeEnabled.observe(this) { shouldBeEnabled ->
            mBinding.updateBtn.isEnabled = shouldBeEnabled
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initView() {
        initOnClick()
        initVM()
        setData()
        requestStateListeners()
    }

    private fun requestStateListeners() {
        mBinding.apply {
            etAddress.doOnTextChanged { text, _, _, _ ->
                if (requestState.value?.address != text.toString()) {
                    Log.d(TAG, "Address has changed")
                    shouldUpdateButtonBeEnabled.value = true
                } else {
                    shouldUpdateButtonBeEnabled.value = false
                }
            }
            etZipCode.doOnTextChanged { text, _, _, _ ->
                if (requestState.value?.zipCode != text.toString()) {
                    Log.d(TAG, "Zip code has changed")
                    shouldUpdateButtonBeEnabled.value = true
                } else {
                    shouldUpdateButtonBeEnabled.value = false
                }
            }
            etCellPhone.doOnTextChanged { text, _, _, _ ->
                if (requestState.value?.mobileNo != text.toString()) {
                    Log.d(TAG, "Mobile number has changed")
                    shouldUpdateButtonBeEnabled.value = true
                } else {
                    shouldUpdateButtonBeEnabled.value = false
                }
            }
        }
    }

    private fun initVM() {

        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )

        val userDetailsRepository = UserDetailsRepository(apiInterface)

        viewModel = ViewModelProvider(
            this, SplashViewModelFactory(userDetailsRepository)
        )[SplashViewModel::class.java]

        viewModel.fetchCustomerDetails()

        viewModel.updatedCustomerDetails.observe(this) {
            val successDialog = SuccessDialog.getInstance(message = "Details updated")
            successDialog.show(supportFragmentManager, successDialog.tag)
            setData()
            customDialogClass.hide()
        }

        viewModel.errorMessage.observe(this) {
            val errorDialog = ErrorDialog.getInstance(message = it.toString())
            errorDialog.show(supportFragmentManager, errorDialog.tag)
            customDialogClass.hide()
        }

    }

    private fun setData() {

        Timber.d("Customer details -> $customerDetailsResponse")

        mBinding.apply {
            tvNickname.text = customerDetailsResponse?.nick_name
            etAddress.setText(customerDetailsResponse?.address)
            etZipCode.setText(customerDetailsResponse?.zip_code)
        }
//        if (customerDetailsResponse?.mobile_no?.contains("-") == true) {
//            mBinding.etCellPhone.setText(
//                customerDetailsResponse?.mobile_no?.split("-")?.get(1) ?: ""
//            )
//        } else {
//            mBinding.etCellPhone.setText(customerDetailsResponse?.mobile_no)
//        }
        mBinding.etCellPhone.setText(customerDetailsResponse?.mobile_no)
        mBinding.etEmail.setText(customerDetailsResponse?.user_details?.email)
        setInitials()

        customerDetailsResponse?.let { customerDetails -> initializeWatchers(it = customerDetails) }

    }

    private fun setInitials() {
        val drawable = TextDrawable.builder()
            .buildRect(
                getNameInitials(customerDetailsResponse?.nick_name ?: "")
            )
        binding?.profileIV?.setImageDrawable(drawable)
    }

    private fun initOnClick() {
        binding?.btnEdit?.setOnClickListener { openPlacesAutoComplete() }
        binding?.updateBtn?.setOnClickListener {

            val phone = binding?.etCellPhone?.text.toString()
            if (phone.isEmpty()) {
                toast("Please enter phone number")
                return@setOnClickListener
            }

            val address = binding?.etAddress?.text.toString()
            if (address.isEmpty()) {
                toast("Please enter address")
                return@setOnClickListener
            }

            val zipCode = mBinding.etZipCode.text.toString()
            if (zipCode.isBlank()) {
                showToast(getString(R.string.please_enter_zip_code))
                return@setOnClickListener
            }

            customDialogClass.show()

            val request = getRequest(phone, zipCode)
            val token = sessionManager.fetchAuthToken().toString()
            viewModel.updateCustomerDetails(token, request)

        }
    }

    private fun initializeWatchers(it: CustomerDetailsResponse) {
        requestState.value = CustomerProfileUpdateRequest(
            address = it.address,
            latitude = it.latitude.toString(),
            longitude = it.longitude.toString(),
            mobileNo = it.mobile_no,
            zipCode = it.zip_code
        )
        Log.d(TAG, "initializeWatchers: ${requestState.value}")
    }

    private fun getParams(
        phone: String,
        zipCode: String
    ): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["mobile_no"] = phone
        params["address"] = binding?.etAddress?.text.toString()
        if (customerDetailsResponse?.address == binding?.etAddress?.text.toString()) {
            params["longitude"] = customerDetailsResponse?.longitude.toString()
            params["latitude"] = customerDetailsResponse?.latitude.toString()

        } else {
            params["longitude"] = place.latLng?.longitude.toString()
            params["latitude"] = place.latLng?.latitude.toString()
        }
        params["zip_code"] = zipCode
        return params
    }

    private fun getRequest(phone: String, zipCode: String): CustomerProfileUpdateRequest {

        val latitude: String
        val longitude: String

        if (customerDetailsResponse?.address == binding?.etAddress?.text.toString()) {
            longitude = customerDetailsResponse?.longitude.toString()
            latitude = customerDetailsResponse?.latitude.toString()

        } else {
            longitude = place.latLng?.longitude.toString()
            latitude = place.latLng?.latitude.toString()
        }

        return CustomerProfileUpdateRequest(
            address = mBinding.etAddress.text.toString(),
            latitude = latitude,
            longitude = longitude,
            mobileNo = phone,
            zipCode = zipCode
        )

    }

    private fun openPlacesAutoComplete() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields =
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.PLUS_CODE,
                Place.Field.ADDRESS_COMPONENTS
            )

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(applicationContext)
        startActivityForResult(intent, autoCompleteRequestCode)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == autoCompleteRequestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        place = Autocomplete.getPlaceFromIntent(data)
                        mBinding.etAddress.setText(place.address)
                        val postalCode = LocationUtils().getPostalCode(context = this, data = data)
                        mBinding.etZipCode.setText(postalCode)
                    }
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Timber.d(status.statusMessage ?: "")
                        val errorDialog =
                            ErrorDialog.getInstance(message = status.statusMessage.getEmptyIfNull())
                        errorDialog.show(supportFragmentManager, errorDialog.tag)
                    }
                }

                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}