package com.laupdev.yocabulary.repository

import androidx.annotation.WorkerThread
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.database.WordDao
import kotlinx.coroutines.flow.Flow

class AppRepository(private val wordDao: WordDao) {

    val allWords: Flow<List<Word>> = wordDao.getAllWords()

    fun getWord(word: String) = wordDao.getWordByName(word)

    fun getWordById(wordId: Int) = wordDao.getWordById(wordId)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(word: Word) {
        wordDao.insert(word)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun removeWordById(wordId: Int) {
        wordDao.removeWordById(wordId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(word: Word) {
        wordDao.update(word)
    }

}