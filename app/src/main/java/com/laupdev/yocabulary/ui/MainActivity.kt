package com.laupdev.yocabulary.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView.*
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

//        applicationContext.deleteDatabase("yocabulary_database")
    }

    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigation: BottomNavigationView = binding.bottomNavigation

        bottomNavigation.selectedItemId = R.id.navigation_vocabulary
        bottomNavigation.setupWithNavController(navController)
        bottomNavigation.setOnItemReselectedListener {}
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_practice -> {
                    navController.navigate(R.id.practicePageFragment)
                    true
                }
                R.id.navigation_sets -> {
                    navController.navigate(R.id.inDevelopmentFragment)
                    true
                }
                R.id.navigation_vocabulary -> {
                    navController.navigate(R.id.vocabularyHomeFragment)
                    true
                }
                R.id.navigation_settings -> {
                    navController.navigate(R.id.inDevelopmentFragment)
                    true
                }
                else -> {
                    true
                }
            }
        }
    }

    fun hideBottomNav() {
        binding.bottomNavigation.visibility = GONE
    }

    fun showBottomNav() {
        binding.bottomNavigation.visibility = VISIBLE
    }
}