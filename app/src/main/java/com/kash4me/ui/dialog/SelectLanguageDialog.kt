package com.kash4me.ui.dialog

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.kash4me.R
import com.kash4me.databinding.DialogSelectLanguageBinding
import com.kash4me.utils.LocaleManager
import com.kash4me.utils.LocaleManager.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale

class SelectLanguageDialog : DialogFragment() {

    private var selectedLanguage: Language? = null

    private var _binding: DialogSelectLanguageBinding? = null
    private val mBinding get() = _binding!!

    interface LanguageChangeListener {
        fun onLanguageChange(selectedLanguage: Language?)
    }

    companion object {

        private lateinit var languageChangeListener: LanguageChangeListener

        fun newInstance(languageChangeListener: LanguageChangeListener): SelectLanguageDialog {

            this.languageChangeListener = languageChangeListener
            return SelectLanguageDialog()

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelectLanguageBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val locale = LocaleManager.getLocale()
            if (locale?.code != null) {
                withContext(Dispatchers.Main) {
                    checkInRadioGroupAccordingToSelectedLanguage(languageCode = locale.code)
                }
            }
        }

        rgLanguageSelectionCheckListener()

        mBinding.btnOk.setOnClickListener {
            dialog?.dismiss()
            languageChangeListener.onLanguageChange(selectedLanguage)
        }
        mBinding.btnCancel.setOnClickListener { dialog?.dismiss() }

    }

    private fun checkInRadioGroupAccordingToSelectedLanguage(languageCode: String) {

        if (languageCode == Language.ENGLISH.code) {
            mBinding.rbEnglish.isChecked = true
            selectedLanguage = Language.ENGLISH
        } else {
            mBinding.rbFrench.isChecked = true
            selectedLanguage = Language.FRENCH
        }

        logSelectedLanguage(selectedLanguage!!)

    }

    private fun logSelectedLanguage(language: Language) {
        val title = getString(language.titleResource)
        Timber.d("Selected language is $title")
    }

    private fun rgLanguageSelectionCheckListener() {
        mBinding.rgLanguageSelection.setOnCheckedChangeListener { radioGroup, i ->

            when (radioGroup.checkedRadioButtonId) {

                R.id.rb_english -> setLocale(Language.ENGLISH)
                R.id.rb_french -> setLocale(Language.FRENCH)

            }
        }
    }

    fun setLocale(language: Language) {

        selectedLanguage = language

        val locale = Locale(language.code, language.countryCode)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.setLocale(locale)
        val displayMetrics = activity?.baseContext?.resources?.displayMetrics
        activity?.baseContext?.resources?.updateConfiguration(configuration, displayMetrics)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}