package com.laupdev.yocabulary.repository

import com.laupdev.yocabulary.database.AppDatabase
import com.laupdev.yocabulary.database.WordWithWritingPracticeProgress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeRepository @Inject constructor(val database: AppDatabase) {

    private lateinit var allWordsWithWritingPracticeProgresses: List<WordWithWritingPracticeProgress>

//    suspend fun getWordsForPractice(wordsCount: Int): List<WordWithWritingPracticeProgress> {
//        val wordsForPractice = mutableListOf<WordWithWritingPracticeProgress>()
//        getAllWordsWithPracticeProgress().forEach {
//            if (wordsForPractice.size == wordsCount) {
//                return wordsForPractice
//            }
//        }
//        return wordsForPractice
//    }

//    private suspend fun getAllWordsWithPracticeProgress(): List<WordWithWritingPracticeProgress> {
//        if (!this::allWordsWithWritingPracticeProgresses.isInitialized) {
//            allWordsWithWritingPracticeProgresses = database.practiceProgressDao().getAllWordsWithPracticeProgress()
//        }
//        return allWordsWithWritingPracticeProgresses
//    }
}