package com.laupdev.yocabulary

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle

class SearchableActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)

        handleIntent(intent)

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        println("-------------------INTENT-------------")
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                println("-------------------SEARCH-------------: " + query)
            }
        }
    }
}