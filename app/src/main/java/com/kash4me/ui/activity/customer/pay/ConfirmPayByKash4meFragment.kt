package com.kash4me.ui.activity.customer.pay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.kash4me.merchant.databinding.FragmentConfirmPayByKash4meBinding
import com.kash4me.security.AES
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.toDoubleOrNull
import timber.log.Timber


class ConfirmPayByKash4meFragment : Fragment() {

    private var _binding: FragmentConfirmPayByKash4meBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmPayByKash4meBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as PayByKash4meActivity).supportActionBar?.title = "Payment Detail"

        val args = arguments?.let { ConfirmPayByKash4meFragmentArgs.fromBundle(it) }
        val qrImage = args?.arg?.qrImage
        Timber.d("QR Image -> $qrImage")
//        val qrCode = ImageUtils().decodeImageFromBase64(
//            context = requireContext(), base64String = qrImage
//        )
//        mBinding.ivQrCode.setImageBitmap(qrCode)

        val mWriter = MultiFormatWriter()
        try {
            val mMatrix =
                mWriter.encode(args?.arg?.qrImage, BarcodeFormat.QR_CODE, 1000, 1000)
            val mEncoder = BarcodeEncoder()
            val mBitmap = mEncoder.createBitmap(mMatrix)
            mBinding.ivQrCode.setImageBitmap(mBitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        Timber.d("Encoded data -> ${args?.arg?.qrImage}")

        val decodedQrContents = try {
            AES().decodeQrContents(qrContents = args?.arg?.qrImage.getEmptyIfNull())
        } catch (ex: Exception) {
            Timber.d("Caught exception -> ${ex.message}")
        }
        Timber.d("Decoded data -> $decodedQrContents")

        mBinding.apply {
            tvAmount.text =
                "$" + args?.purchaseAmount?.toDoubleOrNull()?.formatUsingCurrencySystem()
            tvDateTime.text = args?.arg?.date
            btnDone.setOnClickListener { (activity as PayByKash4meActivity).finish() }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}