package com.laupdev.yocabulary.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_SELECTED
import com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_UNLABELED
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.databinding.ActivityMainBinding
import com.laupdev.yocabulary.network.CheckNetwork

class MainActivity : AppCompatActivity() {

//    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigation: BottomNavigationView = binding.bottomNavigation

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_practice, R.id.navigation_sets, R.id.navigation_vocabulary, R.id.navigation_settings
//            )
//        )
//
//        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigation.selectedItemId = R.id.navigation_vocabulary
        bottomNavigation.setupWithNavController(navController)
        bottomNavigation.labelVisibilityMode = LABEL_VISIBILITY_UNLABELED

        bottomNavigation.setOnItemReselectedListener {}
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_practice -> {
                    navController.navigate(R.id.practicePageFragment)
                    true
                }
                R.id.navigation_sets -> {
                    navController.navigate(R.id.practicePageFragment)
                    true
                }
                R.id.navigation_vocabulary -> {
                    navController.navigate(R.id.vocabularyHomeFragment)
                    true
                }
                R.id.navigation_settings -> {
                    navController.navigate(R.id.practicePageFragment)
                    true
                }
                else -> {
                    true
                }
            }
        }

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