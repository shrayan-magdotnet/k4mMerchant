package com.kash4me.ui.activity.customer.customer_profile


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.kash4me.data.models.customer.customer_details.CustomerDetailsResponse
import com.kash4me.data.models.customer.update_profile.CustomerProfileUpdateRequestV2
import com.kash4me.data.models.user.CountryResponse
import com.kash4me.merchant.databinding.ActivityCustomerProfileBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.ui.fragments.customer.profile.CustomerProfileViewModel
import com.kash4me.ui.fragments.customer.profile.CustomerProfileViewModelFactory
import com.kash4me.utils.AppConstants
import com.kash4me.utils.LocationUtils
import com.kash4me.utils.PhoneUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.roundOffToDecimalDigits
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.listeners.AfterDismissalListener
import com.kash4me.utils.network.Resource
import com.kash4me.utils.toast
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber


class CustomerProfileActivity : AppCompatActivity() {

    private var binding: ActivityCustomerProfileBinding? = null
    private val mBinding get() = binding!!

    private val sessionManager: SessionManager by lazy { SessionManager(applicationContext) }

    private val customDialogClass: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private val autoCompleteRequestCode = 1
    private lateinit var place: Place

    private var mSelectedCountryCode: String? = null

    private val mAvailableCountries = arrayListOf<CountryResponse.Country?>()

    private val mAdapter by lazy {
        ArrayAdapter(
            this, android.R.layout.simple_list_item_1, arrayListOf<CountryResponse.Country>()
        )
    }

    private val requestState = MutableStateFlow<CustomerProfileUpdateRequestV2?>(null)
    private val shouldUpdateButtonBeEnabled = MutableLiveData<Boolean>().apply { value = false }

    private var customerDetailsResponse: CustomerDetailsResponse? = null

    private val viewModel by lazy { initViewModel() }

    companion object {
        private const val TAG = "MerchantProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerProfileBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        initView()
        setupToolbar()

        val address = LocationUtils().getAddress(context = this, zipCode = "45210")
        Timber.d("Lat: ${address?.latitude} | Lon: ${address?.longitude}")
        Timber.d("Country: ${address?.countryName} | Code: ${address?.countryCode}")

        mBinding.countryCodePicker.registerCarrierNumberEditText(mBinding.phoneET)
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
        mBinding.spCountries.adapter = mAdapter
    }

    private fun initVM() {

        val token = sessionManager.fetchAuthToken() ?: ""
        fetchAvailableCountries(token)
        fetchMerchantDetails(token)

    }

    private fun fetchAvailableCountries(token: String) {
        viewModel.getAvailableCountries(token = token).observe(this) {

            when (it) {

                is Resource.Failure -> {
                    showToast(message = it.errorMsg)
                }

                Resource.Loading -> {
                    // Do nothing
                }

                is Resource.Success -> {
                    Timber.d("Countries -> ${it.value}")
                    val countries = it.value.countryLists
                    if (countries != null) {
                        mAdapter.addAll(countries)
                        mAvailableCountries.clear()
                        mAvailableCountries.addAll(countries)
                    }
                    val countryCodes = it.value.countryLists?.map { country -> country?.iso }
                    Timber.d("Country codes -> ${countryCodes?.joinToString()}")
                    if (countryCodes != null) {
                        mBinding.countryCodePicker.setCustomMasterCountries(
                            countryCodes.joinToString(separator = ",")
                        )
                    }
                    Timber.d("Country -> ${sessionManager.fetchCustomerDetails()?.country_name}")
                    val selectedCountry = it.value.countryLists?.find { country ->
                        country?.name.equals(
                            sessionManager.fetchCustomerDetails()?.country_name,
                            ignoreCase = true
                        )
                    }
                    Timber.d("Selected country -> $selectedCountry")
                    val selectedCountryIndex = mAdapter.getPosition(selectedCountry)
                    Timber.d("Selected country index -> $selectedCountryIndex")
                    mBinding.spCountries.setSelection(selectedCountryIndex)
                }

            }

        }
    }

