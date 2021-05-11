package com.laupdev.yourdictionary.application

import android.app.Application
import com.laupdev.yourdictionary.database.AppDatabase
import com.laupdev.yourdictionary.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DictionaryApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { AppRepository(database.wordDao()) }
}