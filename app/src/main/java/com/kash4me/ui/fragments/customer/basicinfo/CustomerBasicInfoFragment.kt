package com.kash4me.ui.fragments.customer.basicinfo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.textfield.TextInputLayout
import com.kash4me.data.models.user.CountryResponse
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentCustomerBasicInfoBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.PostCustomerDetailsRepository
import com.kash4me.repository.UserRepository
import com.kash4me.ui.activity.customer.CustomerRegistrationActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.CurrentLocation
import com.kash4me.utils.LocationPermissionHelper
import com.kash4me.utils.LocationUtils
import com.kash4me.utils.RequestCodes
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.roundOffToDecimalDigits
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import com.kash4me.utils.toast
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CustomerBasicInfoFragment

    : Fragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    companion object {
        fun newInstance() = CustomerBasicInfoFragment()
    }

    private val viewModel: CustomerBasicInfoViewModel by lazy {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(requireContext().applicationContext),
                NotFoundInterceptor()
            )

        val registerRepository = PostCustomerDetailsRepository(apiInterface)
        val userRepository = UserRepository(apiInterface)

        ViewModelProvider(
            this,
            CustomerBasicInfoViewModelFactory(
                postCustomerDetailsRepository = registerRepository,
                userRepository = userRepository
            )
        )[CustomerBasicInfoViewModel::class.java]
    }
    private lateinit var sessionManager: SessionManager
    private var cal: Calendar = Calendar.getInstance()

    private val autoCompleteRequestCode = 1
    private lateinit var addressET: TextInputLayout
    private lateinit var place: Place

    private var selectedCountry: String? = null
    private val availableCountryCodes = arrayListOf<String?>()

    private val mAdapter by lazy {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            arrayListOf<CountryResponse.Country>()
        )
    }

    private var _binding: FragmentCustomerBasicInfoBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerBasicInfoBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireActivity().applicationContext)

        fetchAvailableCountries(token = sessionManager.fetchAuthToken().getEmptyIfNull())
        mBinding.spCountries.adapter = mAdapter

        val nickNameET = view.findViewById<TextInputLayout>(R.id.customerNameET)
        val zipCodeET = view.findViewById<TextInputLayout>(R.id.customerAddressET)
        zipCodeET.editText?.setText(CurrentLocation.postalCode)

        val phoneET = view.findViewById<EditText>(R.id.customerPhoneET)
        addressET = view.findViewById(R.id.addressET)

        mBinding.countryCodePicker.registerCarrierNumberEditText(mBinding.customerPhoneET)

        val nextBtn = view.findViewById<Button>(R.id.nextBtn)
        btnNextListener(nextBtn, nickNameET, zipCodeET, phoneET)

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

//        mBinding.customerDOBET.editText?.inputType = InputType.TYPE_NULL

//        mBinding.customerDOBET.editText?.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                showDate(dateSetListener)
//            }
//        }

