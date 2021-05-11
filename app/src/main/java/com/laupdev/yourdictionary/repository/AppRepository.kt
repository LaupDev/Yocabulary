package com.laupdev.yourdictionary.repository

import androidx.annotation.WorkerThread
import com.laupdev.yourdictionary.database.Word
import com.laupdev.yourdictionary.database.WordDao
import kotlinx.coroutines.flow.Flow

class AppRepository(private val wordDao: WordDao) {

    val allWords: Flow<List<Word>> = wordDao.getAllWords()

    fun getWord(word: String) = wordDao.getWordByName(word)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(word: Word) {
        wordDao.insert(word)
    }

}