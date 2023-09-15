package com.kash4me.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kash4me.merchant.R
import kotlinx.coroutines.flow.first
import timber.log.Timber

private val Context.dataStore by preferencesDataStore(AppConstants.KASH4ME_PREFERENCES)

object LocaleManager {

    private val dataStore by lazy { App.getContext()?.dataStore }

    private val LANGUAGE = stringPreferencesKey(name = AppConstants.APP_LOCALE)

    suspend fun setLocale(language: Language) {

        Timber.d("Setting locale -> $language")
        dataStore?.edit { preferences ->
            preferences[LANGUAGE] = language.code
        }

    }

    suspend fun getLocale(): Language? {

        val preferences = dataStore?.data?.first()
        val languageCode = preferences?.get(LANGUAGE) ?: return null
        Timber.d("Get locale -> $languageCode")
        return Language.getSelectedLanguage(code = languageCode)

    }

    enum class Language(
        val titleResource: Int,
        val code: String,
        val countryCode: String,
    ) {
        ENGLISH(
            titleResource = R.string.english,
            code = "en",
            countryCode = "US"
        ),
        FRENCH(
            titleResource = R.string.french,
            code = "fr",
            countryCode = "CA"
        );

        companion object {
            fun getSelectedLanguage(code: String): Language {

                values().forEach {
                    if (it.code == code)
                        return it
                }

                return ENGLISH

            }
        }

    }

}