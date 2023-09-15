package com.kash4me.ui.activity.merchant.image_settings

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
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import com.kash4me.data.models.merchant.profile.MerchantProfileResponse
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityImageSettingsBinding
import com.kash4me.ui.activity.merchant.merchant_profile.MerchantProfileViewModel
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.FileUtils
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.convertDpToPixel
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getNotAvailableIfEmptyOrNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.listeners.AfterDismissalListener
import com.kash4me.utils.network.Resource
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class ImageSettingsActivity : AppCompatActivity() {

    private var _binding: ActivityImageSettingsBinding? = null
    private val mBinding get() = _binding!!

    private val mProgressDialog by lazy { CustomProgressDialog(this) }

    @Inject
    lateinit var mSessionManager: SessionManager

    private val mViewModel: MerchantProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityImageSettingsBinding.inflate(layoutInflater)
        val view = _binding!!.root
        setContentView(view)
        setupToolbar()

        fetchMerchantDetails()

        mBinding.cvLogo.setOnClickListener { showCameraOrGalleryDialog(view = mBinding.cvLogo) }
        mBinding.cvCoverPhoto.setOnClickListener { showCameraOrGalleryDialog(view = mBinding.cvCoverPhoto) }

        btnUpdateListener()

    }

    private fun btnUpdateListener() {
        mBinding.btnUpdate.setOnClickListener {

            mViewModel.updateMerchantDetails(
                token = mSessionManager.fetchAuthToken().getEmptyIfNull(),
                params = getRequestBody(),
                merchantShopID = mSessionManager.fetchMerchantDetails()?.id.getZeroIfNull()
            ).observe(this) { resource ->

                when (resource) {

                    is Resource.Failure -> {
                        mProgressDialog.hide()
                        val errorDialog = ErrorDialog.getInstance(message = resource.errorMsg)
                        errorDialog.show(supportFragmentManager, errorDialog.tag)
                    }

                    Resource.Loading -> {
                        mProgressDialog.show()
                    }

                    is Resource.Success -> {
                        mProgressDialog.hide()
                        //                        merchantProfileDetails = resource.value
                        setData(profileDetails = resource.value)

                        mSessionManager.saveMerchantDetails(merchantDetails = resource.value)
                        val successDialog = SuccessDialog.getInstance(
                            message = "Profile has been updated",
                            afterDismissClicked = object : AfterDismissalListener {
                                override fun afterDismissed() {
                                    finish()
                                }
                            })
                        successDialog.show(supportFragmentManager, successDialog.tag)

                        Timber.d("Profile update complete")

                    }
                }

            }

        }
    }

    private fun getRequestBody(): HashMap<String, Any?> {
        val params = HashMap<String, Any?>()

        val logo = mViewModel.logoFile
        if (logo != null) {
            params["logo"] = ImageUtils().imageFileToBase64(file = logo)
        }

        val coverPhoto = mViewModel.coverPhotoFile
        if (coverPhoto != null) {
            params["main_image"] = ImageUtils().imageFileToBase64(file = coverPhoto)
        }
        return params
    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Image Settings"
    }

    private fun fetchMerchantDetails() {
        val token = mSessionManager.fetchAuthToken().getEmptyIfNull()
        mViewModel.fetchMerchantDetails(token = token).observe(this) { resource ->

            when (resource) {

                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = resource.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    setData(profileDetails = resource.value, shouldUpdateImages = true)
//                    merchantProfileDetails = resource.value
                    mSessionManager.saveMerchantDetails(merchantDetails = resource.value)
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
                    Glide.with(this@ImageSettingsActivity)
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
                    Glide.with(this@ImageSettingsActivity)
                        .load(profileDetails.mainImage)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.ic_insert_photo)
                        .error(R.drawable.ic_insert_photo)
                        .into(ivCoverPhoto)
                } else {
                    // Do nothing
                }
            }

        }

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
            mViewModel.logoFile = logo
//            viewModel.hasLogoBeenUpdated.value = true

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
            mViewModel.coverPhotoFile = logo
//            viewModel.hasCoverPhotoBeenUpdated.value = true

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

    private fun getAttachmentSizeLimitInfoMessage(): String {
        val message: String = String.format(
            getString(R.string.please_keep_image_size_below_x_mb), AppConstants.IMAGE_SIZE_LIMIT
        )
        return Html.fromHtml(message).toString()
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

                mViewModel.logoFile =
                    FileUtils().saveBitmapAsFile(context = context, bitmap = resizedBitmap)
//                viewModel.hasLogoBeenUpdated.value = true

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

                mViewModel.coverPhotoFile =
                    FileUtils().saveBitmapAsFile(context = context, bitmap = resizedBitmap)
//                viewModel.hasCoverPhotoBeenUpdated.value = true

                Glide.with(this)
                    .load(resizedBitmap)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(mBinding.ivCoverPhoto)

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            // Handle other menu items if needed
            else -> super.onOptionsItemSelected(item)
        }
    }

}