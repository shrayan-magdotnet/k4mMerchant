package com.kash4me.ui.activity.customer.request_cashback

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.kash4me.data.models.request.RequestCashbackQrRequest
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentGenerateQrCodeForCashbackBinding
import com.kash4me.security.AES
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.customer.home.CustomerHomeFragment
import com.kash4me.ui.fragments.customer.home.CustomerHomeViewModel
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class GenerateQrCodeForCashbackFragment : Fragment() {

    private var _binding: FragmentGenerateQrCodeForCashbackBinding? = null
    private val mBinding get() = _binding!!

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    private val mViewModel: CustomerHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenerateQrCodeForCashbackBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.let { GenerateQrCodeForCashbackFragmentArgs.fromBundle(it) }
        Timber.d("Shop ID -> ${args?.request?.shopId} | Purchase Amount -> ${args?.request?.amount}")
        val request = args?.request
        if (request == null) {
            showToast("Couldn't generate QR code")
        } else {
            requestQrCode(request = request)
        }

        mBinding.btnDone.setOnClickListener {
            val sharedPreferences = context?.getSharedPreferences(
                requireActivity().getString(R.string.first_start_status), Context.MODE_PRIVATE
            )
            sharedPreferences?.edit {
                putBoolean(CustomerHomeFragment.CUSTOMER_HOME, true)
            }
            activity?.finish()
        }

    }

    private fun requestQrCode(request: RequestCashbackQrRequest) {

        mViewModel.requestCashback(request = request).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                    mProgressDialog.hide()
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    Timber.d("QR Image -> ${it.value.qrImage}")
//                    val qrCode = ImageUtils().decodeImageFromBase64(
//                        context = requireContext(), base64String = it.value.qrImage
//                    )
//                    mBinding.ivQrCode.setImageBitmap(qrCode)

                    val mWriter = MultiFormatWriter()
                    try {
                        // Here, it.value.qrImage contains encrypted data containing all info
                        // We need to create a QR code for this encrypted data
                        val mMatrix =
                            mWriter.encode(it.value.qrImage, BarcodeFormat.QR_CODE, 1000, 1000)
                        val mEncoder = BarcodeEncoder()
                        val mBitmap = mEncoder.createBitmap(mMatrix)
                        mBinding.ivQrCode.setImageBitmap(mBitmap)
                    } catch (e: WriterException) {
                        e.printStackTrace()
                    }

                    Timber.d("Encrypted data -> ${it.value.qrImage}")

                    val decodedQrContents = try {
                        AES().decodeQrContents(qrContents = it.value.qrImage.getEmptyIfNull())
                    } catch (ex: Exception) {
                        ""
                    }
                    Timber.d("Decoded data -> $decodedQrContents")

                    mProgressDialog.hide()
                }
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}