package com.laupdev.yocabulary.application

import android.app.Application
import com.laupdev.yocabulary.database.AppDatabase
import com.laupdev.yocabulary.network.DictionaryNet
import com.laupdev.yocabulary.repository.VocabularyRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

@HiltAndroidApp
class DictionaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}