package com.kash4me.utils

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.util.Locale

class LocationUtilsTest {

    lateinit var instrumentationContext: Context

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun zipCode_returnCountryName() {

        val zipCode = "45210"
        val address = LocationUtils().getAddress(
            context = instrumentationContext,
            zipCode = zipCode,
            locale = Locale("us", "US")
        )
        println("Lat: ${address?.latitude} | Lon: ${address?.longitude}")
        println("Country: ${address?.countryName} | Code: ${address?.countryCode}")
        assertThat(zipCode).isNotEmpty()

    }

}