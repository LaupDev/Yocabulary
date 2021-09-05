package com.laupdev.yocabulary.model.practice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.laupdev.yocabulary.ui.questions.Question
import com.laupdev.yocabulary.ui.questions.QuestionType
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

class PracticeViewModel : ViewModel() {

    private val _practiceProgress = MutableLiveData(1)
    val practiceProgress: LiveData<Int>
        get() = _practiceProgress

    val questions = mutableListOf(
        Question(QuestionType.MATCH_MEANING,"LOL_1"),
        Question(QuestionType.MATCH_MEANING,"LOL_2"),
        Question(QuestionType.MATCH_MEANING,"LOL_3"),
        Question(QuestionType.MATCH_MEANING,"LOL_4")
    )

    var currentQuestionIndex = 0

    private fun increaseProgressForProgressBar() {
        (_practiceProgress.value ?: 1).apply {
            if (this < questions.size) {
                _practiceProgress.value = this + 1
            }
        }
        Timber.i(_practiceProgress.value.toString())
    }

    fun nextQuestion() {
        increaseProgressForProgressBar()
        currentQuestionIndex++
    }

}