package com.laupdev.yocabulary.repository

import com.laupdev.yocabulary.database.AppDatabase
import com.laupdev.yocabulary.database.PracticeProgress
import com.laupdev.yocabulary.database.PracticeType
import com.laupdev.yocabulary.database.WordWithPracticeProgress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeRepository @Inject constructor(val database: AppDatabase) {

    private lateinit var allWordsWithPracticeProgress: List<WordWithPracticeProgress>

    suspend fun getWordsForPractice(practiceType: PracticeType): List<WordWithPracticeProgress> {
        val wordsForPractice = mutableListOf<WordWithPracticeProgress>()
        getAllWordsWithPracticeProgress().forEach {
            if (wordsForPractice.size == 5) {
                return wordsForPractice
            }
            if (it.wordProgress.shouldBePracticed(practiceType)) {
                wordsForPractice.add(it)
            }
        }
        return wordsForPractice
    }

    private suspend fun getAllWordsWithPracticeProgress(): List<WordWithPracticeProgress> {
        if (!this::allWordsWithPracticeProgress.isInitialized) {
            allWordsWithPracticeProgress = database.practiceProgressDao().getAllWordsWithPracticeProgress()
        }
        return allWordsWithPracticeProgress
    }
}