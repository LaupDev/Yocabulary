package com.laupdev.yocabulary.application

import android.app.Application
import com.laupdev.yocabulary.database.AppDatabase
import com.laupdev.yocabulary.network.DictionaryNet
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DictionaryApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    private val network = DictionaryNet.retrofitService
    val repository by lazy { AppRepository(network, database) }
}