    private fun fetchMerchantDetails(token: String) {
        viewModel.fetchCustomerDetails(token = token).observe(this) { resource ->

            when (resource) {

                is Resource.Failure -> {
                    customDialogClass.hide()
                    val errorDialog = ErrorDialog.getInstance(message = resource.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    customDialogClass.show()
                }

                is Resource.Success -> {
                    customDialogClass.hide()
                    customerDetailsResponse = resource.value
                    setData(profileDetails = resource.value)
                    sessionManager.saveCustomerDetails(customerDetails = resource.value)
                }
            }

        }
    }

    private fun initViewModel(): CustomerProfileViewModel {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext), NotFoundInterceptor()
            )
        val userDetailsRepository = UserDetailsRepository(apiInterface)
        val userRepository = UserRepository(apiInterface)
        return ViewModelProvider(
            this, CustomerProfileViewModelFactory(userDetailsRepository, userRepository)
        )[CustomerProfileViewModel::class.java]
    }

    private fun initializeWatchers(it: CustomerDetailsResponse) {
        requestState.value = CustomerProfileUpdateRequestV2(
            address = it.address,
            country = it.country_name,
            dateOfBirth = null,
            latitude = it.latitude.toString(),
            longitude = it.longitude.toString(),
            mobileNo = it.mobile_no,
            nickName = it.nick_name,
            zipCode = it.zip_code
        )
        Log.d(TAG, "initializeWatchers: ${requestState.value}")
    }

    private fun initializeTextWatchers() {
        mBinding.apply {
            tilNickName.editText?.doOnTextChanged { text, _, _, _ ->
                if (requestState.value?.nickName != text.toString()) {
                    Timber.d(
                        "Store name has changed: Previous -> ${requestState.value?.nickName} New -> $text"
                    )
                    shouldUpdateButtonBeEnabled.value = true
                } else {
                    shouldUpdateButtonBeEnabled.value = false
                }
            }
            spCountries.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // You can define your actions as you want
                }

                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {

                    val selectedObject =
                        mBinding.spCountries.selectedItem as CountryResponse.Country
                    Timber.d("Country from dropdown: ${selectedObject.name} | Selected country: ${requestState.value?.country}")
                    if (!selectedObject.name?.trim().equals(
                            requestState.value?.country?.trim(), ignoreCase = true
                        )
                    ) {
                        Log.d(TAG, "Country has changed")
                        shouldUpdateButtonBeEnabled.value = true
                    } else {
                        shouldUpdateButtonBeEnabled.value = false
                    }

                }
            }
            tilAddress.editText?.doOnTextChanged { text, _, _, _ ->
                if (requestState.value?.address != text.toString()) {
                    Log.d(TAG, "Address has changed")
                    shouldUpdateButtonBeEnabled.value = true
                } else {
                    shouldUpdateButtonBeEnabled.value = false
                }
            }
            tilZipCode.editText?.doOnTextChanged { text, _, _, _ ->
                if (requestState.value?.zipCode != text.toString()) {
                    Timber.d("Zip code has changed")
                    shouldUpdateButtonBeEnabled.value = true
                } else {
                    shouldUpdateButtonBeEnabled.value = false
                }
            }
            countryCodePicker.setOnCountryChangeListener {
                val countryCode = requestState.value?.mobileNo?.split("-")?.getOrNull(0)
                val countryCodeWithoutPlus = countryCode?.removePrefix("+")
                Timber.d("Country code -> $countryCodeWithoutPlus")
                val selectedCountryCode = countryCodePicker.selectedCountryCode
                Timber.d("Selected country code -> $selectedCountryCode")
                if (countryCodeWithoutPlus != selectedCountryCode) {
                    Log.d(TAG, "Country code has changed")
                    shouldUpdateButtonBeEnabled.value = true
                } else {
                    shouldUpdateButtonBeEnabled.value = false
                }
            }
            phoneET.doOnTextChanged { text, _, _, _ ->
                val savedPhoneNumber = requestState.value?.mobileNo?.split("-")?.getOrNull(1)
                val phoneNumber = PhoneUtils().sanitizePhoneNumber(phoneNumber = text.toString())
                Timber.d("Saved phone number -> $savedPhoneNumber")
                Timber.d("Phone number -> $phoneNumber")
                if (savedPhoneNumber != phoneNumber) {
                    Log.d(TAG, "Mobile number has changed")
                    shouldUpdateButtonBeEnabled.value = true
                } else {
                    shouldUpdateButtonBeEnabled.value = false
                }
            }
        }

    }

    private fun setData(profileDetails: CustomerDetailsResponse) {

        Timber.d("Merchant details -> $profileDetails")

        mBinding.apply {
            tilNickName.editText?.setText(profileDetails.nick_name)
            tilAddress.editText?.setText(profileDetails.address)
            tilZipCode.editText?.setText(profileDetails.zip_code)
            countryCodePicker.fullNumber = profileDetails.mobile_no
            mSelectedCountryCode = countryCodePicker.selectedCountryNameCode
            Timber.d("Selected country code -> $mSelectedCountryCode")
            val selectedCountry = mAvailableCountries.find {
                it?.name == profileDetails.country_name
            }
            Timber.d("Get selected country -> $selectedCountry")
//            mBinding.spCountries.setSelection(mAdapter.getPosition(selectedCountry))

            tilEmail.editText?.setText(profileDetails.user_details.email)
        }

        initializeWatchers(it = profileDetails)
        initializeTextWatchers()

    }

    private fun initOnClick() {
        binding?.btnEdit?.setOnClickListener { openPlacesAutoComplete() }
        btnUpdateListener()
    }

    private fun btnUpdateListener() {
        mBinding.updateBtn.setOnClickListener {

            val nickname = mBinding.tilNickName.editText?.text
            if (nickname.isNullOrEmpty()) {
                showToast("Please enter nickname")
                return@setOnClickListener
            }

//            val address = binding?.tilAddress?.editText?.text.toString()
//            if (address.isEmpty()) {
//                toast("Please enter address")
//                return@setOnClickListener
//            }

//            val postalCode = mBinding.tilZipCode.editText?.text.toString()
//            if (postalCode.isBlank()) {
//                showToast(getString(R.string.please_enter_zip_code))
//                return@setOnClickListener
//            }

            val phone = binding?.phoneET?.text.toString()
//            val phone = mBinding.countryCodePicker.fullNumberWithPlus
            Timber.d("Full number with plus-> $phone")
            if (phone.isEmpty()) {
                toast("Please enter phone number")
                return@setOnClickListener
            }

            val selectedCountry = mBinding.spCountries.selectedItem as CountryResponse.Country?
            val request = getRequest()
            val token = sessionManager.fetchAuthToken().toString()
            updateCustomerDetails(token, request)
//            validatePostalCode(
//                selectedCountry = selectedCountry,
//                postalCode = postalCode,
//                onValid = {
//                    val request = getRequest()
//                    val token = sessionManager.fetchAuthToken().toString()
//                    Timber.d("Request -> $request")
//                    updateCustomerDetails(token, request)
//                })

        }
    }

    private fun validatePostalCode(
        selectedCountry: CountryResponse.Country?,
        postalCode: String,
        onValid: () -> Unit
    ) {
        viewModel.reverseGeocode(
            country = selectedCountry?.name.getEmptyIfNull(),
            postalCode = postalCode
        ).observe(this) {

            when (it) {
                is Resource.Failure -> {
                    Timber.d("Failure -> ${it.errorMsg}")
                    customDialogClass.hide()
                    mBinding.tilZipCode.error =
                        "An error occurred while trying to validate postal code"
                }

                Resource.Loading -> {
                    Timber.d("Loading ")
                    customDialogClass.show()
                }

                is Resource.Success -> {
                    Timber.d("Success -> ${it.value}")
                    customDialogClass.hide()
                    if (it.value.results.isNullOrEmpty()) {
                        mBinding.tilZipCode.error = "No postal code found for selected country"
                    } else {
                        onValid()
                    }
                }

            }

        }
    }

    private fun updateCustomerDetails(
        token: String,
        request: HashMap<String, Any?>
    ) {
        viewModel.updateCustomerDetails(
            token = token,
            request = request,
        ).observe(this) { resource ->

            when (resource) {

                is Resource.Failure -> {
                    customDialogClass.hide()
                    val errorDialog = ErrorDialog.getInstance(message = resource.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    customDialogClass.show()
                }

                is Resource.Success -> {
                    customDialogClass.hide()
                    setData(profileDetails = resource.value!!)
                    val successDialog = SuccessDialog.getInstance(
                        message = "Profile has been updated",
                        afterDismissClicked = object : AfterDismissalListener {
                            override fun afterDismissed() {
                                finish()
                            }
                        }
                    )
                    successDialog.show(supportFragmentManager, successDialog.tag)
                    sessionManager.saveCustomerDetails(customerDetails = resource.value)
                }
            }

        }
    }

    private fun getRequest(): HashMap<String, Any?> {
        val params = HashMap<String, Any?>()

        var address: String? = mBinding.tilAddress.editText?.text.toString()
        var longitude: Double?
        var latitude: Double?
        if (!::place.isInitialized) {
            try {
                val decodedAddress = LocationUtils().getAddress(
                    context = this,
                    zipCode = binding?.tilZipCode?.editText?.text.toString()
                )
                latitude = decodedAddress?.latitude
                longitude = decodedAddress?.longitude
            } catch (e: Exception) {
                Timber.d("Exception caught: ${e.message}")
                showToast(message = e.message ?: "Couldn't geocode zipcode")
                latitude = null
                longitude = null
            }
        } else {
            address = place.address
            latitude = place.latLng?.latitude
            longitude = place.latLng?.longitude
        }

        params["nick_name"] = mBinding.tilNickName.editText?.text?.trim().toString()
//        params["address"] = address
        val selectedCountry = mBinding.spCountries.selectedItem as CountryResponse.Country?
        selectedCountry?.let {
            params["country"] = it.iso
        }
        params["mobile_no"] = mBinding.countryCodePicker.fullNumberWithPlus
//        params["zip_code"] = binding?.tilZipCode?.editText?.text.toString()
        params["longitude"] = longitude?.roundOffToDecimalDigits(
            n = AppConstants.LAT_LON_DECIMAL_DIGITS
        )
        params["latitude"] = latitude?.roundOffToDecimalDigits(
            n = AppConstants.LAT_LON_DECIMAL_DIGITS
        )

        return params
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
        val country = mBinding.spCountries.selectedItem as CountryResponse.Country?
        val countries = country?.let { listOf(it.iso) } ?: listOf()
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setCountries(countries)
//            .setTypeFilter(TypeFilter.REGIONS)
            .build(applicationContext)
        startActivityForResult(intent, autoCompleteRequestCode)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == autoCompleteRequestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        place = Autocomplete.getPlaceFromIntent(data)
                        mBinding.tilAddress.editText?.setText(place.address)
                        val postalCode = LocationUtils().getPostalCode(context = this, data = data)
                        mBinding.tilZipCode.editText?.setText(postalCode)
                    }
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Timber.i(status.statusMessage ?: "")
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