//        mBinding.customerDOBET.editText?.setOnClickListener {
//            showDate(dateSetListener)
//        }

        addressET.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                openPlacesAutoComplete()
            }
        }

        addressET.editText?.setOnClickListener {
            openPlacesAutoComplete()
        }

        mBinding.spCountries.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // You can define your actions as you want
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {

                val selectedObject = mBinding.spCountries.selectedItem as CountryResponse.Country
                Timber.d("Selected country -> ${selectedObject.iso}")
                selectedCountry = selectedObject.iso

            }
        }

        viewModel.registerResponse.observe(viewLifecycleOwner) {
            sessionManager.saveUserProfile(true)
            requireContext().toast("Details updated")
            val bundle = bundleOf("userDetails" to it)
            ((activity) as CustomerRegistrationActivity).customDialogClass.hide()
            Navigation.findNavController(view).navigate(R.id.customerFinishFragment, bundle)

        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            requireContext().toast(it.toString())
            ((activity) as CustomerRegistrationActivity).customDialogClass.hide()
        }

    }

    private fun btnNextListener(
        nextBtn: Button,
        nickNameET: TextInputLayout,
        zipCodeET: TextInputLayout,
        phoneET: EditText
    ) {
        nextBtn.setOnClickListener {

//            val areLocationPermissionsGranted =
//                LocationPermissionHelper.arePermissionsGranted(context = requireContext())
//            if (areLocationPermissionsGranted) {
//                proceedToCreatingCustomer(nickNameET, zipCodeET, phoneET)
//            } else {
//                proceedToDetectingCountry() // This initiates asking for location permission
//            }

            proceedToCreatingCustomer(nickNameET, zipCodeET, phoneET)

        }
    }

    private fun proceedToCreatingCustomer(
        nickNameET: TextInputLayout,
        zipCodeET: TextInputLayout,
        phoneET: EditText
    ) {
        val nickname = nickNameET.editText?.text.toString().trim()
        val zipCode = zipCodeET.editText?.text.toString()
        //            val dob = mBinding.customerDOBET.editText?.text.toString()
        val phone = phoneET.text.toString()

        var isValid = true

        if (nickname.isEmpty()) {
            nickNameET.error = "Please enter a valid name"
            isValid = false
        } else {
            nickNameET.error = ""
        }

        if (selectedCountry.isNullOrBlank()) {
            showToast("Please Select a country")
            isValid = false
        }

        //            if (zipCode.isEmpty()) {
        //                showToast("Please enter postal code")
        //                isValid = false
        //            } else {
        //                zipCodeET.error = ""
        //            }

        //            if (dob.isEmpty()) {
        //                dobET.error = "Please enter date of birth"
        //                isValid = false
        //            } else {
        //                dobET.error = ""
        //            }

        if (phone.isEmpty()) {
            phoneET.error = "Please enter phone number"
            isValid = false
        } else {
            phoneET.error = null
        }

        if (!isValid) {
            return
        }

        val params = HashMap<String, String>()
        params["nick_name"] = nickname
        //            if (dob.isNotBlank()) {
        //                params["date_of_birth"] = dob
        //            }
        params["mobile_no"] = "+${mBinding.countryCodePicker.selectedCountryCode}-$phone"
        //            params["zip_code"] = zipCode
        params["country"] = selectedCountry!!

        val token = sessionManager.fetchAuthToken().toString()

        val latitude = viewModel.latitude
        if (latitude != null) {
            params["latitude"] =
                latitude.roundOffToDecimalDigits(n = AppConstants.LAT_LON_DECIMAL_DIGITS)
                    .toString()
        } else {
            params["latitude"] = "0.0"
        }

        val longitude = viewModel.longitude
        if (longitude != null) {
            params["longitude"] =
                longitude.roundOffToDecimalDigits(n = AppConstants.LAT_LON_DECIMAL_DIGITS)
                    .toString()
        } else {
            params["longitude"] = "0.0"
        }

        Timber.d("Request -> $params")
        viewModel.postCustomerDetails(token, params)

        //            validatePostalCode(
        //                selectedCountry = mBinding.spCountries.selectedItem as CountryResponse.Country?,
        //                postalCode = mBinding.customerAddressET.editText?.text.toString(),
        //                onValid = { latitude, longitude ->
        //                    ((activity) as CustomerRegistrationActivity).customDialogClass.show()
        //                    val token = sessionManager.fetchAuthToken().toString()
        //
        //                    params["latitude"] =
        //                        latitude.roundOffToDecimalDigits(n = AppConstants.LAT_LON_DECIMAL_DIGITS)
        //                            .toString()
        //                    params["longitude"] =
        //                        longitude.roundOffToDecimalDigits(n = AppConstants.LAT_LON_DECIMAL_DIGITS)
        //                            .toString()
        //
        //                    Timber.d("Request -> $params")
        //                    viewModel.postCustomerDetails(token, params)
        //                }
        //            )
    }

    private fun proceedToDetectingCountry() {

        val areLocationPermissionsGranted =
            LocationPermissionHelper.arePermissionsGranted(requireContext())
        if (areLocationPermissionsGranted) {

            Timber.d("Permissions are granted, let's use the services")
            getLocation()

        } else {

            Timber.d("Permissions are not granted, let's request the permissions")
            requestLocationPermissions()

        }

    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->

            if (location != null) {
                viewModel.latitude = location.latitude
                viewModel.longitude = location.longitude
                // Now you have the latitude and longitude values, do whatever you need with them.
                Timber.d("Detected Latitude: ${viewModel.latitude} | Longitude: ${viewModel.longitude}")
                val country = LocationUtils().detectCountry(
                    context = requireContext(),
                    latitude = viewModel.latitude.getZeroIfNull(),
                    longitude = viewModel.longitude.getZeroIfNull()
                )
                val countryCode = availableCountryCodes.find { country?.code == it }
                Timber.d("Is the current location available in countries option? -> $countryCode")

                if (countryCode != null) {
                    selectedCountry = countryCode
                    val indexOfCountryCode = availableCountryCodes.indexOf(countryCode)
                    Timber.d("Position of $countryCode -> $indexOfCountryCode")
                    mBinding.spCountries.setSelection(indexOfCountryCode)
                    mBinding.countryCodePicker.setCountryForNameCode(countryCode)
                }

            }
        }
    }

    private fun requestLocationPermissions() {
        LocationPermissionHelper.requestPermissions(fragment = this)
        val rationale = "Application requires location permissions for better functioning"
        EasyPermissions.requestPermissions(
            this,
            rationale,
            LocationPermissionHelper.REQ_CODE,
            *LocationPermissionHelper.getPermissions()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.d("onRequestPermissionsResult called")
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

        Timber.d("onPermissionsGranted: Permissions have been granted")

        if (requestCode == RequestCodes.LOCATION_PERMISSION.getCode()) {

            Timber.d("onPermissionsGranted: Let's get latitude and longitude to detect country")
            proceedToDetectingCountry()

        }

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

        val hasPermanentlyDeniedPermissions =
            EasyPermissions.somePermissionPermanentlyDenied(this, perms)
        if (hasPermanentlyDeniedPermissions) {
            Timber.d("Some permissions are permanently denied")
            showAppSettingsDialog()
        }

    }

    private fun showAppSettingsDialog() {
        Timber.d("Showing app settings dialog")
        AppSettingsDialog
            .Builder(this)
            .build()
            .show()
    }

    override fun onRationaleAccepted(requestCode: Int) {

        if (requestCode == RequestCodes.LOCATION_PERMISSION.getCode()) {
            Timber.d("Rationale accepted for getting latitude and longitude to detect country")
        }

    }

    override fun onRationaleDenied(requestCode: Int) {

        if (requestCode == RequestCodes.LOCATION_PERMISSION.getCode()) {
            Timber.d("Rationale denied for getting latitude and longitude to detect country")
        }

    }

    private fun validatePostalCode(
        selectedCountry: CountryResponse.Country?,
        postalCode: String,
        onValid: (latitude: Double, longitude: Double) -> Unit
    ) {
        viewModel.reverseGeocode(
            country = selectedCountry?.name.getEmptyIfNull(),
            postalCode = postalCode
        ).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    Timber.d("Failure -> ${it.errorMsg}")
                    ((activity) as CustomerRegistrationActivity).customDialogClass.hide()
                    mBinding.customerAddressET.error =
                        "An error occurred while trying to validate postal code"
                }

                Resource.Loading -> {
                    Timber.d("Loading ")
                    ((activity) as CustomerRegistrationActivity).customDialogClass.show()
                }

                is Resource.Success -> {
                    ((activity) as CustomerRegistrationActivity).customDialogClass.hide()
                    Timber.d("Success -> ${it.value}")
                    if (it.value.results.isNullOrEmpty()) {
                        mBinding.customerAddressET.error =
                            "No postal code found for selected country"
                    } else {
                        onValid(
                            it.value.results.getOrNull(0)?.geometry?.location?.lat.getZeroIfNull(),
                            it.value.results.getOrNull(0)?.geometry?.location?.lng.getZeroIfNull()
                        )
                    }
                }

            }

        }
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
//        mBinding.customerDOBET.editText?.setText(sdf.format(cal.time))
    }

    private fun showDate(dateSetListener: DatePickerDialog.OnDateSetListener) {
        val datePickerDialog = DatePickerDialog(
            (activity as CustomerRegistrationActivity),
            dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
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
                Place.Field.PLUS_CODE
            )

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())
        startActivityForResult(intent, autoCompleteRequestCode)

    }

    private fun fetchAvailableCountries(token: String) {
        viewModel.getAvailableCountries(token = token).observe(viewLifecycleOwner) {

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
                        // TODO: Update country selection logic once UI has been updated
                        // Need to decide whether we need address input field or not
                        mAdapter.clear()
                        mAdapter.addAll(countries)
//                        mAvailableCountries.clear()
//                        mAvailableCountries.addAll(countries)
                    }
                    val countryCodes = it.value.countryLists?.map { country -> country?.iso }
                    availableCountryCodes.clear()
                    availableCountryCodes.addAll(countryCodes ?: listOf())
                    Timber.d("Country codes -> $countryCodes")
                    Timber.d("Country codes -> ${countryCodes?.joinToString()}")
                    if (countryCodes != null) {
                        mBinding.countryCodePicker.setCustomMasterCountries(
                            countryCodes.joinToString(separator = ",")
                        )
                    }
                    selectedCountry = it.value.countryLists?.getOrNull(0)?.iso

                    proceedToDetectingCountry()

                }

            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == autoCompleteRequestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        place = Autocomplete.getPlaceFromIntent(data)
                        addressET.editText?.setText(place.address)

                    }
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Timber.d(status.statusMessage.getEmptyIfNull())
                        val errorDialog =
                            ErrorDialog.getInstance(message = status.statusMessage.getEmptyIfNull())
                        errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}