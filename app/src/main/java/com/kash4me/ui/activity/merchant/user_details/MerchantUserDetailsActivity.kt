package com.kash4me.ui.activity.merchant.user_details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.textfield.TextInputLayout
import com.hbb20.CountryCodePicker
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityMerchantUserDetailsBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.ui.activity.splash.SplashViewModel
import com.kash4me.ui.activity.splash.SplashViewModelFactory
import com.kash4me.ui.activity.splash.merchantDetailsResponse
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.SessionManager
import com.kash4me.utils.TextDrawable
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.getNameInitials
import com.kash4me.utils.toast
import timber.log.Timber

class MerchantUserDetailsActivity : AppCompatActivity() {

    private lateinit var backIV: ImageView
    private lateinit var profileIV: ImageView
    private lateinit var nickNameTIL: TextInputLayout
    private lateinit var addressTIL: TextInputLayout
    private lateinit var phoneTIL: TextInputLayout
    private lateinit var emailTIL: TextInputLayout
    private lateinit var postCodeTIL: TextInputLayout
    private lateinit var countryCodePicker: CountryCodePicker


    private val autoCompleteRequestCode = 1

    private lateinit var place: Place


    private lateinit var editBtn: Button
    private lateinit var updateBtn: Button
    private lateinit var viewModel: SplashViewModel

    private var customDialogClass: CustomProgressDialog = CustomProgressDialog(this)

    private lateinit var sessionManager: SessionManager

    private var binding: ActivityMerchantUserDetailsBinding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMerchantUserDetailsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        sessionManager = SessionManager(applicationContext)
        initView()
        setupToolbar()
    }


    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }


    private fun initView() {
//        backIV = findViewById(R.id.backIV)
//        backIV.visibility = View.VISIBLE

        profileIV = findViewById(R.id.profileIV)
        nickNameTIL = findViewById(R.id.nickNameTIL)
        addressTIL = findViewById(R.id.addressTIL)
        phoneTIL = findViewById(R.id.phoneTIL)
        emailTIL = findViewById(R.id.emailTIL)
        postCodeTIL = findViewById(R.id.postalCodeTIL)
        editBtn = findViewById(R.id.editBtn)
        updateBtn = findViewById(R.id.updateBtn)
        countryCodePicker = findViewById(R.id.countryCode_picker)

        setData()

        initOnClick()

        initVM()
    }

    private fun setData() {
        nickNameTIL.editText?.setText(merchantDetailsResponse?.name)
        addressTIL.editText?.setText(merchantDetailsResponse?.address)
        if (merchantDetailsResponse?.mobile_no?.contains("-") == true) {
            phoneTIL.editText?.setText(merchantDetailsResponse?.mobile_no?.split("-")?.get(1) ?: "")
        } else {
            phoneTIL.editText?.setText(merchantDetailsResponse?.mobile_no)
        }
        emailTIL.editText?.setText("merchant@email.com")
        postCodeTIL.editText?.setText(merchantDetailsResponse?.zip_code)
        setInitials()
    }

    private fun setInitials() {
        val drawable = TextDrawable.builder()
            .buildRect(
                getNameInitials(merchantDetailsResponse?.name ?: "")
            )
        profileIV.setImageDrawable(drawable)
    }

    private fun initOnClick() {

//        backIV.setOnClickListener {
//            onBackPressed()
//        }

        editBtn.setOnClickListener {
            updateBtn.visibility = View.VISIBLE
            editBtn.visibility = View.GONE
            enableEditing()
        }

        updateBtn.setOnClickListener {

            val nickName = nickNameTIL.editText?.text.toString().trim()
            val zipCode = postCodeTIL.editText?.text.toString()
            val phone = phoneTIL.editText?.text.toString()

            if (nickName.isEmpty()) {
                toast("Please enter a valid name")
                return@setOnClickListener
            }
            if (zipCode.isEmpty()) {
                toast("Please enter postal code")
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                toast("Please enter phone number")
                return@setOnClickListener
            }

            customDialogClass.show()


            val params = HashMap<String, Any>()
            params["name"] = nickNameTIL.editText?.text.toString()
            params["mobile_no"] = "${countryCodePicker.selectedCountryCode}-$phone"
            params["zip_code"] = postCodeTIL.editText?.text.toString()
            params["country"] = countryCodePicker.selectedCountryNameCode
            params["address"] = addressTIL.editText?.text.toString()
            if (merchantDetailsResponse?.address == addressTIL.editText?.text.toString()) {
                params["longitude"] = merchantDetailsResponse?.longitude.toString()
                params["latitude"] = merchantDetailsResponse?.latitude.toString()

            } else {
                params["longitude"] = place.latLng?.longitude.toString()
                params["latitude"] = place.latLng?.latitude.toString()
            }

            val token = sessionManager.fetchAuthToken().toString()
            viewModel.updateMerchantDetails(token, params, merchantDetailsResponse?.id ?: 0)
        }

        addressTIL.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                openPlacesAutoComplete()
            }
        }

        addressTIL.editText?.setOnClickListener {
            openPlacesAutoComplete()
        }
    }


    private fun enableEditing() {
        nickNameTIL.isEnabled = true
        addressTIL.isEnabled = true
        phoneTIL.isEnabled = true
        postCodeTIL.isEnabled = true
        enableCountryCodePicker()
    }

    private fun enableCountryCodePicker() {
        mBinding.countryCodePicker.alpha = 1f
        mBinding.countryCodePicker.setCcpClickable(true)
    }

    private fun disableEditing() {
        nickNameTIL.isEnabled = false
        addressTIL.isEnabled = false
        phoneTIL.isEnabled = false
        postCodeTIL.isEnabled = false
        updateBtn.visibility = View.GONE
        editBtn.visibility = View.VISIBLE
        disableCountryCodePicker()
    }

    private fun disableCountryCodePicker() {
        mBinding.countryCodePicker.alpha = 0.35f
        mBinding.countryCodePicker.setCcpClickable(false)
    }

    private fun initVM() {

        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )

        val userDetailsRepository = UserDetailsRepository(apiInterface)

        viewModel = ViewModelProvider(
            this,
            SplashViewModelFactory(userDetailsRepository),

            )[SplashViewModel::class.java]

        viewModel.updatedMerchantDetails.observe(this) {
            val successDialog = SuccessDialog.getInstance(message = "Details updated")
            successDialog.show(supportFragmentManager, successDialog.tag)
            setData()
            disableEditing()
            customDialogClass.hide()

        }

        viewModel.errorMessage.observe(this) {
            val errorDialog = ErrorDialog.getInstance(message = it.toString())
            errorDialog.show(supportFragmentManager, errorDialog.tag)
            customDialogClass.hide()
        }

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
            .build(applicationContext)
        startActivityForResult(intent, autoCompleteRequestCode)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == autoCompleteRequestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        place = Autocomplete.getPlaceFromIntent(data)
                        addressTIL.editText?.setText(place.address)

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