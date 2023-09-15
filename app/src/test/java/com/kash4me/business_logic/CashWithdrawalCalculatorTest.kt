package com.kash4me.business_logic

import com.google.common.truth.Truth
import com.kash4me.data.models.user.UserType
import com.kash4me.data.models.user.fee_settings.FeeSettingsResponse
import org.junit.Test

class CashWithdrawalCalculatorTest {

    @Test
    fun shouldReturnFalse_calculateWithdrawAmountForMerchant() {

        val amountToBeWithdrawn = CashWithdrawalCalculator().calculateAmount(
            withdrawAmount = 100.00,
            userType = UserType.MERCHANT,
            feeSettingsResponse = FeeSettingsResponse(
                commissionPercentage = "10", customerFee = "1", merchantFee = "1"
            )
        )

        Truth.assertThat(amountToBeWithdrawn).isNotEqualTo(5)

    }

    @Test
    fun shouldReturnTrue_calculateWithdrawAmountForMerchant() {

        val amountToBeWithdrawn = CashWithdrawalCalculator().calculateAmount(
            withdrawAmount = 100.00,
            userType = UserType.MERCHANT,
            feeSettingsResponse = FeeSettingsResponse(
                commissionPercentage = "10", customerFee = "1", merchantFee = "1"
            )
        )

        Truth.assertThat(amountToBeWithdrawn).isEqualTo(111)

    }


    @Test
    fun shouldReturnFalse_calculateWithdrawAmountForCustomer() {

        val amountToBeWithdrawn = CashWithdrawalCalculator().calculateAmount(
            withdrawAmount = 100.00,
            userType = UserType.CUSTOMER,
            feeSettingsResponse = FeeSettingsResponse(
                commissionPercentage = "10", customerFee = "1", merchantFee = "1"
            )
        )

        Truth.assertThat(amountToBeWithdrawn).isEqualTo(99)

    }

}