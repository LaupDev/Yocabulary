package com.laupdev.yocabulary.repository

import com.laupdev.yocabulary.database.AppDatabase
import com.laupdev.yocabulary.database.MeaningWithMeaningPracticeProgress
import com.laupdev.yocabulary.database.WordWithWritingPracticeProgress
import com.laupdev.yocabulary.ui.practice.questions.MeaningQuestion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeRepository @Inject constructor(val database: AppDatabase) {

    suspend fun getWordsForWritingPractice(itemCount: Int): List<WordWithWritingPracticeProgress> {
        val wordsForPractice = mutableListOf<WordWithWritingPracticeProgress>()
        database.practiceProgressDao().getAllWordsWithWritingPracticeProgress().forEach {
            if (wordsForPractice.size == itemCount) {
                return wordsForPractice
            }
            if (it.wordProgress.shouldBePracticed()) {
                wordsForPractice.add(it)
            }
        }
        return wordsForPractice
    }

    suspend fun getMeaningQuestions(itemCount: Int): List<MeaningQuestion> {
        val meaningQuestions = mutableListOf<MeaningQuestion>()
        database.practiceProgressDao().getAllMeaningsWithMeaningPracticeProgress().forEach { meaningWithProgress ->
            if (meaningWithProgress.meaning.meaning.isNotEmpty() && meaningWithProgress.meaningPracticeProgress.shouldBePracticed()) {
                val rightAnswer = meaningWithProgress.meaning.word
                val possibleAnswers = getPossibleAnswers(rightAnswer)

                val meaningQuestion = MeaningQuestion(
                    meaningWithProgress.meaning.meaning,
                    possibleAnswers,
                    rightAnswer
                )
                meaningQuestions.add(meaningQuestion)
            }
            if (meaningQuestions.size == itemCount) {
                return meaningQuestions
            }
        }
        return meaningQuestions
    }

    private suspend fun getPossibleAnswers(rightAnswer: String): List<String> {
        val randomWords = database.wordDao().getTenWords().map { it.word }
        return randomWords.shuffled().filter { it != rightAnswer }.take(3).toMutableList().let {
            it.add(rightAnswer)
            it.shuffled()
        }
    }

}