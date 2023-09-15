package com.kash4me.ui.fragments.customer.finish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kash4me.data.models.CustomerResponse
import com.kash4me.merchant.R
import com.kash4me.ui.activity.customer.customer_dashboard.CustomerDashboardActivity
import com.kash4me.utils.extensions.getNotAvailableIfEmptyOrNull


class CustomerFinishFragment : Fragment() {

    companion object {
        fun newInstance() = CustomerFinishFragment()
    }

    private lateinit var viewModel: CustomerFinishViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customer_finish, container, false)

        updateStepper(view)


        val userDetail = arguments?.get("userDetails") as CustomerResponse

        val nicknameTV = view.findViewById<TextView>(R.id.customerNameTV)
        val capitalizedString = userDetail.nick_name?.split(" ")
            ?.joinToString(" ") { it.replace(it.get(0), it.get(0).uppercaseChar()) }
        nicknameTV.text = capitalizedString
        val countryTV = view.findViewById<TextView>(R.id.countryTV)
        countryTV.text = userDetail.country_name
        val postalCodeTV = view.findViewById<TextView>(R.id.postalCodeTV)
        postalCodeTV.text = userDetail.zip_code
        val dobTV = view.findViewById<TextView>(R.id.dobTV)
        dobTV.text = userDetail.date_of_birth.getNotAvailableIfEmptyOrNull()
        val phoneNumberTV = view.findViewById<TextView>(R.id.phoneNumberTV)
        phoneNumberTV.text = userDetail.mobile_no


        val nextBtn = view.findViewById<Button>(R.id.finishBtn)
        nextBtn.setOnClickListener {
            val intent = CustomerDashboardActivity.getNewIntent(
                packageContext = requireActivity(),
                isFreshLogin = true
            )
            activity?.startActivity(intent)
            activity?.finish()
        }

        viewModel = ViewModelProvider(this)[CustomerFinishViewModel::class.java]


        return view
    }

    private fun updateStepper(view: View) {
        val firstImageView = view.findViewById<ImageView>(R.id.imageView)
        val secondImageView = view.findViewById<ImageView>(R.id.imageView2)

        firstImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_check
            )
        )
        secondImageView.setBackgroundResource(R.drawable.green_circle)
    }

}