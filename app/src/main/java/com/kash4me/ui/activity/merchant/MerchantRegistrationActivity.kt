package com.kash4me.ui.activity.merchant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.kash4me.merchant.R
import com.kash4me.utils.custom_views.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MerchantRegistrationActivity : AppCompatActivity() {

    var customDialogClass: CustomProgressDialog = CustomProgressDialog(this)

    var goToCashBack: Boolean = false

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merchant_registration)
        goToCashBack = intent.getBooleanExtra("goToCashBack", false)

//        setupToolbarWithNavController()

    }

    private fun setupToolbarWithNavController() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                as NavHostFragment
        navController = navHostFragment.findNavController()

        val appBarLayout: AppBarLayout = findViewById(R.id.customAppBar)
        val toolbar: Toolbar = appBarLayout.findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController)
    }

//    override fun onSupportNavigateUp(): Boolean {
//        return navController.navigateUp() || super.onSupportNavigateUp()
//    }

}