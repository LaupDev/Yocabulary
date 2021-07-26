package com.laupdev.yocabulary.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.network.CheckNetwork

class MainActivity : AppCompatActivity() {

    companion object {
        var isConnected = false
    }

//    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        val checkNetwork = CheckNetwork(applicationContext)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            checkNetwork.registerNetworkCallback()
//        } else {
//            isConnected = true
//        }
//        applicationContext.deleteDatabase("yocabulary_database")

//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        navController = navHostFragment.navController

//        setupActionBarWithNavController(navController)
    }

//    override fun onSupportNavigateUp(): Boolean {
//        return navController.navigateUp() || super.onSupportNavigateUp()
//    }
}