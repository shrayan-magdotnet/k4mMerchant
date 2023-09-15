package com.kash4me.ui.fragments.merchant.registration.image_upload

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import com.kash4me.R
import com.kash4me.data.models.BasicInfo
import com.kash4me.data.models.BusinessInfo
import com.kash4me.data.models.BusinessInfoResponse
import com.kash4me.data.models.HeadOfficeDetails
import com.kash4me.data.models.TagDetail
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateResponse
import com.kash4me.databinding.FragmentImageUploadBinding
import com.kash4me.ui.activity.merchant.MerchantRegistrationActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.merchant.registration.basicinfo.MerchantBasicInfoViewModel
import com.kash4me.utils.AppConstants
import com.kash4me.utils.FileUtils
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.convertDpToPixel
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class ImageUploadFragment : Fragment() {

    private var _binding: FragmentImageUploadBinding? = null
    private val mBinding get() = _binding!!

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    @Inject
    lateinit var mSessionManager: SessionManager

    private val mViewModel: MerchantBasicInfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageUploadBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.cvLogo.setOnClickListener { showCameraOrGalleryDialog(view = mBinding.cvLogo) }
        mBinding.cvCoverPhoto.setOnClickListener { showCameraOrGalleryDialog(view = mBinding.cvCoverPhoto) }

        btnNextListener()

        mViewModel.errorMessage.observe(viewLifecycleOwner) {
            val errorDialog = ErrorDialog.getInstance(message = it)
            errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
            mProgressDialog.hide()
        }

    }

    override fun onStart() {
        super.onStart()

        updateStepper(view = mBinding.root)

        if (mViewModel.logoFile != null) {
            Glide.with(this)
                .load(mViewModel.logoFile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(mBinding.ivLogo)
        }

        if (mViewModel.coverPhotoFile != null) {
            Glide.with(this)
                .load(mViewModel.coverPhotoFile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(mBinding.ivCoverPhoto)
        }

    }

    override fun onResume() {
        super.onResume()

        val appBarLayout: AppBarLayout =
            ((activity) as MerchantRegistrationActivity).findViewById(R.id.customAppBar)
        val ivBack: ImageView = appBarLayout.findViewById(R.id.backIV)
        ivBack.isVisible = true
        ivBack.setOnClickListener { ((activity) as MerchantRegistrationActivity).onBackPressed() }

    }

    private fun updateStepper(view: View) {
        val basicInfo = view.findViewById<ImageView>(R.id.imageView)
        val ivImageUpload = view.findViewById<ImageView>(R.id.iv_image_upload)

        basicInfo.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
        )
        ivImageUpload.setBackgroundResource(R.drawable.green_circle)
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
            val logo =
                FileUtils().saveBitmapAsFile(context = requireContext(), bitmap = imageBitmap)
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
            val logo =
                FileUtils().saveBitmapAsFile(context = requireContext(), bitmap = imageBitmap)
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

                    processSelectedImage(context = requireContext(), imageUri = uri)
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

    private fun btnNextListener() {
        mBinding.btnNext.setOnClickListener {

            mProgressDialog.show()

            if (mViewModel.logoFile == null && mViewModel.coverPhotoFile == null) {
                findNavController().navigate(R.id.cashBackFragment)
                mProgressDialog.hide()
                return@setOnClickListener
            }

            mViewModel.updateMerchantDetails(
                token = mSessionManager.fetchAuthToken().getEmptyIfNull(),
                params = getRequestBody(),
                merchantShopID = mSessionManager.fetchMerchantBasicInfo()?.business_info?.shop_id.getZeroIfNull()
            ).observe(viewLifecycleOwner) { resource ->

                val businessInfoResponse = getBusinessInfoResponseForSaving(resource)
                mSessionManager.saveMerchantBasicInfo(businessInfoResponse = businessInfoResponse)
                mSessionManager.saveMerchantId(it.id.getZeroIfNull())
                mSessionManager.saveUserProfile(true)
                mProgressDialog.hide()
                findNavController().navigate(R.id.cashBackFragment)

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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}