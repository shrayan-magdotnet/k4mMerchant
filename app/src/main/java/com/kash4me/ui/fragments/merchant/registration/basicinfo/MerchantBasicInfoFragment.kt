package com.kash4me.ui.fragments.merchant.registration.basicinfo

import android.Manifest
import android.annotation.SuppressLint
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ScrollView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.kash4me.R
import com.kash4me.data.models.BasicInfo
import com.kash4me.data.models.BusinessInfo
import com.kash4me.data.models.BusinessInfoResponse
import com.kash4me.data.models.HeadOfficeDetails
import com.kash4me.data.models.TagDetail
import com.kash4me.data.models.merchant.profile.MerchantProfileResponse
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateResponse
import com.kash4me.data.models.user.CountryResponse
import com.kash4me.data.models.user.TagResponse
import com.kash4me.databinding.FragmentMerchantBasicInfoBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.PostMerchantDetailsRepository
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository
import com.kash4me.ui.activity.merchant.MerchantRegistrationActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.CurrentLocation
import com.kash4me.utils.FileUtils
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.LocationPermissionHelper
import com.kash4me.utils.LocationUtils
import com.kash4me.utils.RequestCodes
import com.kash4me.utils.SessionManager
import com.kash4me.utils.ValidationUtils
import com.kash4me.utils.convertDpToPixel
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.clear
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.roundOffToDecimalDigits
import com.kash4me.utils.extensions.showSnackbar
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import com.permissionx.guolindev.PermissionX
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt


