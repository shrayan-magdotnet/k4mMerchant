package com.kash4me.ui.activity.merchant.merchant_profile

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.kash4me.R
import com.kash4me.data.models.merchant.TagsDetail
import com.kash4me.data.models.merchant.profile.MerchantProfileResponse
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateRequest
import com.kash4me.data.models.user.CountryResponse
import com.kash4me.data.models.user.TagResponse
import com.kash4me.databinding.ActivityMerchantProfileBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.FileUtils
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.LocationUtils
import com.kash4me.utils.PhoneUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.ValidationUtils
import com.kash4me.utils.convertDpToPixel
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.clear
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getMinusOneIfNull
import com.kash4me.utils.extensions.getNotAvailableIfEmptyOrNull
import com.kash4me.utils.extensions.roundOffToDecimalDigits
import com.kash4me.utils.extensions.showSnackbar
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.listeners.AfterDismissalListener
import com.kash4me.utils.network.Resource
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt

@AndroidEntryPoint
class MerchantProfileActivity : AppCompatActivity() {

    private var binding: ActivityMerchantProfileBinding? = null
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

    private val atvTagsAdapter by lazy {
        val tags = arrayListOf<TagResponse.Result>()
        ArrayAdapter(this, android.R.layout.simple_list_item_1, tags)
    }
    private val selectedTags = arrayListOf<TagsDetail>()

    private val requestState = MutableStateFlow<MerchantProfileUpdateRequest?>(null)
    private val tagsState = MutableStateFlow<List<TagsDetail>?>(null)
    private val shouldUpdateButtonBeEnabled = MutableLiveData<Boolean>().apply { value = false }

    private var merchantProfileDetails: MerchantProfileResponse? = null

    private val viewModel: MerchantProfileViewModel by viewModels()

    companion object {
        private const val TAG = "MerchantProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMerchantProfileBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        initView()
        setupToolbar()

        mBinding.atvTags.apply {
            setAdapter(atvTagsAdapter)
            setDropDownBackgroundResource(R.color.white)
            setOnFocusChangeListener { _, _ -> showDropDown() }
        }

        getAvailableTags()

        mBinding.countryCodePicker.registerCarrierNumberEditText(mBinding.phoneET)
        shouldUpdateButtonBeEnabled.observe(this) { shouldBeEnabled ->
            mBinding.updateBtn.isEnabled = shouldBeEnabled
        }

        mBinding.cvLogo.setOnClickListener { showCameraOrGalleryDialog(view = mBinding.cvLogo) }
        mBinding.cvCoverPhoto.setOnClickListener { showCameraOrGalleryDialog(view = mBinding.cvCoverPhoto) }

    }

    fun showCameraOrGalleryDialog(view: MaterialCardView) {
        // check get current api version
        var lPerm = Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lPerm = Manifest.permission.READ_MEDIA_VIDEO
        }

