package com.kash4me.data.models.merchant.cashback

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CashbackResponseTest {

    @Test
    fun hasNoTags_returnsEmptyResult() {

        val cashbackResponse =
            CashbackResponse(
                merchantDetails = CashbackResponse.MerchantDetails(
                    tags = listOf(CashbackResponse.MerchantDetails.Tag(name = ""))
                )
            )

        val actualResult = cashbackResponse.merchantDetails?.getTagNames()
        val expectedResult = ""
        assertThat(actualResult).isEqualTo(expectedResult)
        println("Tags -> $actualResult")

    }

    @Test
    fun hasTags_returnsTagsSeparatedByComma() {

        val cashbackResponse =
            CashbackResponse(
                merchantDetails = CashbackResponse.MerchantDetails(
                    tags = listOf(
                        CashbackResponse.MerchantDetails.Tag(name = "Tag1"),
                        CashbackResponse.MerchantDetails.Tag(name = "Tag2"),
                        CashbackResponse.MerchantDetails.Tag(name = "Tag3"),
                        CashbackResponse.MerchantDetails.Tag(name = "Tag4")
                    )
                )
            )

        val actualResult = cashbackResponse.merchantDetails?.getTagNames()
        val expectedResult = listOf("Tag1", "Tag2", "Tag3", "Tag4").joinToString()
        println("Tags -> $actualResult")
        assertThat(actualResult).isEqualTo(expectedResult)

    }

}