class MerchantBasicInfoFragment

    : Fragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    companion object {

        private const val TAG = "MerchantBasicInfo"

        fun newInstance() = MerchantBasicInfoFragment()
    }

    private val mAvailableCountries = arrayListOf<CountryResponse.Country?>()

    private val viewModel: MerchantBasicInfoViewModel by lazy { initViewModel() }

    private fun initViewModel(): MerchantBasicInfoViewModel {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(requireContext().applicationContext),
                NotFoundInterceptor()
            )
        val repository = PostMerchantDetailsRepository(apiInterface)
        val userRepository = UserRepository(apiInterface)
        val userDetailsRepository = UserDetailsRepository(apiInterface)
        return ViewModelProvider(
            this,
            MerchantBasicInfoViewModelFactory(repository, userRepository, userDetailsRepository)
        )[MerchantBasicInfoViewModel::class.java]
    }

    private val mCountriesAdapter by lazy {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            arrayListOf<CountryResponse.Country>()
        )
    }

    private var _binding: FragmentMerchantBasicInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }

    private val autoCompleteRequestCode = 1

    private lateinit var place: Place

    lateinit var view: ScrollView

    private val atvTagsAdapter by lazy {
        val tags = arrayListOf<TagResponse.Result>()
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, tags)
    }
    private val selectedTags = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMerchantBasicInfoBinding.inflate(inflater, container, false)
        view = binding.root

        Timber.d("Postal code -> ${CurrentLocation.postalCode}")
        binding.postalCodeET.editText?.setText(CurrentLocation.postalCode)

        sessionManager = SessionManager(requireActivity().applicationContext)

        binding.btnSearchAddress.setOnClickListener { openPlacesAutoComplete() }

        binding.countryCodePicker.registerCarrierNumberEditText(binding.phoneET)

        binding.atvTags.apply {
            setAdapter(atvTagsAdapter)
            setDropDownBackgroundResource(R.color.white)
            setOnFocusChangeListener { _, _ -> showDropDown() }
        }
        getAvailableTags()

        binding.btnAdd.setOnClickListener {
            //            val tag = mBinding.tagsET.editText?.text.toString()
            val tag = binding.atvTags.text.toString()
            if (tag.isBlank().not()) {
                updateSelectedTags(tag = tag)
            }
        }

        btnNextListener()

        binding.cvLogo.setOnClickListener { showCameraOrGalleryDialog(view = binding.cvLogo) }
        binding.cvCoverPhoto.setOnClickListener { showCameraOrGalleryDialog(view = binding.cvCoverPhoto) }

        return view

    }

    private fun btnNextListener() {
        binding.nextBtn.setOnClickListener {

            val businessName = binding.businessNameET.editText?.text.toString().trim()
            val businessAddress = binding.addressET.editText?.text.toString().trim()
            val businessPhone = binding.phoneET.text.toString().trim()
            val businessPostalCode = binding.postalCodeET.editText?.text.toString().trim()

            val contactPersonName = binding.userNameET.editText?.text.toString().trim()

            var isValid = true

            if (contactPersonName.isEmpty()) {
                binding.userNameET.error = "Please enter a valid name"
                isValid = false
            } else {
                binding.userNameET.error = ""
            }

            if (businessName.isEmpty()) {
                binding.businessNameET.error = "Please enter a valid business name"
                isValid = false
            } else {
                binding.businessNameET.error = ""
            }

            if (businessAddress.isEmpty()) {
                binding.addressET.error = "Please enter a valid address"
                isValid = false
            } else {
                binding.addressET.error = ""
            }

            if (businessPhone.isEmpty()) {
                binding.phoneET.error = "Please enter a valid phone"
                isValid = false
            } else {
                binding.phoneET.error = null
            }

            if (businessPostalCode.isEmpty()) {
                binding.postalCodeET.error = "Please enter a valid postal code"
                isValid = false
            } else {
                binding.postalCodeET.error = ""
            }

            val website =
                binding.tilWebsite.prefixText.toString() + binding.tilWebsite.editText?.text
            if (!website.isNullOrBlank()) {
                val isNotValidUrl = !ValidationUtils().isValidUrl(urlString = website.toString())
                if (isNotValidUrl) {
                    binding.tilWebsite.error = "Please enter a valid url"
                    return@setOnClickListener
                }
            }

            var address: String? = businessAddress
            if (!::place.isInitialized) {
                Timber.d("Google places has been initialized")
                try {
                    val decodedAddress = LocationUtils().getAddress(
                        context = requireContext(),
                        zipCode = businessPostalCode
                    )
                    viewModel.latitude = decodedAddress?.latitude
                    viewModel.longitude = decodedAddress?.longitude
                } catch (e: Exception) {
                    Timber.d("Exception caught: ${e.message}")
                    showToast(message = e.message ?: "Couldn't geocode zipcode")
                    viewModel.latitude = null
                    viewModel.longitude = null
                }
            } else {
                address = place.address
                viewModel.latitude = place.latLng?.latitude
                viewModel.longitude = place.latLng?.longitude
            }

            if (!isValid) {
                return@setOnClickListener
            }

            val userParams = HashMap<String, Any>()
            userParams["name"] = contactPersonName

            val businessInfo = HashMap<String, String?>()
            businessInfo["name"] = businessName
            val headOfficeId = binding.headOfficeET.editText?.text.toString()
            if (headOfficeId.isNotBlank()) {
                businessInfo["head_office_id"] = binding.headOfficeET.editText?.text.toString()
            }
            businessInfo["address"] = address
            businessInfo["mobile_no"] = binding.countryCodePicker.fullNumberWithPlus
            val websiteUrl = binding.tilWebsite.editText?.text
            if (!websiteUrl.isNullOrBlank()) {
                val prefixText = binding.tilWebsite.prefixText.toString()
                val fullWebsiteUrl = prefixText + websiteUrl.toString().getEmptyIfNull()
                Timber.d("Full website url -> $fullWebsiteUrl")
                businessInfo["website"] = fullWebsiteUrl
            }
            businessInfo["zip_code"] = businessPostalCode
            businessInfo["description"] = binding.tvDescription.editText?.text.toString()
            businessInfo["promotional_text"] = binding.etPromotionalText.editText?.text.toString()
            businessInfo["country"] = viewModel.selectedCountry?.iso
            businessInfo["latitude"] = viewModel.latitude
                ?.roundOffToDecimalDigits(AppConstants.LAT_LON_DECIMAL_DIGITS).toString()
            businessInfo["longitude"] = viewModel.longitude
                ?.roundOffToDecimalDigits(AppConstants.LAT_LON_DECIMAL_DIGITS).toString()

            val finalParam = HashMap<String, Any>()
            finalParam["basic_info"] = userParams
//            finalParam["business_info"] = businessInfo
            finalParam["tags"] = selectedTags

            Timber.d("Request -> $finalParam")

            val token = sessionManager.fetchAuthToken().toString()
            Timber.d("Ready to hit API")

            val merchantInfo = sessionManager.fetchMerchantBasicInfo()
            if (merchantInfo != null) {
                validatePostalCode(
                    selectedCountry = binding.spCountries.selectedItem as CountryResponse.Country?,
                    postalCode = binding.postalCodeET.editText?.text.toString(),
                    onValid = {

                        progressDialog.show()

                        Timber.d("Let's call patch API")
                        val merchantShopId =
                            sessionManager.fetchMerchantBasicInfo()?.business_info?.shop_id.getZeroIfNull()
                        val patchParams = HashMap<String, Any?>()
                        patchParams["name"] = businessName
                        patchParams["person_name"] = contactPersonName
                        patchParams["head_office_id"] =
                            binding.headOfficeET.editText?.text.toString()
                        patchParams["promotional_text"] =
                            binding.etPromotionalText.editText?.text.toString()
                        patchParams["description"] = binding.tvDescription.editText?.text.toString()
                        val websiteUrl = binding.tilWebsite.editText?.text
                        if (!websiteUrl.isNullOrBlank()) {
                            val prefixText = binding.tilWebsite.prefixText.toString()
                            val fullWebsiteUrl = prefixText + websiteUrl.toString().getEmptyIfNull()
                            Timber.d("Full website url -> $fullWebsiteUrl")
                            patchParams["website"] = fullWebsiteUrl
                        }
                        patchParams["address"] = address
                        patchParams["mobile_no"] = binding.countryCodePicker.fullNumberWithPlus
                        patchParams["zip_code"] = businessPostalCode
                        patchParams["tags"] = selectedTags
                        val logo = viewModel.logoFile
                        if (logo != null) {
                            patchParams["logo"] = ImageUtils().imageFileToBase64(file = logo)
                        }
                        val coverPhotoFile = viewModel.coverPhotoFile
                        if (coverPhotoFile != null) {
                            patchParams["main_image"] =
                                ImageUtils().imageFileToBase64(file = coverPhotoFile)
                        }

                        progressDialog.hide()

                        fetchTimezone(
                            onSuccess = {
                                patchParams["latitude"] = viewModel.latitude
                                    ?.roundOffToDecimalDigits(AppConstants.LAT_LON_DECIMAL_DIGITS)
                                    .toString()
                                patchParams["longitude"] = viewModel.longitude
                                    ?.roundOffToDecimalDigits(AppConstants.LAT_LON_DECIMAL_DIGITS)
                                    .toString()
                                patchParams["timezone"] =
                                    viewModel.selectedTimezone.getEmptyIfNull()

                                Timber.d("Let's proceed to making request")
                                Timber.d("Request -> $patchParams")

                                progressDialog.show()
                                updateMerchantDetails(token, patchParams, merchantShopId)

                            },
                            onFailure = {
                                val errorDialog = ErrorDialog.getInstance(
                                    message = "Could not get timezone details according to the provided address information"
                                )
                                errorDialog.show(
                                    activity?.supportFragmentManager!!, errorDialog.tag
                                )
                            }
                        )

                    }
                )
            } else {
                validatePostalCode(
                    selectedCountry = binding.spCountries.selectedItem as CountryResponse.Country?,
                    postalCode = binding.postalCodeET.editText?.text.toString(),
                    onValid = {
                        fetchTimezone(
                            onSuccess = {

                                businessInfo["latitude"] = viewModel.latitude
                                    ?.roundOffToDecimalDigits(AppConstants.LAT_LON_DECIMAL_DIGITS)
                                    .toString()
                                businessInfo["longitude"] = viewModel.longitude
                                    ?.roundOffToDecimalDigits(AppConstants.LAT_LON_DECIMAL_DIGITS)
                                    .toString()
                                businessInfo["timezone"] =
                                    viewModel.selectedTimezone.getEmptyIfNull()
                                val logo = viewModel.logoFile
                                if (logo != null) {
                                    businessInfo["logo"] =
                                        ImageUtils().imageFileToBase64(file = logo)
                                }
                                val coverPhotoFile = viewModel.coverPhotoFile
                                if (coverPhotoFile != null) {
                                    businessInfo["main_image"] =
                                        ImageUtils().imageFileToBase64(file = coverPhotoFile)
                                }

                                finalParam["business_info"] = businessInfo

                                Timber.d("Let's proceed to making request")
                                Timber.d("Request -> $finalParam")

                                viewModel.postBusinessUserDetails(token, finalParam)
                                    .observe(viewLifecycleOwner) {

                                        when (it) {
                                            is Resource.Failure -> {
                                                progressDialog.hide()
                                                val errorDialog =
                                                    ErrorDialog.getInstance(message = it.errorMsg)
                                                errorDialog.show(
                                                    activity?.supportFragmentManager!!,
                                                    errorDialog.tag
                                                )
                                            }

                                            Resource.Loading -> {
                                                progressDialog.show()
                                            }

                                            is Resource.Success -> {
                                                progressDialog.hide()

//                                                val logo = viewModel.logoFile
//                                                Timber.d("Logo -> $logo")
//                                                updateMerchantLogoUsingMultipart(logo, token, it)

                                                Timber.d("Post response -> ${it.value}")
                                                selectedTags.clear()

                                                sessionManager.saveMerchantBasicInfo(
                                                    businessInfoResponse = it.value
                                                )
                                                sessionManager.saveMerchantId(it.value.business_info.shop_id)
                                                sessionManager.saveUserProfile(true)
                                                Navigation.findNavController(view)
                                                    .navigate(R.id.imageUploadFragment)

                                            }
                                        }

                                    }

                            },

                            onFailure = {
                                val errorDialog = ErrorDialog.getInstance(
                                    message = "Could not get timezone details according to the provided address information"
                                )
                                errorDialog.show(
                                    activity?.supportFragmentManager!!, errorDialog.tag
                                )
                            }

                        )
                    }
                )
            }

            viewModel.errorMessage.observe(viewLifecycleOwner) {
                val errorDialog = ErrorDialog.getInstance(message = it)
                errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                progressDialog.hide()
            }

        }
    }

    private fun updateMerchantLogoUsingMultipart(
        logo: File?,
        token: String,
        it: Resource.Success<BusinessInfoResponse>
    ) {
        if (logo != null) {
            viewModel.updateMerchantLogo(
                token = token,
                merchantShopId = it.value.business_info.shop_id.getZeroIfNull(),
                logo = logo
            )
                .observe(viewLifecycleOwner) { logoUploadResponse ->
                    when (logoUploadResponse) {
                        is Resource.Failure -> {
                            progressDialog.hide()
                            showToast("Couldn't upload logo")
                        }

                        Resource.Loading -> {
                            // Do nothing since progress is being shown already
                            progressDialog.show()
                        }

                        is Resource.Success -> {
                            Timber.d("Logo upload response -> ${logoUploadResponse.value}")
                            selectedTags.clear()
                            val businessInfoAfterUploadingLogo =
                                getBusinessInfoResponseAfterUploadingLogo(
                                    it = logoUploadResponse.value,
                                    timezone = it.value.business_info.timezone
                                )
                            sessionManager.saveLogo(logo = logoUploadResponse.value.logo.getEmptyIfNull())
                            sessionManager.saveMerchantBasicInfo(
                                businessInfoResponse = businessInfoAfterUploadingLogo
                            )
                            sessionManager.saveMerchantId(
                                businessInfoAfterUploadingLogo.business_info.shop_id
                            )
                            sessionManager.saveUserProfile(
                                true
                            )
                            progressDialog.hide()
                            Navigation.findNavController(
                                view
                            ).navigate(R.id.imageUploadFragment)
                        }
                    }
                }
        } else {
            selectedTags.clear()

            sessionManager.saveMerchantBasicInfo(
                businessInfoResponse = it.value
            )
            sessionManager.saveMerchantId(it.value.business_info.shop_id)
            sessionManager.saveUserProfile(true)
            Navigation.findNavController(view)
                .navigate(R.id.imageUploadFragment)
        }
    }

    fun showCameraOrGalleryDialog(view: MaterialCardView) {
        // check get current api version
        var lPerm = Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lPerm = Manifest.permission.READ_MEDIA_VIDEO
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                lPerm
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
            val alertDialog = AlertDialog.Builder(requireContext()).create()
            alertDialog.setMessage("Please choose!")
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera") { dialog, _ ->
                dialog.dismiss()
                dispatchTakePictureIntent(view = view)
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Gallery") { dialog, _ ->
                dialog.dismiss()

                val sizeLimitMessage = getAttachmentSizeLimitInfoMessage()

                AlertDialog.Builder(requireContext())
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

            val paddingHorizontal = 16f.convertDpToPixel(requireContext()).roundToInt()
            val paddingVertical = 8f.convertDpToPixel(requireContext()).roundToInt()

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
                requireContext(),
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
                if (view == binding.cvLogo) {
                    Timber.d("Logo is clicked")
                    captureLogoLauncher.launch(takePictureIntent)
                } else if (view == binding.cvCoverPhoto) {
                    Timber.d("Cover Photo is clicked")
                    captureCoverPhotoLauncher.launch(takePictureIntent)
                }
            } catch (e: ActivityNotFoundException) {
                // display error state to the user
            }
        }
    }

    private val captureLogoLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            val imageBitmap = it.data?.extras?.get("data") as Bitmap
            val logo = saveBitmapAsFile(context = requireContext(), bitmap = imageBitmap)
