package com.kash4me.network

import com.bumptech.glide.load.engine.Resource
import com.kash4me.data.models.*
import com.kash4me.data.models.customer.annoucement.AnnouncementsResponse
import com.kash4me.data.models.customer.claim_coupon.ClaimCouponRequest
import com.kash4me.data.models.customer.claim_coupon.ClaimCouponResponse
import com.kash4me.data.models.customer.create_transaction.CreateTransactionRequest
import com.kash4me.data.models.customer.create_transaction.CreateTransactionResponse
import com.kash4me.data.models.customer.customer_details.CustomerDetailsResponse
import com.kash4me.data.models.customer.delete.DeleteCustomerResponse
import com.kash4me.data.models.customer.on_your_way_transactions.ProcessingTransaction
import com.kash4me.data.models.customer.pay_by_kash4me.PayByKash4meRequest
import com.kash4me.data.models.customer.pay_by_kash4me.PayByKash4meResponse
import com.kash4me.data.models.customer.response.CustomerTransactionsResponse
import com.kash4me.data.models.customer.response.CustomerTransactionsResponseV2
import com.kash4me.data.models.customer.transactions_according_to_merchant.TransactionsAccordingToMerchantResponse
import com.kash4me.data.models.customer.transactions_according_to_merchant.transaction_details.TransactionDetailsForReturningPurchase
import com.kash4me.data.models.customer.update_profile.CustomerProfileUpdateRequest
import com.kash4me.data.models.customer.withdraw_amount.WithdrawAmountResponse
import com.kash4me.data.models.merchant.CustomerDetailsResponseDto
import com.kash4me.data.models.merchant.accept_kash4me_payment.AcceptKash4mePaymentRequest
import com.kash4me.data.models.merchant.accept_kash4me_payment.AcceptKash4mePaymentResponse
import com.kash4me.data.models.merchant.announcement.create_or_update.CreateOrUpdateAnnouncementRequest
import com.kash4me.data.models.merchant.announcement.create_or_update.CreateOrUpdateAnnouncementResponse
import com.kash4me.data.models.merchant.announcement.get.GetAnnouncementResponse
import com.kash4me.data.models.merchant.assign_cashback.AssignCashbackRequest
import com.kash4me.data.models.merchant.assign_cashback.AssignCashbackResponse
import com.kash4me.data.models.merchant.calculate_cashback.CalculateCashbackRequest
import com.kash4me.data.models.merchant.cashback.CashbackResponseV2
import com.kash4me.data.models.merchant.cashback_code.SendCashbackCodeRequest
import com.kash4me.data.models.merchant.cashback_code.SendCashbackCodeResponse
import com.kash4me.data.models.merchant.delete.DeleteMerchantResponse
import com.kash4me.data.models.merchant.profile.MerchantProfileResponse
import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseRequest
import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseResponse
import com.kash4me.data.models.merchant.registration.MerchantBasicInfoRequest
import com.kash4me.data.models.merchant.rollback_transaction.RollbackTransactionRequest
import com.kash4me.data.models.merchant.rollback_transaction.RollbackTransactionResponse
import com.kash4me.data.models.merchant.sub_user_settings.SubUserResponse
import com.kash4me.data.models.merchant.sub_user_settings.add_sub_user.AddSubUserRequest
import com.kash4me.data.models.merchant.sub_user_settings.delete_sub_user.DeleteSubUserResponse
import com.kash4me.data.models.merchant.sub_user_settings.reset_staff_password.ResetStaffPasswordRequest
import com.kash4me.data.models.merchant.sub_user_settings.reset_staff_password.ResetStaffPasswordResponse
import com.kash4me.data.models.merchant.transaction_by_time.ViewTransactionByTimeResponse
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateRequest
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateResponse
import com.kash4me.data.models.payment_gateway.ConnectYourBankResponse
import com.kash4me.data.models.payment_gateway.PaymentGatewayResponse
import com.kash4me.data.models.payment_gateway.PaymentInformationResponse
import com.kash4me.data.models.request.RefreshAccessTokenRequest
import com.kash4me.data.models.request.RequestCashbackQrRequest
import com.kash4me.data.models.response.RefreshAccessTokenResponse
import com.kash4me.data.models.response.RequestCashbackQrResponse
import com.kash4me.data.models.staff.StaffDetailsResponse
import com.kash4me.data.models.staff.StaffTransactionsResponse
import com.kash4me.data.models.user.*
import com.kash4me.data.models.user.change_password.ChangePasswordRequest
import com.kash4me.data.models.user.fee_settings.FeeSettingsResponse
import com.kash4me.data.models.user.feedback.FeedbackRequest
import com.kash4me.data.models.user.feedback.FeedbackResponse
import com.kash4me.data.models.user.notification_settings.NotificationSettingsResponse
import com.kash4me.data.models.user.notification_settings.UpdateNotificationSettingsRequest
import com.kash4me.data.models.user.timezone.TimezoneResponse
import com.kash4me.network.EndPoint.API.ADD_CASHBACK_URL
import com.kash4me.network.EndPoint.API.AVAILABLE_COUNTRY
import com.kash4me.network.EndPoint.API.BASE_URL
import com.kash4me.network.EndPoint.API.CHANGE_PASSWORD_URL
import com.kash4me.network.EndPoint.API.CREATE_CASHBACK_TRANSACTION
import com.kash4me.network.EndPoint.API.CUSTOMER_CASHBACK_DETAILS_URL
import com.kash4me.network.EndPoint.API.CUSTOMER_DETAILS_URL
import com.kash4me.network.EndPoint.API.CUSTOMER_REQUEST_QR
import com.kash4me.network.EndPoint.API.CUSTOMER_TOTAL_CASHBACK_URL
import com.kash4me.network.EndPoint.API.DELETE_SUB_USER
import com.kash4me.network.EndPoint.API.FETCH_CUSTOMER_DETAILS_FROM_MERCHANT
import com.kash4me.network.EndPoint.API.FORGET_PASSWORD_URL
import com.kash4me.network.EndPoint.API.GET_CASHBACK_INFO_URL
import com.kash4me.network.EndPoint.API.GET_MERCHANTS_URL
import com.kash4me.network.EndPoint.API.GET_TRANSACTION_SUMMARY
import com.kash4me.network.EndPoint.API.INFO_BOX
import com.kash4me.network.EndPoint.API.LOGIN_URL
import com.kash4me.network.EndPoint.API.MERCHANT_BRANCH_LIST_URL
import com.kash4me.network.EndPoint.API.MERCHANT_CUSTOMER_LIST_URL
import com.kash4me.network.EndPoint.API.MERCHANT_DETAILS_URL
import com.kash4me.network.EndPoint.API.MERCHANT_FIRST_SHOP_URL
import com.kash4me.network.EndPoint.API.MERCHANT_VIEW_TRANSACTION_DETAILS_URL
import com.kash4me.network.EndPoint.API.NOTIFICATION_SETTINGS
import com.kash4me.network.EndPoint.API.POST_BUSINESS_USER_AND_INFO_URL
import com.kash4me.network.EndPoint.API.POST_CUSTOMER_DETAIL_URL
import com.kash4me.network.EndPoint.API.REFRESH_ACCESS_TOKEN
import com.kash4me.network.EndPoint.API.RESENT_OTP_URL
import com.kash4me.network.EndPoint.API.RESET_PASSWORD
import com.kash4me.network.EndPoint.API.ROLLBACK_TRANSACTION
import com.kash4me.network.EndPoint.API.STAFF_TRANSACTIONS
import com.kash4me.network.EndPoint.API.STAFF_TRANSACTION_DETAILS
import com.kash4me.network.EndPoint.API.TAGS
import com.kash4me.network.EndPoint.API.UPDATE_CUSTOMER_DETAILS_URL
import com.kash4me.network.EndPoint.API.USER_REGISTRATION_URL
import com.kash4me.network.EndPoint.API.VERIFY_EMAIL_URL
import com.kash4me.utils.PaymentMethod
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface ApiServices {

    @POST(USER_REGISTRATION_URL)
    fun registerUser(@Body params: HashMap<String, Any>): Call<SuccessResponse>

    @POST(LOGIN_URL)
    fun login(@Body request: HashMap<String, String>): Call<LoginResponse>

    @POST(VERIFY_EMAIL_URL)
    fun verifyEmail(@Body params: HashMap<String, String>): Call<VerifyEmailResponse>

    @POST(RESENT_OTP_URL)
    fun resendOTP(@Body params: HashMap<String, String>): Call<SuccessResponse>

    @POST(FORGET_PASSWORD_URL)
    fun forgetPassword(@Body params: HashMap<String, String>): Call<SuccessResponse>

    @POST(CHANGE_PASSWORD_URL)
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<SuccessResponse>

    @POST(REFRESH_ACCESS_TOKEN)
    fun refreshAccessToken(@Body request: RefreshAccessTokenRequest): Call<RefreshAccessTokenResponse>

    @POST(POST_CUSTOMER_DETAIL_URL)
    fun postCustomerDetails(
        @Header("Authorization") token: String,
        @Body params: HashMap<String, String>
    ): Call<CustomerResponse>


    @POST(POST_BUSINESS_USER_AND_INFO_URL)
    suspend fun postBusinessUserDetails(
        @Header("Authorization") token: String,
        @Body params: HashMap<String, Any>
    ): Response<BusinessInfoResponse>

    @Multipart
    @POST("merchant/registration/")
    suspend fun postMerchantBasicInfo(
        @Header("Authorization") token: String,
        @Part("basic_info") basicInfo: MerchantBasicInfoRequest.BasicInfo,
        @Part("business_info") businessInfo: MerchantBasicInfoRequest.BusinessInfo,
        @Part file: MultipartBody.Part,
        @Part("tags") tags: List<String>
    ): Response<BusinessInfoResponse>

    @POST(ADD_CASHBACK_URL)
    fun addCashBack(
        @Path("merchant_shop_id") merchantShopID: Int,
        @Header("Authorization") token: String,
        @Body params: HashMap<String, Any>
    ): Call<CashbackResponseV2>

    @GET(GET_MERCHANTS_URL)
    fun getMerchants(
        @QueryMap filterOptions: Map<String, String>,
        @Header("Authorization") token: String,
    ): Call<NearByMerchantsResponse>

    @GET("customers/get-merchant-details/{merchant_shop_id}/")
    fun getMerchantDetailsWithCustomerInfo(
        @Path("merchant_shop_id") merchantShopID: Int,
        @Header("Authorization") token: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
    ): Call<com.kash4me.data.models.customer.view_merchant_details.MerchantDetailsResponse>


    @POST(GET_CASHBACK_INFO_URL)
    fun getCashBackInfo(
        @Header("Authorization") token: String,
        @Body params: HashMap<String, Any>
    ): Call<CashBackAmountResponse>

    @POST("merchant/get-cashback-info/")
    suspend fun calculateCashbackAmount(
        @Header("Authorization") token: String,
        @Body request: CalculateCashbackRequest
    ): Response<CashBackAmountResponse>

    @POST(GET_CASHBACK_INFO_URL)
    fun getCashbackValue(
        @Header("Authorization") token: String,
        @Body request: AssignCashbackRequest
    ): Call<AssignCashbackResponse>

    @POST(CREATE_CASHBACK_TRANSACTION)
    fun createCashBackTransaction(
        @Path("merchant_shop_id") merchantShopID: Int,
        @Header("Authorization") token: String,
        @Body params: HashMap<String, Any>
    ): Call<CashBackSuccessResponse>

    @GET(MERCHANT_FIRST_SHOP_URL)
    fun fetchMerchantDetailsForCashBack(
        @Header("Authorization") token: String
    ): Call<Merchant>


    @GET(CUSTOMER_CASHBACK_DETAILS_URL)
    fun fetchCustomerCashBackDetails(
        @Header("Authorization") token: String,
        @QueryMap filterOptions: Map<String, String>
    ): Call<CustomerCashBackDetailsResponse>

    @GET(CUSTOMER_CASHBACK_DETAILS_URL)
    suspend fun fetchCustomerCashbacks(
        @Header("Authorization") token: String,
    ): Response<CustomerCashBackDetailsResponse>

    @GET(CUSTOMER_TOTAL_CASHBACK_URL)
    fun fetchCustomerTotalCashBack(
        @Header("Authorization") token: String,
        @QueryMap filterOptions: Map<String, String>
    ): Call<TotalCashBackResponse>

    @GET(CUSTOMER_TOTAL_CASHBACK_URL)
    suspend fun fetchCustomerTransactionDetails(@Header("Authorization") token: String): Response<CustomerTransactionsResponseV2>

    @GET(CUSTOMER_TOTAL_CASHBACK_URL)
    fun fetchCustomerTransactions(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Call<CustomerTransactionsResponse>

    @POST("customers/transactions/")
    fun createCustomerTransaction(
        @Header("Authorization") token: String,
        @Body request: CreateTransactionRequest
    ): Call<CreateTransactionResponse>

    @GET("customers/processing-transactions/")
    suspend fun getProcessingTransactions(
        @Header("Authorization") token: String
    ): Response<List<ProcessingTransaction>?>

    @POST("customers/transactions/")
    suspend fun withdrawAmount(
        @Header("Authorization") token: String,
        @Body request: HashMap<String, Any>
    ): Response<WithdrawAmountResponse>

    @POST("customers/claim-coupon/")
    suspend fun claimCoupon(
        @Header("Authorization") token: String,
        @Body request: ClaimCouponRequest
    ): Response<ClaimCouponResponse>

    @GET(CUSTOMER_DETAILS_URL)
    fun fetchCustomerDetails(
        @Header("Authorization") token: String,
    ): Call<CustomerDetailsResponse>

    @GET(CUSTOMER_DETAILS_URL)
    suspend fun fetchCustomerProfile(
        @Header("Authorization") token: String,
    ): Resource<CustomerDetailsResponse>

    @POST(CUSTOMER_REQUEST_QR)
    fun requestQr(
        @Header("Authorization") token: String,
        @Body request: RequestCashbackQrRequest
    ): Call<RequestCashbackQrResponse>

    @POST("customers/request-cashback-qr/")
    suspend fun generateQrCodeToRequestCashback(
        @Header("Authorization") token: String,
        @Body request: RequestCashbackQrRequest
    ): Response<RequestCashbackQrResponse>

    @GET("customers/get-merchant/transaction-list/{merchant_id}/")
    suspend fun getTransactionsAccordingToMerchant(
        @Header("Authorization") token: String,
        @Path("merchant_id") merchantId: Int
    ): Response<TransactionsAccordingToMerchantResponse>

    @GET("customers/get-merchant/transaction-details/{txn_id}/")
    suspend fun getTransactionDetailsForReturningPurchase(
        @Header("Authorization") token: String,
        @Path("txn_id") transactionId: Int
    ): Response<TransactionDetailsForReturningPurchase>

    @PATCH(UPDATE_CUSTOMER_DETAILS_URL)
    fun updateCustomerDetails(
        @Header("Authorization") token: String,
        @Body params: HashMap<String, String>
    ): Call<CustomerDetailsResponse>

    @PATCH(UPDATE_CUSTOMER_DETAILS_URL)
    fun updateCustomerDetails(
        @Header("Authorization") token: String,
        @Body request: CustomerProfileUpdateRequest
    ): Call<CustomerDetailsResponse>

    @PATCH(UPDATE_CUSTOMER_DETAILS_URL)
    suspend fun updateCustomerDetails(
        @Header("Authorization") token: String,
        @Body params: HashMap<String, Any?>
    ): Response<CustomerDetailsResponse>

    @POST("customers/pay-by-kash4me/")
    suspend fun payByKash4me(
        @Header("Authorization") token: String,
        @Body request: PayByKash4meRequest
    ): Response<PayByKash4meResponse>

    @POST("customers/delete-account/")
    suspend fun deleteCustomerAccount(@Header("Authorization") token: String): Response<DeleteCustomerResponse>

    @GET("customers/get-merchant-announcement-list/")
    suspend fun getAnnouncementsForCustomer(
        @Header("Authorization") token: String,
        @Query("search") searchQuery: String
    ): Response<AnnouncementsResponse>

    @GET(GET_TRANSACTION_SUMMARY)
    suspend fun getTransactionSummary(
        @Header("Authorization") token: String,
        @Query("merchant_shop_id") merchantId: Int?,
    ): Response<MerchantTransactionSummaryResponse>


    @GET(MERCHANT_BRANCH_LIST_URL)
    fun getMerchantBranchList(
        @Header("Authorization") token: String,
        @QueryMap filterOptions: Map<String, String>?,
    ): Call<BranchListResponse>


    @GET(MERCHANT_VIEW_TRANSACTION_DETAILS_URL)
    fun getMerchantTransactionDetails(
        @Header("Authorization") token: String,
//        @Query("mode") mode: String,
//        @Query("merchant_shop_id") shopId: Int,
        @QueryMap filterOptions: HashMap<String, Any>,
    ): Call<ViewTransactionByTimeResponse>


    @GET(MERCHANT_CUSTOMER_LIST_URL)
    fun getMerchantCustomerList(
        @Header("Authorization") token: String,
        @QueryMap filterOptions: Map<String, String>
    ): Call<CustomerListResponse>

    @GET(MERCHANT_DETAILS_URL)
    fun fetchMerchantDetails(
        @Header("Authorization") token: String,
    ): Call<MerchantDetailsResponse>

    @GET(MERCHANT_DETAILS_URL)
    fun fetchMerchantProfileDetails(
        @Header("Authorization") token: String,
    ): Call<MerchantProfileResponse>

    @PATCH("merchant/update-merchant/{merchant_shop_id}/")
    fun updateMerchantDetails(
        @Header("Authorization") token: String,
        @Body params: HashMap<String, Any>,
        @Path("merchant_shop_id") merchantShopID: Int,
    ): Call<MerchantDetailsResponse>

    @PATCH("merchant/update-merchant/{merchant_shop_id}/")
    fun updateMerchantDetailsInRegistration(
        @Header("Authorization") token: String,
        @Body params: HashMap<String, Any?>,
        @Path("merchant_shop_id") merchantShopID: Int,
    ): Call<MerchantProfileUpdateResponse>

    @PATCH("merchant/update-merchant/{merchant_shop_id}/")
    fun updateMerchantProfileDetails(
        @Header("Authorization") token: String,
        @Path("merchant_shop_id") merchantShopID: Int,
        @Body params: HashMap<String, Any?>,
    ): Call<MerchantProfileResponse>

    @PATCH("merchant/update-merchant/{merchant_shop_id}/")
    suspend fun updateMerchantProfile(
        @Header("Authorization") token: String,
        @Path("merchant_shop_id") merchantShopID: Int,
        @Body params: HashMap<String, Any?>,
    ): Response<MerchantProfileResponse>

    @Multipart
    @PATCH("merchant/update-merchant/{merchant_shop_id}/")
    suspend fun updateMerchantProfileLogo(
        @Header("Authorization") token: String,
        @Path("merchant_shop_id") merchantShopID: Int,
        @Part logo: MultipartBody.Part
    ): Response<MerchantProfileResponse>

    @PATCH("merchant/update-merchant/{merchant_shop_id}/")
    fun updateMerchantDetails(
        @Header("Authorization") token: String,
        @Body request: MerchantProfileUpdateRequest,
        @Path("merchant_shop_id") merchantShopID: Int,
    ): Call<MerchantProfileUpdateResponse>

    @GET("merchant/active-cashback-settings/{merchant_shop_id}/")
    fun getActiveCashBackSetting(
        @Path("merchant_shop_id") merchantShopID: Int,
        @Header("Authorization") token: String,
    ): Call<ActiveCashbackSettings>

    @GET("merchant/active-cashback-settings/{merchant_shop_id}/")
    suspend fun fetchActiveCashbackSettings(
        @Path("merchant_shop_id") merchantShopId: Int,
        @Header("Authorization") token: String,
    ): Response<ActiveCashbackSettings>

    @GET(FETCH_CUSTOMER_DETAILS_FROM_MERCHANT)
    fun getCustomerDetailsFromMerchant(
        @Path("customer_id") customerId: Int,
        @Header("Authorization") token: String,
    ): Call<CustomerDetailsResponseDto>

    @POST(ROLLBACK_TRANSACTION)
    fun rollbackTransaction(
        @Header("Authorization") token: String,
        @Body request: RollbackTransactionRequest
    ): Call<RollbackTransactionResponse>

    @POST("merchant/coupon/")
    suspend fun sendCashbackCode(
        @Header("Authorization") token: String,
        @Body request: SendCashbackCodeRequest
    ): Response<SendCashbackCodeResponse>

    @POST("merchant/purchase-return/")
    suspend fun returnPurchase(
        @Header("Authorization") token: String,
        @Body request: ReturnPurchaseRequest
    ): Response<ReturnPurchaseResponse>

    @POST("merchant/pay-by-kash4me/")
    suspend fun acceptKash4mePayment(
        @Header("Authorization") token: String,
        @Body request: AcceptKash4mePaymentRequest
    ): Response<AcceptKash4mePaymentResponse>

    @POST("merchant/delete-account/")
    suspend fun deleteMerchantAccount(@Header("Authorization") token: String): Response<DeleteMerchantResponse>

    @POST("merchant/merchant-announcement/")
    suspend fun createOrUpdateYourAnnouncement(
        @Header("Authorization") token: String,
        @Body request: CreateOrUpdateAnnouncementRequest
    ): Response<CreateOrUpdateAnnouncementResponse>

    @GET("merchant/merchant-announcement/")
    suspend fun getYourAnnouncement(@Header("Authorization") token: String): Response<GetAnnouncementResponse>

    /* Sub User Settings */

    @GET("merchant/staff/")
    suspend fun getSubUsers(@Header("Authorization") token: String): Response<List<SubUserResponse>>

    @POST("merchant/staff/")
    suspend fun addSubUser(
        @Header("Authorization") token: String,
        @Body request: AddSubUserRequest
    ): Response<List<SubUserResponse>?>

    @HTTP(method = "DELETE", path = DELETE_SUB_USER, hasBody = true)
    suspend fun deleteSubUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<DeleteSubUserResponse?>

    @POST(RESET_PASSWORD)
    suspend fun resetStaffUserPassword(
        @Header("Authorization") token: String,
        @Body request: ResetStaffPasswordRequest
    ): Response<ResetStaffPasswordResponse>

    // Users
    @GET(INFO_BOX)
    fun getInfoBox(@Header("Authorization") token: String): Call<InfoBoxResponse>

    @GET(AVAILABLE_COUNTRY)
    fun getAvailableCountries(@Header("Authorization") token: String): Call<CountryResponse>

    @GET(NOTIFICATION_SETTINGS)
    suspend fun getNotificationSettings(@Header("Authorization") token: String): Response<NotificationSettingsResponse>

    @POST(NOTIFICATION_SETTINGS)
    suspend fun updateNotificationSettings(
        @Header("Authorization") token: String,
        @Body request: UpdateNotificationSettingsRequest
    ): Response<NotificationSettingsResponse>

    @POST("users/feedback/")
    suspend fun sendFeedback(
        @Header("Authorization") token: String,
        @Body request: FeedbackRequest
    ): Response<FeedbackResponse>

    // Staff
    @GET(STAFF_TRANSACTIONS)
    suspend fun getTransactions(@Header("Authorization") token: String): Response<StaffTransactionsResponse>

    @GET(STAFF_TRANSACTION_DETAILS)
    suspend fun getTransactionDetails(@Header("Authorization") token: String): Response<StaffDetailsResponse>

    @GET(TAGS)
    suspend fun getAvailableTags(@Header("Authorization") token: String): Response<TagResponse>

    // Payment
    @GET("payment/vopay/{payment_method}/connect/")
    suspend fun connectBank(
        @Header("Authorization") token: String,
        @Path("payment_method") paymentMethod: PaymentMethod
    ): Response<ConnectYourBankResponse>

    @GET("payment/user/information/")
    suspend fun getPaymentInformation(
        @Header("Authorization") token: String,
        @Query("payment_method") paymentMethod: PaymentMethod,
    ): Response<List<PaymentInformationResponse>>

    @POST("payment/user/information/")
    suspend fun createPaymentInformation(
        @Header("Authorization") token: String,
        @Body request: HashMap<String, Any>
    ): Response<List<PaymentInformationResponse>>

    @PATCH("payment/user/{id}/information/update/")
    suspend fun updatePaymentInformation(
        @Header("Authorization") token: String,
        @Path("id") paymentId: Int,
        @Body request: HashMap<String, Any>
    ): Response<List<PaymentInformationResponse>>

    @GET("users/payment-gateway-lists/")
    suspend fun getPaymentGateways(@Header("Authorization") token: String): Response<List<PaymentGatewayResponse>>

    // User
    @GET("users/fee-settings/")
    suspend fun getFeeSettings(
        @Header("Authorization") token: String,
        @Query("fee_type") feeType: Int,
    ): Response<FeeSettingsResponse>

    // User
    @GET("users/timezone/")
    suspend fun getTimezones(
        @Header("Authorization") token: String,
        @Query("country_code") countryCode: String,
    ): Response<TimezoneResponse>

    companion object {

        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor,
            notFoundInterceptor: NotFoundInterceptor
        ): ApiServices {

            val builder = OkHttpClient.Builder()
            builder
                .addInterceptor(getLoggingInterceptor())
                .addInterceptor(networkConnectionInterceptor)
                .addInterceptor(notFoundInterceptor)
//                .addInterceptor(AuthorizationInterceptor())
                .authenticator(TokenRefreshAuthenticator())
                .build()

            val okHttpClient = builder.build()
            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiServices::class.java)
        }

        private fun getLoggingInterceptor(): HttpLoggingInterceptor {
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            return interceptor
        }

    }


}

