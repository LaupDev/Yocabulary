package com.laupdev.yocabulary.model.practice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.laupdev.yocabulary.repository.PracticeRepository
import com.laupdev.yocabulary.ui.practice.questions.MeaningQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(val repository: PracticeRepository) : ViewModel() {

    private val _practiceProgress = MutableLiveData(1)
    val practiceProgress: LiveData<Int>
        get() = _practiceProgress

    val questions = mutableListOf(
        MeaningQuestion("Blablablablablabla  blablalbalb blablab ablalbalbalal", listOf("blabla", "asdasdasdasddsa", "lololololo", "braaaaa"), "blabla"),
        MeaningQuestion("Lolololo lo lolo ololo lol olol ol lo ool ollo lolo lo olol o", listOf("asdasdasdasddsa", "lololololo", "blabla", "braaaaa"), "lololololo"),
        MeaningQuestion("Brrrrrrrrrrrrraaaaaaaaaaaaa brabrbarbabbab braaaaaaaaaaaaaa brbrabrbarbabrbaba", listOf("blabla", "lololololo", "asdasdasdasddsa", "braaaaa"), "braaaaa"),
        MeaningQuestion("ASasdasdasdas dsadasdasasd", listOf("braaaaa", "blabla", "asdasdasdasddsa", "lololololo"), "asdasdasdasddsa")
    )

    var currentQuestionIndex = 0

    val rightWrongAnswerIndexes = intArrayOf(-1, -1)

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