        if (ContextCompat.checkSelfPermission(this, lPerm) != PackageManager.PERMISSION_GRANTED) {
            this.let {
                PermissionX.init(it)
                    .permissions(lPerm)
                    .explainReasonBeforeRequest()
                    .onExplainRequestReason { scope, deniedList ->
                        scope.showRequestReasonDialog(
                            deniedList,
                            "Please allow the following permissions to access full features of the app",
                            "OK",
                            "Cancel"
                        )
                    }
                    .onForwardToSettings { scope, deniedList ->
                        scope.showForwardToSettingsDialog(
                            deniedList,
                            "You need to allow necessary permissions in Settings manually",
                            "OK",
                            "Cancel"
                        )
                    }
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            //                            Toast.makeText(context, "All permissions are granted", Toast.LENGTH_LONG).show()
                        } else {
                            //                            Toast.makeText(context, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        } else {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setMessage("Please choose!")
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera") { dialog, _ ->
                dialog.dismiss()
                dispatchTakePictureIntent(view = view)
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Gallery") { dialog, _ ->
                dialog.dismiss()

                val sizeLimitMessage = getAttachmentSizeLimitInfoMessage()

                AlertDialog.Builder(this)
                    .setTitle(sizeLimitMessage)
                    .setPositiveButton(R.string.ok) { warningDialog, _ ->
                        pickImageFromGallery(view = view)
                        warningDialog.dismiss()
                    }
                    .setNegativeButton(R.string.cancel) { warningDialog, _ ->
                        warningDialog?.dismiss()
                    }
                    .show()

            }

            alertDialog.show()

            val btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            val paddingHorizontal = 16f.convertDpToPixel(this).roundToInt()
            val paddingVertical = 8f.convertDpToPixel(this).roundToInt()

            btnPositive.setPaddingRelative(
                paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical
            )
            btnNegative.setPaddingRelative(
                paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical
            )

        }
    }

    private fun getAttachmentSizeLimitInfoMessage(): String {
        val message: String = String.format(
            getString(R.string.please_keep_image_size_below_x_mb), AppConstants.IMAGE_SIZE_LIMIT
        )
        return Html.fromHtml(message).toString()
    }

    private fun dispatchTakePictureIntent(view: MaterialCardView) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            this.let {
                PermissionX.init(it)
                    .permissions(Manifest.permission.CAMERA)
                    .explainReasonBeforeRequest()
                    .onExplainRequestReason { scope, deniedList ->
                        scope.showRequestReasonDialog(
                            deniedList,
                            "Please allow the following permissions to access full features of the app",
                            "OK",
                            "Cancel"
                        )
                    }
                    .onForwardToSettings { scope, deniedList ->
                        scope.showForwardToSettingsDialog(
                            deniedList,
                            "You need to allow necessary permissions in Settings manually",
                            "OK",
                            "Cancel"
                        )
                    }
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            //                            Toast.makeText(context, "All permissions are granted", Toast.LENGTH_LONG).show()
                        } else {
                            //                            Toast.makeText(context, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                if (view == mBinding.cvLogo) {
                    Timber.d("Selected view is Logo")
                    selectLogoLauncher.launch(takePictureIntent)
                } else if (view == mBinding.cvCoverPhoto) {
                    Timber.d("Selected view is Cover Photo")
                    selectCoverPhotoLauncher.launch(takePictureIntent)
                }
            } catch (e: ActivityNotFoundException) {
                // display error state to the user
            }
        }
    }

    private val selectLogoLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            val imageBitmap = it.data?.extras?.get("data") as Bitmap
            val logo = FileUtils().saveBitmapAsFile(context = this, bitmap = imageBitmap)
            viewModel.logoFile = logo
            viewModel.hasLogoBeenUpdated.value = true

            val maxWidth = 256 // Maximum desired width
            val maxHeight = 256 // Maximum desired height

            // Resize the bitmap
            val resizedBitmap =
                Bitmap.createScaledBitmap(imageBitmap, maxWidth, maxHeight, true)

            Glide.with(this)
                .load(resizedBitmap)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(mBinding.ivLogo)

        }

    private val selectCoverPhotoLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            val imageBitmap = it.data?.extras?.get("data") as Bitmap
            val logo = FileUtils().saveBitmapAsFile(context = this, bitmap = imageBitmap)
            viewModel.coverPhotoFile = logo
            viewModel.hasCoverPhotoBeenUpdated.value = true

            val maxWidth = 256 // Maximum desired width
            val maxHeight = 256 // Maximum desired height

            // Resize the bitmap
            val resizedBitmap =
                Bitmap.createScaledBitmap(imageBitmap, maxWidth, maxHeight, true)

            Glide.with(this)
                .load(resizedBitmap)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(mBinding.ivCoverPhoto)

        }


    private fun pickImageFromGallery(view: MaterialCardView) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"

        if (view == mBinding.cvLogo) {
            Timber.d("Logo is selected")
            pickLogoLauncher.launch(intent)
        } else if (view == mBinding.cvCoverPhoto) {
            Timber.d("Cover Photo is selected")
            pickCoverPhotoLauncher.launch(intent)
        }

    }

    private val pickLogoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri: Uri? = result.data?.data

                // Handle the selected image URI here
                if (uri != null) {

                    val file = FileUtils().createFileFromUri(context = this, uri = uri)

                    if (file != null) {

                        val sizeInMb = FileUtils().getFileSizeInMB(file = file)
                        Timber.d("Image size in MB -> $sizeInMb")
                        val maxImageSize = AppConstants.IMAGE_SIZE_LIMIT
                        if (sizeInMb > maxImageSize) {
                            val errorDialog = ErrorDialog.getInstance(
                                message = "Selected image is greater than maximum image size"
                            )
                            errorDialog.show(supportFragmentManager, errorDialog.tag)
                            return@registerForActivityResult
                        }

                    }

                    processSelectedImage(context = this, imageUri = uri)
                }
            }
        }

    private val pickCoverPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri: Uri? = result.data?.data

                // Handle the selected image URI here
                if (uri != null) {

                    val file = FileUtils().createFileFromUri(context = this, uri = uri)

                    if (file != null) {

                        val sizeInMb = FileUtils().getFileSizeInMB(file = file)
                        Timber.d("Image size in MB -> $sizeInMb")
                        val maxImageSize = AppConstants.IMAGE_SIZE_LIMIT
                        if (sizeInMb > maxImageSize) {
                            val errorDialog = ErrorDialog.getInstance(
                                message = "Selected image is greater than maximum image size"
                            )
                            errorDialog.show(supportFragmentManager, errorDialog.tag)
                            return@registerForActivityResult
                        }

                    }

                    processSelectedImageForCoverPhoto(context = this, imageUri = uri)
                }
            }
        }

    // Process the selected image
    private fun processSelectedImage(context: Context, imageUri: Uri) {
        try {
            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT < 28) {
                // For Android versions prior to Android 10
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            } else {
                // For Android 10 and above
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            }

            // Use the bitmap for further processing (e.g., display, upload, etc.)
            if (bitmap != null) {
                // Do something with the bitmap
//                viewModel.logoFile = saveBitmapAsFile(context = context, bitmap = bitmap)
                val maxWidth = 256 // Maximum desired width
                val maxHeight = 256 // Maximum desired height

                // Resize the bitmap
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true)

                viewModel.logoFile =
                    FileUtils().saveBitmapAsFile(context = context, bitmap = resizedBitmap)
                viewModel.hasLogoBeenUpdated.value = true

                Glide.with(this)
                    .load(resizedBitmap)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(mBinding.ivLogo)

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Process the selected image
    private fun processSelectedImageForCoverPhoto(context: Context, imageUri: Uri) {
        try {
            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT < 28) {
                // For Android versions prior to Android 10
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            } else {
                // For Android 10 and above
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            }

            // Use the bitmap for further processing (e.g., display, upload, etc.)
            if (bitmap != null) {
                // Do something with the bitmap
//                viewModel.logoFile = saveBitmapAsFile(context = context, bitmap = bitmap)
                val maxWidth = 256 // Maximum desired width
                val maxHeight = 256 // Maximum desired height

                // Resize the bitmap
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true)

                viewModel.coverPhotoFile =
                    FileUtils().saveBitmapAsFile(context = context, bitmap = resizedBitmap)
                viewModel.hasCoverPhotoBeenUpdated.value = true

                Glide.with(this)
                    .load(resizedBitmap)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(mBinding.ivCoverPhoto)

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getAvailableTags() {
        viewModel.getAvailableTags().observe(this) {

            when (it) {
                is Resource.Failure -> {
                    Timber.d(it.errorMsg)
                    showToast(it.errorMsg)
                }

                Resource.Loading -> {
                    Timber.d("Loading")
                }

                is Resource.Success -> {
                    Timber.d(it.value.toString())
                    if (it.value.results != null)
                        atvTagsAdapter.addAll(it.value.results)
                }
            }

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
                    Timber.d("Country codes -> $countryCodes")
                    Timber.d("Country codes -> ${countryCodes?.joinToString()}")
                    if (countryCodes != null) {
                        mBinding.countryCodePicker.setCustomMasterCountries(
                            countryCodes.joinToString(separator = ",")
                        )
                    }
                    val selectedCountry = it.value.countryLists?.find { country ->
                        country?.name.equals(merchantProfileDetails?.countryName, ignoreCase = true)
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
        viewModel.fetchMerchantDetails(token = token).observe(this) { resource ->

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
                    setData(profileDetails = resource.value, shouldUpdateImages = true)
                    merchantProfileDetails = resource.value
                    sessionManager.saveMerchantDetails(merchantDetails = resource.value)
                }
            }

        }
    }

    private fun initializeWatchers(it: MerchantProfileResponse) {
        requestState.value = MerchantProfileUpdateRequest(
            address = it.address,
            countryName = it.countryName,
            logo = it.logo,
            latitude = it.latitude,
            longitude = it.longitude,
            mobileNo = it.mobileNo,
            tags = it.tagsDetails?.map { tag -> tag?.name },
            zipCode = it.zipCode,
            description = it.description,
            headOfficeId = it.headOfficeDetails?.uniqueOfficeId,
            name = it.name,
            personName = null,
            promotionalText = it.promotionalText
        )
        tagsState.value = it.tagsDetails?.map { TagsDetail(name = it?.name, tagId = it?.tagId) }
        Log.d(TAG, "initializeWatchers: ${requestState.value}")
        Log.d(TAG, "initializeWatchers: ${tagsState.value}")
    }

    private fun initializeTextWatchers() {
        mBinding.apply {
            tilBusinessName.editText?.doOnTextChanged { text, _, _, _ ->
                if (requestState.value?.name != text.toString()) {
                    Timber.d(
                        "Store name has changed: Previous -> ${requestState.value?.name} New -> $text"
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
                    Timber.d("Country from dropdown: ${selectedObject.name} | Selected country: ${requestState.value?.countryName}")
                    if (!selectedObject.name?.trim().equals(
                            requestState.value?.countryName?.trim(), ignoreCase = true
                        )
                    ) {
                        Log.d(TAG, "Country has changed")
                        shouldUpdateButtonBeEnabled.value = true
                        mBinding.tilAddress.clear()
                        mBinding.tilZipCode.clear()
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
            tilPromotionalText.editText?.doOnTextChanged { text, _, _, _ ->
                if (requestState.value?.promotionalText != text.toString()) {
                    Timber.d("Promotional text has changed")
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
            tilDescription.editText?.doOnTextChanged { text, _, _, _ ->
                if (requestState.value?.description != text.toString()) {
                    Log.d(TAG, "Description has changed")
                    shouldUpdateButtonBeEnabled.value = true
                } else {
                    shouldUpdateButtonBeEnabled.value = false
                }
            }
            tilHeadOfficeId.editText?.doOnTextChanged { text, _, _, _ ->
                if (requestState.value?.headOfficeId != text.toString()) {
                    Log.d(TAG, "Head office ID has changed")
                    shouldUpdateButtonBeEnabled.value = true
                } else {
                    shouldUpdateButtonBeEnabled.value = false
                }
            }
            viewModel.hasLogoBeenUpdated.observe(this@MerchantProfileActivity) { logoHasBeenChanged ->
                shouldUpdateButtonBeEnabled.value = logoHasBeenChanged
            }
            lifecycleScope.launchWhenStarted {
                tagsState.collect {
                    Log.d(TAG, "Request state tags -> ${requestState.value?.tags}")
                    val tagTitles = it?.map { tags -> tags.name }
                    Timber.tag(TAG).d("Tags state -> $tagTitles")
                    if (requestState.value?.tags != tagTitles) {
                        Log.d(TAG, "Tag titles has changed")
                        shouldUpdateButtonBeEnabled.value = true
                    } else {
                        Log.d(TAG, "Tag titles hasn't changed")
                        shouldUpdateButtonBeEnabled.value = false
                    }
                }
            }
        }

    }

    private fun setData(
        profileDetails: MerchantProfileResponse,
        shouldUpdateImages: Boolean = false
    ) {

        Timber.d("Merchant details -> $profileDetails")

        mBinding.apply {

            val nameInitials =
                ImageUtils().getNameInitialsImage(profileDetails.name.getNotAvailableIfEmptyOrNull())

            if (shouldUpdateImages) {
                if (profileDetails.logo != null) {
                    Glide.with(this@MerchantProfileActivity)
                        .load(profileDetails.logo)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.ic_insert_photo)
                        .error(nameInitials)
                        .into(ivLogo)
                } else {
                    ivLogo.setImageDrawable(nameInitials)
                }
            }

            if (shouldUpdateImages) {
                if (profileDetails.mainImage != null) {
                    Glide.with(this@MerchantProfileActivity)
                        .load(profileDetails.mainImage)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.ic_insert_photo)
                        .error(R.drawable.ic_insert_photo)
                        .into(ivCoverPhoto)
                } else {
                    // Do nothing
                }
            }

            tilBusinessName.editText?.setText(profileDetails.name)
            tilAddress.editText?.setText(profileDetails.address)
            tilZipCode.editText?.setText(profileDetails.zipCode)
            tilDescription.editText?.setText(profileDetails.description)
            tilPromotionalText.editText?.setText(profileDetails.promotionalText)
            countryCodePicker.fullNumber = profileDetails.mobileNo
            mSelectedCountryCode = countryCodePicker.selectedCountryNameCode
            Timber.d("Selected country code -> $mSelectedCountryCode")
            val selectedCountry = mAvailableCountries.find {
                it?.iso == countryCodePicker.selectedCountryNameCode
            }
            Timber.d("Get selected country -> ${countryCodePicker.selectedCountryNameCode}")
            mBinding.spCountries.setSelection(mAdapter.getPosition(selectedCountry))

            tilEmail.editText?.setText(profileDetails.email)
            tilHeadOfficeId.editText?.setText(profileDetails.headOfficeDetails?.uniqueOfficeId)
            if (!profileDetails.website.isNullOrBlank()) {
                val website = profileDetails.website.replace("https://", "")
                tilWebsite.editText?.setText(website)
            }
        }

        tagsResponseObserver(profileDetails = profileDetails)
        btnAddListener()
        initializeWatchers(it = profileDetails)
        initializeTextWatchers()

    }

    private fun tagsResponseObserver(profileDetails: MerchantProfileResponse) {
        profileDetails.tagsDetails?.forEachIndexed { index, tag ->

            if (index == 0) {
                clearTags() // Let's clear tags at first to avoid redundancy
            }

            if (tag != null) {
                addToSelectedTags(tag)
                val chip = getChip(tag)
                mBinding.cgTags.addView(chip)
                Log.d(TAG, "setData: After adding tags from API -> $selectedTags")
            }

        }
    }

    private fun clearTags() {
        selectedTags.clear()
        mBinding.cgTags.removeAllViews()
    }

    private fun btnAddListener() {
        mBinding.btnAdd.setOnClickListener {
            val tag = mBinding.atvTags.text.toString()
            if (tag.isBlank().not()) {
                updateSelectedTags(tag = tag)
            }
        }
    }

    fun updateSelectedTags(tag: String) {

        Timber.d("Selected tags -> $selectedTags")
        if (selectedTags.size >= 10) {
            showSnackbar(
                rootView = mBinding.root,
                message = R.string.you_can_send_only_10_tags_please_choose_the_tags_that_fits_your_business
            )
            return
        }

        val tagTitles = selectedTags.map { it.name }
        val tagAlreadyExists = tagTitles.contains(tag)
        if (tagAlreadyExists) {
            return
        }

        val tagDetail = TagsDetail(name = tag, tagId = selectedTags.lastIndex.toString())
        addToSelectedTags(tagDetail)

        val chip = getChip(tag = tagDetail)
        mBinding.cgTags.addView(chip)
        mBinding.atvTags.text?.clear()
        Log.d(TAG, "all tags -> $selectedTags")

    }

    private fun addToSelectedTags(tag: TagsDetail) {
        selectedTags.add(tag)
//        tagsState.value = ArrayList<TagsDetail>(selectedTags)
        tagsState.value = selectedTags.map { it.copy() }
    }

    private fun getChip(tag: TagsDetail): Chip {
        val chip = Chip(this)
        chip.setChipBackgroundColorResource(R.color.graySurface)
        chip.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        chip.setCloseIconResource(R.drawable.ic_close)
        chip.apply {

            Log.d(TAG, "Tag -> $tag")
            Log.d(TAG, "Last index -> ${selectedTags.lastIndex}")

            id = selectedTags.lastIndex
            text = tag.name
            isCloseIconVisible = true
            setOnCloseIconClickListener { chipToBeRemoved ->

                val categoryToBeRemoved = selectedTags.find { chipText ->
                    val materialChip = chipToBeRemoved as Chip
                    val materialChipText = materialChip.text.toString()
                    materialChipText.equals(chipText.name, true)
                }

                Log.d(TAG, "Chips to be removed -> $categoryToBeRemoved")
                removeFromSelectedTags(categoryToBeRemoved)

                Log.d(TAG, "After removing a chip -> $selectedTags")
                mBinding.cgTags.removeView(chipToBeRemoved)

            }

        }
        return chip
    }

    private fun removeFromSelectedTags(categoryToBeRemoved: TagsDetail?) {
        selectedTags.remove(categoryToBeRemoved)
//        tagsState.value = ArrayList<TagsDetail>(selectedTags)
        tagsState.value = selectedTags.map { it.copy() }
        Timber.d("Remaining tags -> $selectedTags")
    }

    private fun initOnClick() {
        binding?.btnEdit?.setOnClickListener { openPlacesAutoComplete() }
        btnUpdateListener()
    }

    private fun btnUpdateListener() {
        mBinding.updateBtn.setOnClickListener {

            clearErrorsInInputFields()

            val businessName = mBinding.tilBusinessName.editText?.text
            if (businessName.isNullOrEmpty()) {
                mBinding.tilBusinessName.error = "Please enter business name"
                return@setOnClickListener
            }

            val address = binding?.tilAddress?.editText?.text.toString()
            if (address.isEmpty()) {
                mBinding.tilAddress.error = "Please enter address"
                return@setOnClickListener
            }

            val postalCode = mBinding.tilZipCode.editText?.text.toString()
            if (postalCode.isBlank()) {
                mBinding.tilZipCode.error = getString(R.string.please_enter_zip_code)
                return@setOnClickListener
            }

            val phone = mBinding.phoneET.text.toString()
//            val phone = mBinding.countryCodePicker.fullNumberWithPlus
            Timber.d("Full number with plus-> $phone")
            if (phone.isEmpty()) {
                mBinding.phoneET.error = "Please enter phone number"
                return@setOnClickListener
            }

            val websiteUrl =
                mBinding.tilWebsite.prefixText.toString() + mBinding.tilWebsite.editText?.text
            if (!websiteUrl.isNullOrBlank()) {
                val isNotValidUrl = !ValidationUtils().isValidUrl(urlString = websiteUrl.toString())
                if (isNotValidUrl) {
                    mBinding.tilWebsite.error = "Please enter a valid url"
                    return@setOnClickListener
                }
            }

            val selectedCountry = mBinding.spCountries.selectedItem as CountryResponse.Country?
            validatePostalCode(
                selectedCountry = selectedCountry,
                postalCode = postalCode,
                onValid = {
                    val request = getRequest()
                    val token = sessionManager.fetchAuthToken().toString()
                    Timber.d("Request -> $request")
                    updateMerchantDetails(token, request)
                })

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
                    customDialogClass.hide()
                    Timber.d("Success -> ${it.value}")
                    if (it.value.results.isNullOrEmpty()) {
                        mBinding.tilZipCode.error = "No postal code found for selected country"
                    } else {
                        onValid()
                    }
                }

            }

        }
    }

    private fun clearErrorsInInputFields() {
        mBinding.apply {
            tilBusinessName.error = ""
            tilAddress.error = ""
            tilZipCode.error = ""
            phoneET.error = null
        }
    }

    private fun updateMerchantDetails(
        token: String,
        request: HashMap<String, Any?>
    ) {
        viewModel.updateMerchantDetails(
            token = token,
            params = request,
            merchantShopID = merchantProfileDetails?.id ?: -1
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
                    merchantProfileDetails = resource.value
                    setData(profileDetails = resource.value)

                    sessionManager.saveMerchantDetails(merchantDetails = resource.value)
                    val successDialog = SuccessDialog.getInstance(
                        message = "Profile has been updated",
                        afterDismissClicked = object : AfterDismissalListener {
                            override fun afterDismissed() {
                                finish()
                            }
                        })
                    successDialog.show(supportFragmentManager, successDialog.tag)

                    Timber.d("Profile update complete")

//                    updateMerchantLogoUsingMultiPart()


                }
            }

        }
    }

    private fun updateMerchantLogoUsingMultiPart() {

//        val logo = viewModel.logoFile
//
//        if (logo != null) {
//            Timber.d("Let's update logo")
//            updateMerchantLogo(token, logo)
//        } else {
//            sessionManager.saveMerchantDetails(merchantDetails = resource.value)
//            val successDialog =
//                SuccessDialog.getInstance(
//                    message = "Profile has been updated",
//                    afterDismissClicked = object : AfterDismissalListener {
//                        override fun afterDismissed() {
//                            finish()
//                        }
//                    })
//            successDialog.show(supportFragmentManager, successDialog.tag)
//        }

    }

    private fun updateMerchantLogo(token: String, logo: File) {
        viewModel.updateMerchantLogo(
            token = token,
            merchantShopId = merchantProfileDetails?.id.getMinusOneIfNull(),
            logo = logo
        ).observe(this) {

            when (it) {
                is Resource.Failure -> {
                    showToast("Couldn't update logo")
                    customDialogClass.hide()
                }

                Resource.Loading -> {
                    customDialogClass.show()
                }

                is Resource.Success -> {
                    customDialogClass.hide()
                    setData(profileDetails = it.value, shouldUpdateImages = true)
                    sessionManager.saveMerchantDetails(merchantDetails = it.value)
                    val successDialog =
                        SuccessDialog.getInstance(
                            message = "Profile has been updated",
                            afterDismissClicked = object : AfterDismissalListener {
                                override fun afterDismissed() {
                                    finish()
                                }
                            })
                    successDialog.show(supportFragmentManager, successDialog.tag)
                }
            }

        }
    }

    private fun getRequest(): HashMap<String, Any?> {
        val params = HashMap<String, Any?>()

        val phone = binding?.phoneET?.text.toString()
        val sanitizedPhoneNumber = PhoneUtils().sanitizePhoneNumber(phoneNumber = phone)
        val countryCode = mBinding.countryCodePicker.selectedCountryCode
        val phoneNumberWithCountryCode = "+$countryCode-$sanitizedPhoneNumber"

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

        params["name"] = mBinding.tilBusinessName.editText?.text.toString()
        params["head_office_id"] = mBinding.tilHeadOfficeId.editText?.text.toString()
        params["description"] = mBinding.tilDescription.editText?.text.toString()
        params["address"] = address
        val selectedCountry = mBinding.spCountries.selectedItem as CountryResponse.Country?
        selectedCountry?.let { params["country"] = it.iso }
//        params["mobile_no"] = phoneNumberWithCountryCode
        params["mobile_no"] = mBinding.countryCodePicker.fullNumberWithPlus

        val websiteUrl = mBinding.tilWebsite.editText?.text
        if (!websiteUrl.isNullOrBlank()) {
            val prefixText = mBinding.tilWebsite.prefixText.toString()
            val fullWebsiteUrl = prefixText + websiteUrl.toString().getEmptyIfNull()
            Timber.d("Full website url -> $fullWebsiteUrl")
            params["website"] = fullWebsiteUrl
        }

        params["zip_code"] = binding?.tilZipCode?.editText?.text.toString()
        params["promotional_text"] = mBinding.tilPromotionalText.editText?.text.toString()
        params["longitude"] =
            longitude?.roundOffToDecimalDigits(n = AppConstants.LAT_LON_DECIMAL_DIGITS).toString()
        params["latitude"] =
            latitude?.roundOffToDecimalDigits(n = AppConstants.LAT_LON_DECIMAL_DIGITS).toString()
        params["tags"] = selectedTags.map { it.name }
        val logo = viewModel.logoFile
        if (logo != null) {
            params["logo"] = ImageUtils().imageFileToBase64(file = logo)
        }

        val coverPhoto = viewModel.coverPhotoFile
        if (coverPhoto != null) {
            params["main_image"] = ImageUtils().imageFileToBase64(file = coverPhoto)
        }

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
            if (resultCode == RESULT_CANCELED) {
                return
            }
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        place = Autocomplete.getPlaceFromIntent(data)
                        mBinding.tilAddress.editText?.setText(place.address)
//                        val postalCode = LocationUtils().getPostalCode(context = this, data = data)
//                        mBinding.tilZipCode.editText?.setText(postalCode)

                        // Get the postal code from the Place object
                        val postalCode = getPostalCodeFromPlace(place)
                        mBinding.tilZipCode.editText?.setText(postalCode)
                        Log.d(TAG, "Postal Code: $postalCode")

                    }
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i("TAG", status.statusMessage ?: "")
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

    private fun getPostalCodeFromPlace(place: Place): String? {
        val addressComponents: List<AddressComponent>? = place.addressComponents?.asList()

        // Find the postal code in the address components
        addressComponents?.forEach { component ->
            if (component.types.contains("postal_code") == true) {
                return component.name
            }
        }
        return null
    }

}