//            viewModel.logoFile = logo

            val maxWidth = 256 // Maximum desired width
            val maxHeight = 256 // Maximum desired height

            // Resize the bitmap
            val resizedBitmap =
                Bitmap.createScaledBitmap(imageBitmap, maxWidth, maxHeight, true)

            viewModel.logoFile =
                saveBitmapAsFile(context = requireContext(), bitmap = resizedBitmap)

            Glide.with(this)
                .load(resizedBitmap)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.ivLogo)

        }

    private val captureCoverPhotoLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            val imageBitmap = it.data?.extras?.get("data") as Bitmap
            val logo = saveBitmapAsFile(context = requireContext(), bitmap = imageBitmap)
//            viewModel.logoFile = logo

            val maxWidth = 256 // Maximum desired width
            val maxHeight = 256 // Maximum desired height

            // Resize the bitmap
            val resizedBitmap =
                Bitmap.createScaledBitmap(imageBitmap, maxWidth, maxHeight, true)

            viewModel.coverPhotoFile =
                saveBitmapAsFile(context = requireContext(), bitmap = resizedBitmap)

            Glide.with(this)
                .load(resizedBitmap)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.ivCoverPhoto)

        }

    // Save the bitmap as an image file
    private fun saveBitmapAsFile(context: Context, bitmap: Bitmap): File? {
        // Get the directory for storing the image file
        val directory = context.filesDir
        // Create a unique file name
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        // Create the file object
        val file = File(directory, fileName)

        try {
            // Create a file output stream
            val outputStream = FileOutputStream(file)
            // Compress the bitmap to JPEG format with 100% quality (optional)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            // Flush and close the output stream
            outputStream.flush()
            outputStream.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun pickImageFromGallery(view: MaterialCardView) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"

        if (view == binding.cvLogo) {
            Timber.d("Pick Logo Launcher")
            pickLogoLauncher.launch(intent)
        } else if (view == binding.cvCoverPhoto) {
            Timber.d("Pick Cover Photo Launcher")
            pickCoverPhotoLauncher.launch(intent)
        }

    }

    private val pickLogoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                // Handle the selected image URI here
                if (uri != null) {

                    val file = FileUtils().createFileFromUri(context = requireContext(), uri = uri)

                    if (file != null) {

                        val sizeInMb = FileUtils().getFileSizeInMB(file = file)
                        Timber.d("Image size in MB -> $sizeInMb")
                        val maxImageSize = AppConstants.IMAGE_SIZE_LIMIT
                        if (sizeInMb > maxImageSize) {
                            val errorDialog = ErrorDialog.getInstance(
                                message = "Selected image is greater than maximum image size"
                            )
                            errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                            return@registerForActivityResult
                        }

                    }

                    processSelectedImageForLogo(context = requireContext(), imageUri = uri)
                }
            }
        }

    private val pickCoverPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                // Handle the selected image URI here
                if (uri != null) {

                    val file = FileUtils().createFileFromUri(context = requireContext(), uri = uri)

                    if (file != null) {

                        val sizeInMb = FileUtils().getFileSizeInMB(file = file)
                        Timber.d("Image size in MB -> $sizeInMb")
                        val maxImageSize = AppConstants.IMAGE_SIZE_LIMIT
                        if (sizeInMb > maxImageSize) {
                            val errorDialog = ErrorDialog.getInstance(
                                message = "Selected image is greater than maximum image size"
                            )
                            errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                            return@registerForActivityResult
                        }

                    }

                    processSelectedImageForCoverPhoto(context = requireContext(), imageUri = uri)
                }
            }
        }

    // Process the selected image
    private fun processSelectedImageForLogo(context: Context, imageUri: Uri) {
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
                val resizedBitmap =
                    Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true)

                viewModel.logoFile = saveBitmapAsFile(context = context, bitmap = resizedBitmap)

                Glide.with(this)
                    .load(resizedBitmap)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(binding.ivLogo)

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
                val resizedBitmap =
                    Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true)

                viewModel.coverPhotoFile =
                    saveBitmapAsFile(context = context, bitmap = resizedBitmap)

                Glide.with(this)
                    .load(resizedBitmap)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(binding.ivCoverPhoto)

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun fetchTimezone(onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModel.getTimezone(
            latitude = viewModel.latitude.getZeroIfNull(),
            longitude = viewModel.longitude.getZeroIfNull()
        ).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    progressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                    onFailure()
                }

                Resource.Loading -> {
                    progressDialog.show()
                }

                is Resource.Success -> {
                    progressDialog.hide()
                    Timber.d("Success -> ${it.value}")
                    viewModel.selectedTimezone = it.value.timeZoneId
                    onSuccess()
                }
            }

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
        ).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    Timber.d("Failure -> ${it.errorMsg}")
                    progressDialog.hide()
                    binding.postalCodeET.error =
                        "An error occurred while trying to validate postal code"
                    binding.postalCodeET.requestFocus()
                }

                Resource.Loading -> {
                    Timber.d("Loading ")
                    progressDialog.show()
                }

                is Resource.Success -> {
                    progressDialog.hide()
                    Timber.d("Success -> ${it.value}")
                    if (it.value.results.isNullOrEmpty()) {
                        binding.postalCodeET.error = "No postal code found for selected country"
                        binding.postalCodeET.requestFocus()
                    } else {
                        viewModel.latitude = it.value.results.getOrNull(0)?.geometry?.location?.lat
                        viewModel.longitude = it.value.results.getOrNull(0)?.geometry?.location?.lng
                        onValid()
                    }
                }

            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.spCountries.adapter = mCountriesAdapter

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the necessary data to the bundle
        outState.putParcelable("selected_country", viewModel.selectedCountry)
        outState.putString("selected_timezone", viewModel.selectedTimezone)
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
                        mCountriesAdapter.clear()
                        mCountriesAdapter.addAll(countries)
                        mAvailableCountries.clear()
                        mAvailableCountries.addAll(countries)
                    }
                    val countryCodes = it.value.countryLists?.map { country -> country?.iso }
                    Timber.d("Country codes -> $countryCodes")
                    Timber.d("Country codes -> ${countryCodes?.joinToString()}")
                    if (countryCodes != null) {
                        binding.countryCodePicker.setCustomMasterCountries(
                            countryCodes.joinToString(separator = ",")
                        )
                    }
                    viewModel.selectedCountry = it.value.countryLists?.getOrNull(0)

                    proceedToDetectingCountry()

                }

            }

        }
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
                val latitude = location.latitude
                val longitude = location.longitude
                // Now you have the latitude and longitude values, do whatever you need with them.
                Timber.d("Detected Latitude: $latitude | Longitude: $longitude")
                val country = LocationUtils().detectCountry(
                    context = requireContext(), latitude = latitude, longitude = longitude
                )
                val countryCode = mAvailableCountries.find { country?.code == it?.iso }
                Timber.d("Is the current location available in countries option? -> $countryCode")

                if (countryCode != null) {
                    viewModel.selectedCountry = countryCode
                    val indexOfCountryCode = mAvailableCountries.indexOf(countryCode)
                    Timber.d("Position of $countryCode -> $indexOfCountryCode")
                    binding.spCountries.setSelection(indexOfCountryCode)
                    binding.countryCodePicker.setCountryForNameCode(countryCode.iso)
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


    private fun updateMerchantDetails(
        token: String,
        params: HashMap<String, Any?>,
        merchantShopId: Int
    ) {
        viewModel.updateMerchantDetails(
            token = token,
            params = params,
            merchantShopID = merchantShopId
        ).observe(viewLifecycleOwner) {
            selectedTags.clear()
            val businessInfoResponse = getBusinessInfoResponseForSaving(it)
            sessionManager.saveMerchantBasicInfo(businessInfoResponse = businessInfoResponse)
            sessionManager.saveMerchantId(it.id.getZeroIfNull())
            sessionManager.saveUserProfile(true)
            progressDialog.hide()
            Navigation.findNavController(view).navigate(R.id.imageUploadFragment)
//            updateLogoUsingMultipart(token, it, businessInfoResponse)
        }
    }

    private fun updateLogoUsingMultipart(
        token: String,
        it: MerchantProfileUpdateResponse,
        businessInfoResponse: BusinessInfoResponse
    ) {
        val logo = viewModel.logoFile
        Timber.d("Logo -> $logo")
        if (logo != null) {
            viewModel.updateMerchantLogo(
                token = token,
                merchantShopId = it.id.getZeroIfNull(),
                logo = logo
            ).observe(viewLifecycleOwner) { logoUploadResponse ->
                when (logoUploadResponse) {
                    is Resource.Failure -> {
                        progressDialog.hide()
                        showToast("Couldn't upload logo")
                    }

                    Resource.Loading -> {
                        // Do nothing since progress is being shown already
                    }

                    is Resource.Success -> {
                        Timber.d("Logo upload response -> ${logoUploadResponse.value}")
                        val businessInfo = getBusinessInfoResponseAfterUploadingLogo(
                            it = logoUploadResponse.value,
                            timezone = it.timezone
                        )
                        sessionManager.saveLogo(logo = logoUploadResponse.value.logo.getEmptyIfNull())
                        sessionManager.saveMerchantBasicInfo(businessInfoResponse = businessInfo)
                        sessionManager.saveMerchantId(businessInfo.business_info.shop_id)
                        sessionManager.saveUserProfile(true)
                        progressDialog.hide()
                        Navigation.findNavController(view).navigate(R.id.imageUploadFragment)
                    }
                }
            }
        } else {
            sessionManager.saveMerchantBasicInfo(businessInfoResponse = businessInfoResponse)
            sessionManager.saveMerchantId(it.id.getZeroIfNull())
            sessionManager.saveUserProfile(true)
            progressDialog.hide()
            Navigation.findNavController(view).navigate(R.id.imageUploadFragment)
        }
    }

    private fun getBusinessInfoResponseForSaving(it: MerchantProfileUpdateResponse): BusinessInfoResponse {
        val businessInfoResponse = BusinessInfoResponse(
            basic_info = BasicInfo(
                merchant_user_id = it.id.getZeroIfNull(),
                mobile_no = it.mobileNo.getEmptyIfNull(),
                name = it.name.getEmptyIfNull()
            ),
            business_info = BusinessInfo(
                address = it.address.getEmptyIfNull(),
                description = it.description.getEmptyIfNull(),
                head_office_details = HeadOfficeDetails(
                    address = it.headOfficeDetails?.address.getEmptyIfNull(),
                    head_merchant_id = it.headOfficeDetails?.headMerchantId.getEmptyIfNull(),
                    mobile_no = it.headOfficeDetails?.mobileNo.getEmptyIfNull(),
                    name = it.headOfficeDetails?.name.getEmptyIfNull(),
                    unique_office_id = it.headOfficeDetails?.uniqueOfficeId.getEmptyIfNull()
                ),
                latitude = it.latitude.getEmptyIfNull(),
                logo = null,
                longitude = it.longitude.toString(),
                mobile_no = it.mobileNo.getEmptyIfNull(),
                name = it.name.getEmptyIfNull(),
                promotional_text = it.promotionalText.getEmptyIfNull(),
                shop_id = it.id.getZeroIfNull(),
                unique_office_id = it.uniqueOfficeId.getEmptyIfNull(),
                zip_code = it.zipCode.getEmptyIfNull(),
                country_name = it.countryName.getEmptyIfNull(),
                timezone = it.timezone.getEmptyIfNull(),
                website = it.website.getEmptyIfNull()
            ),
            tags_list = it.tagsDetails?.map { tag ->
                TagDetail(
                    name = tag?.name.getEmptyIfNull(),
                    tag_id = tag?.tagId.getEmptyIfNull()
                )
            }
        )
        return businessInfoResponse
    }

    private fun getBusinessInfoResponseAfterUploadingLogo(
        it: MerchantProfileResponse,
        timezone: String?
    ): BusinessInfoResponse {
        val businessInfoResponse = BusinessInfoResponse(
            basic_info = BasicInfo(
                merchant_user_id = it.id.getZeroIfNull(),
                mobile_no = it.mobileNo.getEmptyIfNull(),
                name = it.name.getEmptyIfNull()
            ),
            business_info = BusinessInfo(
                address = it.address.getEmptyIfNull(),
                description = it.description.getEmptyIfNull(),
                head_office_details = HeadOfficeDetails(
                    address = it.headOfficeDetails?.address.getEmptyIfNull(),
                    head_merchant_id = it.headOfficeDetails?.headMerchantId.getEmptyIfNull(),
                    mobile_no = it.headOfficeDetails?.mobileNo.getEmptyIfNull(),
                    name = it.headOfficeDetails?.name.getEmptyIfNull(),
                    unique_office_id = it.headOfficeDetails?.uniqueOfficeId.getEmptyIfNull()
                ),
                latitude = it.latitude.getEmptyIfNull(),
                logo = null,
                longitude = it.longitude.toString(),
                mobile_no = it.mobileNo.getEmptyIfNull(),
                name = it.name.getEmptyIfNull(),
                promotional_text = it.promotionalText.getEmptyIfNull(),
                shop_id = it.id.getZeroIfNull(),
                unique_office_id = it.uniqueOfficeId.getEmptyIfNull(),
                zip_code = it.zipCode.getEmptyIfNull(),
                country_name = it.countryName.getEmptyIfNull(),
                timezone = timezone.getEmptyIfNull(),
                website = it.website.getEmptyIfNull()
            ),
            tags_list = it.tagsDetails?.map { tag ->
                TagDetail(
                    name = tag?.name.getEmptyIfNull(),
                    tag_id = tag?.tagId.getEmptyIfNull()
                )
            }
        )
        return businessInfoResponse
    }

    fun updateSelectedTags(tag: String) {

        Timber.d("Selected tags -> $selectedTags")
        if (selectedTags.size >= 10) {
            showSnackbar(R.string.you_can_send_only_10_tags_please_choose_the_tags_that_fits_your_business)
            return
        }

        val tagAlreadyExists = selectedTags.contains(tag)
        if (tagAlreadyExists) {
            return
        }

        selectedTags.add(tag)
        Timber.d("Selected tags -> $selectedTags")

        val chip = getChip(tag)
        binding.cgTags.addView(chip)
//        binding.etTags.editText?.text?.clear()
        binding.atvTags.text?.clear()
        Log.d(TAG, "all tags -> $selectedTags")

    }

    private fun getChip(tag: String): Chip {
        val chip = Chip(context)
        chip.setChipBackgroundColorResource(R.color.graySurface)
        chip.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        chip.setCloseIconResource(R.drawable.ic_close)
        chip.apply {

            Log.d(TAG, "Tag -> $tag")
            Log.d(TAG, "Last index -> ${selectedTags.lastIndex}")

            id = selectedTags.lastIndex
            text = tag
            isCloseIconVisible = true
            setOnCloseIconClickListener { chipToBeRemoved ->

                Timber.d("Selected tags -> $selectedTags")
                Log.d(TAG, "Chip to be removed -> ID: ${chipToBeRemoved.id}")
                val categoryToBeRemoved = selectedTags.find { chipText ->
                    val materialChip = chipToBeRemoved as Chip
                    val materialChipText = materialChip.text.toString()
                    materialChipText.equals(chipText, ignoreCase = true)
                }

                Log.d(TAG, "Chips to be removed -> $categoryToBeRemoved")
                selectedTags.remove(categoryToBeRemoved)

                Log.d(TAG, "After removing a chip -> $selectedTags")
                binding.cgTags.removeView(chipToBeRemoved)

            }

        }
        return chip
    }

    private fun getAvailableTags() {
        viewModel.getAvailableTags().observe(viewLifecycleOwner) {

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

    override fun onStart() {
        super.onStart()

        val appBarLayout: AppBarLayout =
            ((activity) as MerchantRegistrationActivity).findViewById(R.id.customAppBar)
        val ivBack: ImageView = appBarLayout.findViewById(R.id.backIV)
        ivBack.isVisible = false

        if (((activity) as MerchantRegistrationActivity).goToCashBack) {
            Navigation.findNavController(view).navigate(R.id.imageUploadFragment)
        }

        val businessInfo = HashMap<String, String?>()

        val finalParam = HashMap<String, Any>()
        finalParam["business_info"] = businessInfo
        finalParam["tags"] = selectedTags

        val merchantBasicInfo = sessionManager.fetchMerchantBasicInfo()
        Timber.d("Saved merchant basic info -> $merchantBasicInfo")

        if (viewModel.logoFile != null) {
            Glide.with(this)
                .load(viewModel.logoFile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.ivLogo)
        }

        if (viewModel.coverPhotoFile != null) {
            Glide.with(this)
                .load(viewModel.coverPhotoFile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.ivCoverPhoto)
        }

        if (merchantBasicInfo != null) {
            binding.apply {
                businessNameET.editText?.setText(merchantBasicInfo.business_info.name)
                addressET.editText?.setText(merchantBasicInfo.business_info.address)
                postalCodeET.editText?.setText(merchantBasicInfo.business_info.zip_code)
                etPromotionalText.editText?.setText(merchantBasicInfo.business_info.promotional_text)
                tvDescription.editText?.setText(merchantBasicInfo.business_info.description)
                countryCodePicker.fullNumber = merchantBasicInfo.business_info.mobile_no
                userNameET.editText?.setText(merchantBasicInfo.basic_info.name)
                headOfficeET.editText?.setText(merchantBasicInfo.business_info.head_office_details.unique_office_id)
                if (!merchantBasicInfo.business_info.website.isNullOrBlank()) {
                    val website = merchantBasicInfo.business_info.website.replace("https://", "")
                    tilWebsite.editText?.setText(website)
                }
                val tags = merchantBasicInfo.tags_list?.map { it.name }
                Timber.d("Tags -> $tags")
                Timber.d("Selected tags -> $selectedTags")
                tags?.forEach { tag ->
                    if (tag.isNotBlank()) {
                        updateSelectedTags(tag = tag)
                    }
                }
            }
        } else {

            val token = sessionManager.fetchAuthToken().getEmptyIfNull()
            fetchAvailableCountries(token = token)

        }

        binding.spCountries.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // You can define your actions as you want
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {

                val selectedObject = binding.spCountries.selectedItem as CountryResponse.Country
                Timber.d("Selected country -> ${selectedObject.iso}")
                viewModel.selectedCountry = selectedObject
                binding.addressET.clear()
                binding.postalCodeET.clear()

            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun openPlacesAutoComplete() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setCountries(listOf(viewModel.selectedCountry?.iso))
            .build(requireContext())
        startActivityForResult(intent, autoCompleteRequestCode)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == autoCompleteRequestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        place = Autocomplete.getPlaceFromIntent(data)
                        binding.addressET.editText?.setText(place.address)
                        Timber.d("Latitude -> ${place.latLng?.latitude}")
                        Timber.d("Longitude -> ${place.latLng?.longitude}")
                        viewModel.latitude = place.latLng?.latitude
                        viewModel.longitude = place.latLng?.longitude

                        // Get the postal code from the Place object
                        val postalCode = getPostalCodeFromPlace(place)
                        binding.postalCodeET.editText?.setText(postalCode)
                        Timber.d("Postal Code: $postalCode")

                    }
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Timber.d(status.statusMessage ?: "")
                        val errorDialog =
                            ErrorDialog.getInstance(message = status.statusMessage ?: "Error")
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

    private fun getPostalCodeFromPlace(place: Place): String? {
        val addressComponents: List<AddressComponent>? = place.addressComponents?.asList()

        // Find the postal code in the address components
        addressComponents?.forEach { component ->
            if (component.types.contains("postal_code")) {
                return component.name
            }
        }
        return null
    }

}