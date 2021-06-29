package com.laupdev.yocabulary.application

import android.app.Application
import com.laupdev.yocabulary.database.AppDatabase
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DictionaryApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { AppRepository(database.wordDao(), database.partOfSpeechDao(), database.meaningDao()) }
}