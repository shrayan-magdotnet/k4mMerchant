package com.kash4me.business_logic

import com.kash4me.data.models.user.UserType
import com.kash4me.data.models.user.fee_settings.FeeSettingsResponse
import com.kash4me.utils.extensions.getZeroIfNull
import timber.log.Timber

class CashWithdrawalCalculator {

    fun calculateAmount(
        withdrawAmount: Double, userType: UserType, feeSettingsResponse: FeeSettingsResponse
    ): Double {

        Timber.d("User Type: ${userType.name}")
        Timber.d("Withdraw Amount: $withdrawAmount")

        when (userType) {
            UserType.MERCHANT -> {

                val commissionPercentage =
                    feeSettingsResponse.commissionPercentage?.toDoubleOrNull()
                val amountAfterAddingCommission = withdrawAmount +
                        (commissionPercentage?.div(100.00).getZeroIfNull() * withdrawAmount)
                Timber.d("Amount after adding commission: $amountAfterAddingCommission")

                val merchantFee = feeSettingsResponse.merchantFee?.toDoubleOrNull().getZeroIfNull()
                val amountAfterAddingFee = amountAfterAddingCommission + merchantFee
                Timber.d("Amount after adding merchant fee: $amountAfterAddingFee")

                return amountAfterAddingFee

            }

            UserType.CUSTOMER -> {

                val customerFee = feeSettingsResponse.customerFee?.toDoubleOrNull().getZeroIfNull()
                val amountCustomerGets = withdrawAmount.minus(customerFee)

                Timber.d("Amount customer gets after deducting withdraw amount")

                return amountCustomerGets

            }

            else -> {

                return withdrawAmount

            }
        }

    }

}