class EndPoint {
    object API {

        const val BASE_URL = "https://api.kash4me.com/api/v1/"
//        const val BASE_URL = "https://a7db-110-34-1-194.ngrok-free.app/api/v1/"

        // Customers
        const val CUSTOMER_REQUEST_QR = "customers/request-cashback-qr/"
        const val CUSTOMER_CASHBACK_DETAILS_URL = "customers/cashback-info/"
        const val CUSTOMER_TOTAL_CASHBACK_URL = "customers/transactions/"
        const val CUSTOMER_DETAILS_URL = "customers/customer-details/"
        const val UPDATE_CUSTOMER_DETAILS_URL = "customers/update-details/"
        const val GET_MERCHANTS_URL = "customers/get-merchants/"
        const val POST_CUSTOMER_DETAIL_URL = "customers/registration/"

        // Users
        const val INFO_BOX = "users/info-box/"
        const val USER_REGISTRATION_URL = "users/registration/"
        const val LOGIN_URL = "users/login/"
        const val VERIFY_EMAIL_URL = "users/verify-email/"
        const val RESENT_OTP_URL = "users/resend/verification-code/"
        const val FORGET_PASSWORD_URL = "users/password-reset/"
        const val CHANGE_PASSWORD_URL = "users/password-change/"
        const val AVAILABLE_COUNTRY = "users/available-country/"
        const val NOTIFICATION_SETTINGS = "users/notification-settings/"
        const val TAGS = "users/tags/"
        const val REFRESH_ACCESS_TOKEN = "users/jwt/refresh/"

