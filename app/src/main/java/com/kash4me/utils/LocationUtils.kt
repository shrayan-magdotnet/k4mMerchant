package com.kash4me.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.kash4me.utils.extensions.getEmptyIfNull
import timber.log.Timber
import java.io.IOException
import java.util.*


class LocationUtils {

    companion object {
        private const val TAG = "LocationUtils"
    }

    fun getCurrentLocation(context: Context): CurrentLocation? {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val isFineLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val isCoarseLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (isCoarseLocationPermissionGranted && isFineLocationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                Log.d(TAG, "Location detected -> $location")
                CurrentLocation.lat = location?.latitude ?: 0.0
                CurrentLocation.lng = location?.longitude ?: 0.0
                Log.d(TAG, "Lat: ${CurrentLocation.lat} Long: ${CurrentLocation.lng}")
                getCountryName(context)
            }
        } else {
            return null
        }

        return CurrentLocation

    }

    fun updateCurrentLocation(context: Context) {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val isFineLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val isCoarseLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (isCoarseLocationPermissionGranted && isFineLocationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                Log.d(TAG, "Location detected -> $location")
                CurrentLocation.lat = location?.latitude ?: 0.0
                CurrentLocation.lng = location?.longitude ?: 0.0
                Log.d(TAG, "Lat: ${CurrentLocation.lat} Long: ${CurrentLocation.lng}")
                getCountryName(context)
            }
        }

    }

    private fun getCountryName(context: Context) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>?
        try {

            addresses = geocoder.getFromLocation(CurrentLocation.lat, CurrentLocation.lng, 1)
            Timber.d("Latitude -> ${CurrentLocation.lat}")
            Timber.d("Longitude -> ${CurrentLocation.lng}")

            Timber.d("getCountryName: $addresses")

            if (addresses != null && addresses.isNotEmpty()) {
                addresses.getOrNull(0)?.countryName?.let { CurrentLocation.country = it }
                addresses.getOrNull(0)?.postalCode?.let { CurrentLocation.postalCode = it }
                addresses.getOrNull(0)?.countryCode?.let { CurrentLocation.countryCode = it }
            }

        } catch (ignored: IOException) {
            Log.d("SplashActivity", "getCountryName: Error: ${ignored.localizedMessage}")
        }
    }

    @Throws(Exception::class)
    fun getAddress(context: Context, zipCode: String, locale: Locale? = null): Address? {
        val geocoder = if (locale != null)
            Geocoder(context, locale)
        else
            Geocoder(context)
        try {
            val addresses: List<Address>? = geocoder.getFromLocationName(zipCode, 10)
            Timber.d("Addresses -> $addresses")
            return if (addresses != null && addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                // Use the address as needed
                val message = String.format(
                    "Latitude: %f, Longitude: %f", address.latitude, address.longitude
                )
                Timber.d("getAddress: $message")
                address
            } else {
                Timber.d("Caught exception: Unable to geocode zipcode")
                // Display appropriate message when Geocoder services are not available
                null
            }
        } catch (e: IOException) {
            // handle exception
            Timber.d("Caught exception: ${e.message}")
            return null
        }
    }

    fun printLocationData(context: Context, data: Intent) {

        val place: Place = Autocomplete.getPlaceFromIntent(data)
        Log.e(
            "Data",
            "Place_Data: Name: " + place.getName() + "\tLatLng: " + place.getLatLng() + "\tAddress: " + place.getAddress() + "\tAddress Component: " + place.getAddressComponents()
        )

        try {
            val geocoder = Geocoder(context, Locale.US);
            val addresses: MutableList<Address?>?

            try {
                addresses = geocoder.getFromLocation(
                    place.latLng.latitude, place.latLng.longitude, 1
                )

                // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                val address1: String? = addresses?.get(0)?.getAddressLine(0)

                val city: String? = addresses?.get(0)?.locality
                val state: String? = addresses?.get(0)?.adminArea
                val country: String? = addresses?.get(0)?.countryName
                val postalCode: String? = addresses?.get(0)?.postalCode

                Log.e("Address1: ", "" + address1)
                Log.e("AddressCity: ", "" + city)
                Log.e("AddressState: ", "" + state)
                Log.e("AddressCountry: ", "" + country)
                Log.e("AddressPostal: ", "" + postalCode)
                Log.e("AddressLatitude: ", "" + place.getLatLng().latitude)
                Log.e("AddressLongitude: ", "" + place.getLatLng().longitude)

            } catch (e: IOException) {
                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace();
            //setMarker(latLng);
        }
    }

    fun getPostalCode(context: Context, data: Intent): String? {

        val place: Place = Autocomplete.getPlaceFromIntent(data)
        Log.e(
            "LocationUtils",
            "Name: " + place.getName() + "\tLatLng: " + place.getLatLng() + "\tAddress: " + place.getAddress() + "\tAddress Component: " + place.getAddressComponents()
        )

        return try {

            try {
                val addresses =
                    Geocoder(context, Locale.US).getFromLocation(
                        place.latLng.latitude, place.latLng.longitude, 1
                    )

                val postalCode: String? = addresses?.getOrNull(0)?.postalCode
                Log.d("AddressPostal: ", "" + postalCode)
                postalCode

            } catch (e: IOException) {
                e.printStackTrace()
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    fun detectCountry(context: Context, latitude: Double, longitude: Double): CountryDetails? {

        Timber.d("Latitude: $latitude | Longitude: $longitude")

        // Use Geocoder to get country name from latitude and longitude
        val geocoder = Geocoder(context, Locale.US)
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses?.isNotEmpty() == true) {
            val countryName = addresses.getOrNull(0)?.countryName.getEmptyIfNull()
            val countryCode = addresses.getOrNull(0)?.countryCode.getEmptyIfNull()
            Timber.d("Detected country name: $countryName")
            Timber.d("Detected country code: $countryCode")

            return CountryDetails(name = countryName, code = countryCode)
        }

        return null

    }

    class CountryDetails(val name: String, val code: String)

}