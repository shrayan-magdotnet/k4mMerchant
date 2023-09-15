package com.kash4me.ui.fragments.merchant.announcement

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.databinding.ActivityCreateAnnouncementBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CreateAnnouncementActivity : AppCompatActivity() {

    private var binding: ActivityCreateAnnouncementBinding? = null
    private val mBinding get() = binding!!

    private val mProgressDialog by lazy { CustomProgressDialog(this) }

    private val mStartDate: Calendar by lazy { Calendar.getInstance(Locale.US) }
    private val mStartDateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            mStartDate.set(Calendar.YEAR, year)
            mStartDate.set(Calendar.MONTH, monthOfYear)
            mStartDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView(date = mStartDate, textView = mBinding.tvStartDate)
        }

    private val mExpiryDate: Calendar by lazy { Calendar.getInstance(Locale.US) }
    private val mExpiryDateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            mExpiryDate.set(Calendar.YEAR, year)
            mExpiryDate.set(Calendar.MONTH, monthOfYear)
            mExpiryDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView(date = mExpiryDate, textView = mBinding.tvExpiryDate)
        }

    private val mViewModel: CreateAnnouncementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAnnouncementBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
        setupToolbar(title = "Announcement")

        setDefaultDates()

        mBinding.cvStartDate.setOnClickListener {
            openDatePicker(date = mStartDate, dateSetListener = mStartDateSetListener)
        }

        mBinding.cvExpiryDate.setOnClickListener {
            openDatePicker(date = mExpiryDate, dateSetListener = mExpiryDateSetListener)
        }

        getAnnouncement()
        btnSendListener()

    }

    private fun getAnnouncement() {
        mViewModel.getYourAnnouncement().observe(this) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    mBinding.apply {

                        if (it.value.message?.isNotBlank() == true)
                            etAnnouncement.setText(it.value.message)

                        if (it.value.announcedAt?.isNotBlank() == true)
                            tvStartDate.text = it.value.announcedAt

                        if (it.value.expireOn?.isNotBlank() == true)
                            tvExpiryDate.text = it.value.expireOn

                    }
                }
            }

        }
    }

    private fun btnSendListener() {
        mBinding.btnSend.setOnClickListener {

            val announcement = mBinding.etAnnouncement.text.toString()
            if (announcement.isBlank()) {
                showToast("Please enter your announcement")
                return@setOnClickListener
            }

            val announcedAt = mBinding.tvStartDate.text.toString()
            val expiresOn = mBinding.tvExpiryDate.text.toString()

            Timber.d("Announcement -> $announcement")
            Timber.d("Announced at -> $announcedAt")
            Timber.d("Expires on -> $expiresOn")

            createOrUpdateAnnouncement(announcement, announcedAt, expiresOn)

        }
    }

    private fun createOrUpdateAnnouncement(
        announcement: String,
        announcedAt: String,
        expiresOn: String
    ) {
        mViewModel.createOrUpdateYourAnnouncement(
            announcement = announcement, announcedAt = announcedAt, expiresOn = expiresOn
        ).observe(this) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    val successDialog = SuccessDialog.getInstance(
                        message = it.value.detail ?: "Announcement has been updated"
                    )
                    successDialog.show(supportFragmentManager, successDialog.tag)
                }
            }

        }
    }

    fun setupToolbar(title: String) {
        setSupportActionBar(mBinding.toolbar.root)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbar.root.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setDefaultDates() {
        updateDateInView(date = mStartDate, textView = mBinding.tvStartDate)
        updateDateInView(date = mExpiryDate, textView = mBinding.tvExpiryDate)
    }

    private fun updateDateInView(date: Calendar, textView: TextView) {
        val myFormat = "yyyy-MM-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        textView.text = sdf.format(date.time ?: Date())
    }

    private fun openDatePicker(
        date: Calendar,
        dateSetListener: DatePickerDialog.OnDateSetListener
    ) {
        val datePickerDialog = DatePickerDialog(
            this, dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

}