        // Merchant
        const val ROLLBACK_TRANSACTION = "merchant/rollback-transaction/"
        const val DELETE_SUB_USER = "merchant/staff/{id}/"
        const val RESET_PASSWORD = "merchant/staff/password-reset/"
        const val FETCH_CUSTOMER_DETAILS_FROM_MERCHANT = "merchant/customer-details/{customer_id}/"
        const val CREATE_CASHBACK_TRANSACTION = "merchant/create-transaction/{merchant_shop_id}/"
        const val GET_TRANSACTION_SUMMARY = "merchant/get-transaction-summary/"
        const val POST_BUSINESS_USER_AND_INFO_URL = "merchant/registration/"
        const val ADD_CASHBACK_URL = "merchant/add-cashback-settings/{merchant_shop_id}/"
        const val GET_CASHBACK_INFO_URL = "merchant/get-cashback-info/"
        const val MERCHANT_FIRST_SHOP_URL = "merchant/first-shop/"
        const val MERCHANT_BRANCH_LIST_URL = "merchant/view-branch-list/"
        const val MERCHANT_VIEW_TRANSACTION_DETAILS_URL = "merchant/view-transaction-by-timeframe/"
        const val MERCHANT_CUSTOMER_LIST_URL = "merchant/customer-list/"
        const val MERCHANT_DETAILS_URL = "merchant/detail-merchant/"

        // Staff
        const val STAFF_TRANSACTIONS = "staff-merchant/transactions/list/"
        const val STAFF_TRANSACTION_DETAILS = "staff-merchant/details/"

        // Google Places API
        const val GOOGLE_PLACES_BASE_URL = "https://maps.google.com/maps/api/"


    }
}