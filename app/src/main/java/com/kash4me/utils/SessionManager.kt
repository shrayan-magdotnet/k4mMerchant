package com.kash4me.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kash4me.R
import com.kash4me.data.local.AppDatabase
import com.kash4me.data.models.BusinessInfoResponse
import com.kash4me.data.models.customer.customer_details.CustomerDetailsResponse
import com.kash4me.data.models.merchant.cashback.CashbackResponseV2
import com.kash4me.data.models.merchant.profile.MerchantProfileResponse
import com.kash4me.data.models.user.UserType
import com.kash4me.ui.activity.login.LoginActivity
import timber.log.Timber

class SessionManager
constructor(val context: Context) {

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    private var mFirstOpenStatusSharedPref: SharedPreferences =
        context.getSharedPreferences(
            context.getString(R.string.first_start_status), Context.MODE_PRIVATE
        )

    private val encryptedSharedPref get() = getEncryptedSharedPreferences()
    private fun getEncryptedSharedPreferences(): SharedPreferences {

        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "token_preferences",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    }

    private val mAppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    companion object {
        const val TAG = "SessionManager"

        const val USER_TOKEN = "user_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val MERCHANT_USER_ID = "merchant_user_id"
        const val USER_TYPE = "user_type"
        const val USER_PROFILE = "user_profile"
        const val CB_SETTINGS = "cb_settings"

        const val MERCHANT_BASIC_INFO = "merchant_basic_info"
        const val MERCHANT_DETAILS = "merchant_details"
        const val CASHBACK_SETTINGS = "cashback_settings"

        const val CUSTOMER_DETAILS = "customer_details"
    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String) {
        encryptedSharedPref.edit {
            Log.d(TAG, "saveAuthToken: Saving access token in encrypted shared pref -> $token")
            putString(USER_TOKEN, token)
        }
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        val token = encryptedSharedPref.getString(USER_TOKEN, null)
        Log.d(TAG, "fetchAuthToken: getting token from encrypted shared pref -> $token")
        return token
    }

    private fun removeAuthToken() {
        encryptedSharedPref.edit {
            Log.d(TAG, "removeAuthToken: Removing access token from encrypted shared pref")
            putString(USER_TOKEN, null)
        }
    }

    fun saveRefreshToken(token: String) {
        encryptedSharedPref.edit { putString(REFRESH_TOKEN, token) }
    }

    fun fetchRefreshToken(): String? {
        return encryptedSharedPref.getString(REFRESH_TOKEN, null)
    }

    private fun removeRefreshToken() {
        encryptedSharedPref.edit { remove(REFRESH_TOKEN) }
    }

    fun saveMerchantDetails(merchantDetails: MerchantProfileResponse) {
        val json = Gson().toJson(merchantDetails)
        Timber.d("Merchant details -> $json")
        sharedPreferences.edit { putString(MERCHANT_DETAILS, json) }
    }

    fun fetchMerchantDetails(): MerchantProfileResponse? {
        val json = sharedPreferences.getString(MERCHANT_DETAILS, "")
        val gson = Gson()
        if (json == null) {
            return null
        }
        val objectType = object : TypeToken<MerchantProfileResponse>() {}.type
        return gson.fromJson(json, objectType)
    }

    fun clearMerchantDetails() {
        sharedPreferences.edit { putString(MERCHANT_DETAILS, null) }
    }

    fun saveCustomerDetails(customerDetails: CustomerDetailsResponse) {
        val json = Gson().toJson(customerDetails)
        Timber.d("Customer details -> $json")
        sharedPreferences.edit { putString(CUSTOMER_DETAILS, json) }
    }

    fun fetchCustomerDetails(): CustomerDetailsResponse? {
        val json = sharedPreferences.getString(CUSTOMER_DETAILS, "")
        val gson = Gson()
        if (json == null) {
            return null
        }
        val objectType = object : TypeToken<CustomerDetailsResponse>() {}.type
        return gson.fromJson(json, objectType)
    }

    fun clearCustomerDetails() {
        sharedPreferences.edit { putString(CUSTOMER_DETAILS, null) }
    }

    fun saveMerchantId(merchantId: Int) {
        sharedPreferences.edit(commit = true) {
            putInt(MERCHANT_USER_ID, merchantId)
        }

    }

    fun fetchMerchantId(): Int {
        return sharedPreferences.getInt(MERCHANT_USER_ID, 0)
    }

    private fun removeMerchantId() {
        sharedPreferences.edit {
            remove(MERCHANT_USER_ID)
        }
    }

    fun fetchMerchantBasicInfo(): BusinessInfoResponse? {
        val merchantBasicInfoInJson = sharedPreferences.getString(MERCHANT_BASIC_INFO, "")
        val gson = Gson()
        if (merchantBasicInfoInJson == null) {
            return null
        }
        val objectType = object : TypeToken<BusinessInfoResponse>() {}.type
        return gson.fromJson(merchantBasicInfoInJson, objectType)
    }

    fun saveMerchantBasicInfo(businessInfoResponse: BusinessInfoResponse) {
        val basicInfoInJson = Gson().toJson(businessInfoResponse)
        Timber.d("Merchant basic info -> $basicInfoInJson")
        sharedPreferences.edit {
            putString(MERCHANT_BASIC_INFO, basicInfoInJson)
        }
    }

    fun clearMerchantBasicInfo() {
        sharedPreferences.edit {
            putString(MERCHANT_BASIC_INFO, null)
        }
    }

    fun fetchCashbackSettings(): CashbackResponseV2? {
        val cashbackSettingsInJson = sharedPreferences.getString(CASHBACK_SETTINGS, "")
        Timber.d("Fetching cashback settings -> $cashbackSettingsInJson")
        val gson = Gson()
        if (cashbackSettingsInJson == null) {
            return null
        }
        val objectType = object : TypeToken<CashbackResponseV2>() {}.type
        return gson.fromJson(cashbackSettingsInJson, objectType)
    }

    fun saveCashbackSettings(cashbackSettings: CashbackResponseV2) {
        val cashbackSettingsInJson = Gson().toJson(cashbackSettings)
        Timber.d("Saving cashback settings -> $cashbackSettingsInJson")
        sharedPreferences.edit {
            putString(CASHBACK_SETTINGS, cashbackSettingsInJson)
        }
    }

    fun clearCashbackSettings() {
        sharedPreferences.edit {
            putString(CASHBACK_SETTINGS, null)
        }
    }

    fun saveUserType(userType: Int) {
        sharedPreferences.edit(commit = true) { putInt(USER_TYPE, userType) }
    }

    fun fetchUserType(): UserType? {
        val userTypeId = sharedPreferences.getInt(USER_TYPE, AppConstants.MINUS_ONE)
        Timber.d("User type -> $userTypeId")
        return when (userTypeId) {
            UserType.CUSTOMER.id -> UserType.CUSTOMER
            UserType.MERCHANT.id -> UserType.MERCHANT
            UserType.STAFF.id -> UserType.STAFF
            UserType.ANONYMOUS.id -> UserType.ANONYMOUS
            else -> null
        }
    }

    private fun removeUserType() {
        sharedPreferences.edit { remove(USER_TYPE) }
    }


    /**
     * Function to save user profile, if user has completed profile or not
     */
    fun saveUserProfile(userProfile: Boolean = false) {
        sharedPreferences.edit(commit = true) {
            putBoolean(USER_PROFILE, userProfile)
        }
    }

    /**
     * Function to fetch user profile
     */
    fun fetchUserProfile(): Boolean {
        return sharedPreferences.getBoolean(USER_PROFILE, false)
    }

    /**
     * Function to remove user profile
     */
    private fun removeUserProfile() {
        sharedPreferences.edit {
            remove(USER_PROFILE)
        }
    }


    /**
     * Function to save cashback settings, if merchant  has completed cashback settings or not
     */
    fun saveCBSettings(merchantShop: Boolean = false) {
        sharedPreferences.edit(commit = true) {
            putBoolean(CB_SETTINGS, merchantShop)
        }
    }

    /**
     * Function to fetch cashback settings
     */
    fun fetchCBSettings(): Boolean {
        return sharedPreferences.getBoolean(CB_SETTINGS, false)
    }

    /**
     * Function to remove cashback settings
     */
    private fun removeCBSettings() {
        sharedPreferences.edit {
            remove(CB_SETTINGS)
        }
    }

    fun saveLogo(logo: String) {
        sharedPreferences.edit { putString("logo", logo) }
    }

    fun getLogo(): String? {
        return sharedPreferences.getString("logo", null)
    }

    suspend fun logoutUser(packageContext: Context) {
        removeUserType()
        removeAuthToken()
        removeRefreshToken()
        removeMerchantId()
        removeUserProfile()
        removeCBSettings()
        clearMerchantBasicInfo()
        clearMerchantDetails()
        clearCustomerDetails()
        mFirstOpenStatusSharedPref.edit().clear().apply()
        mAppDatabase.clearAllTables()
        mAppDatabase.close()
        navigateToLogin(packageContext = packageContext)
    }

    private fun navigateToLogin(packageContext: Context) {
        val intent = LoginActivity.getNewIntent(packageContext = packageContext)
        packageContext.startActivity(intent)
    }

}