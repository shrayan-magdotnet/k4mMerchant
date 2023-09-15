package com.kash4me.utils.media
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.content.ActivityNotFoundException
//import android.content.Context
//import android.content.ContextWrapper
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.database.Cursor
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Build
//import android.provider.MediaStore
//import androidx.activity.result.ActivityResult
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.core.content.ContextCompat
//import androidx.core.net.toUri
//import com.permissionx.guolindev.PermissionX
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import java.io.OutputStream
//import java.util.UUID
//
//class ImagePickerUtils {
//
//    fun showCameraOrGallaryDialog(activity: AppCompatActivity) {
//        // check get current api version
//        var lPerm = Manifest.permission.READ_EXTERNAL_STORAGE
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            lPerm = Manifest.permission.READ_MEDIA_VIDEO
//        }
//
//        val isPermissionDenied =
//            ContextCompat.checkSelfPermission(activity, lPerm) != PackageManager.PERMISSION_GRANTED
//        if (isPermissionDenied) {
//            activity.let {
//                PermissionX.init(it)
//                    .permissions(lPerm)
//                    .explainReasonBeforeRequest()
//                    .onExplainRequestReason { scope, deniedList ->
//                        scope.showRequestReasonDialog(
//                            deniedList,
//                            "Please allow the following permissions to access full features of the app",
//                            "OK",
//                            "Cancel"
//                        )
//                    }
//                    .onForwardToSettings { scope, deniedList ->
//                        scope.showForwardToSettingsDialog(
//                            deniedList,
//                            "You need to allow necessary permissions in Settings manually",
//                            "OK",
//                            "Cancel"
//                        )
//                    }
//                    .request { allGranted, grantedList, deniedList ->
//                        if (allGranted) {
//                            //                            Toast.makeText(context, "All permissions are granted", Toast.LENGTH_LONG).show()
//                        } else {
//                            //                            Toast.makeText(context, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
//                        }
//                    }
//            }
//        } else {
//            val builder = AlertDialog.Builder(activity)
//            builder.setMessage("Please choose!")
//            builder.setPositiveButton("Camera") { dialog, which ->
//                dialog.dismiss()
//                dispatchTakePictureIntent(activity)
//            }
//            builder.setNegativeButton("Gallery") { dialog, which ->
//                dialog.dismiss()
//                getfileResultLauncher.launch(requestFileIntent)
//            }
//            builder.show()
//        }
//    }
//
//    private fun dispatchTakePictureIntent(activity: AppCompatActivity) {
//        if (ContextCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            activity.let {
//                PermissionX.init(it)
//                    .permissions(Manifest.permission.CAMERA)
//                    .explainReasonBeforeRequest()
//                    .onExplainRequestReason { scope, deniedList ->
//                        scope.showRequestReasonDialog(
//                            deniedList,
//                            "Please allow the following permissions to access full features of the app",
//                            "OK",
//                            "Cancel"
//                        )
//                    }
//                    .onForwardToSettings { scope, deniedList ->
//                        scope.showForwardToSettingsDialog(
//                            deniedList,
//                            "You need to allow necessary permissions in Settings manually",
//                            "OK",
//                            "Cancel"
//                        )
//                    }
//                    .request { allGranted, grantedList, deniedList ->
//                        if (allGranted) {
//                            //                            Toast.makeText(context, "All permissions are granted", Toast.LENGTH_LONG).show()
//                        } else {
//                            //                            Toast.makeText(context, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
//                        }
//                    }
//            }
//        } else {
//            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            try {
//                camlistResultLauncher.launch(takePictureIntent)
//            } catch (e: ActivityNotFoundException) {
//                // display error state to the user
//            }
//        }
//    }
//
//    //image
//    private val requestFileIntent by lazy {
//        Intent(Intent.ACTION_GET_CONTENT).apply {
//            type = "image/*"
//        }
//    }
//
//    var getfileResultLauncher: ActivityResultLauncher<Intent> =
//        registerForActivityResult<Intent, ActivityResult>(
//            ActivityResultContracts.StartActivityForResult()
//        ) { it ->
//            if (it.resultCode != Activity.RESULT_OK) {
//                return@registerForActivityResult
//            }
//            //++++++++++++++++++++++++++++++++++++++++++++For gallery+++++++++++++++++
//            it.data?.data?.let { fileUri ->
//                getImageFilePath(fileUri)?.let { filePath ->
//                    photoPermissionGranted?.let {
//                        it(fileUri, filePath)
//                    }
//                }
//            }
//        }
//
//    @SuppressLint("Range")
//    fun getImageFilePath(context: Context, uri: Uri): String? {
//        val file = File(uri.path)
//        val filePath = file.path.split(":").toTypedArray()
//        val image_id = filePath[filePath.size - 1]
//        val cursor: Cursor? = context.contentResolver?.query(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            null,
//            MediaStore.Images.Media._ID + " = ? ",
//            arrayOf(image_id),
//            null
//        )
//        if (cursor != null) {
//            cursor.moveToFirst()
//            val imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
//            cursor.close()
//            return imagePath
//        }
//        return null
//    }
//
//    var camlistResultLauncher: ActivityResultLauncher<Intent> =
//        registerForActivityResult<Intent, ActivityResult>(
//            ActivityResultContracts.StartActivityForResult()
//        ) { it ->
//            if (it.resultCode != Activity.RESULT_OK) {
//                return@registerForActivityResult
//            }
//
//            val imageBitmap = it.data?.extras?.get("data") as Bitmap
//            val uri = bitmapToFile(imageBitmap)
//
//            photoPermissionGranted?.let {
//                val fileUri = File(uri.path).toUri()
//                val filePath = File(uri.path).absolutePath
//                it(fileUri, filePath)
//            }
//
//        }
//
//    private fun bitmapToFile(context: Context, bitmap: Bitmap): Uri {
//        // Get the context wrapper
//        val wrapper = ContextWrapper(context)
//
//        // Initialize a new file instance to save bitmap object
//        var file = wrapper.filesDir
//        file = File(file, "${UUID.randomUUID()}.jpg")
//
//        try {
//            // Compress the bitmap and save in jpg format
//            val stream: OutputStream = FileOutputStream(file)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//            stream.flush()
//            stream.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        // Return the saved bitmap uri
//        return Uri.parse(file.absolutePath)
//    }
//
//    private var photoPermissionGranted: ((Uri, String) -> Unit)? = null
//    fun onPhotoPermissionGranted(photoPermissionGranteds: ((Uri, String) -> Unit)) {
//        photoPermissionGranted = photoPermissionGranteds
//    }
//
//}