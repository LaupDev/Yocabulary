package com.laupdev.yocabulary.model.practice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laupdev.yocabulary.exceptions.NotEnoughWords
import com.laupdev.yocabulary.repository.PracticeRepository
import com.laupdev.yocabulary.ui.practice.questions.MeaningQuestion
import com.laupdev.yocabulary.ui.practice.questions.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(val repository: PracticeRepository) : ViewModel() {

    private val _practiceProgress = MutableLiveData(1)
    val practiceProgress: LiveData<Int>
        get() = _practiceProgress

    private val _questions = MutableLiveData<List<MeaningQuestion>?>()
    val questions: LiveData<List<MeaningQuestion>?>
        get() = _questions

    private val _exceptionHolder = MutableLiveData<Exception?>(null)
    val exceptionHolder: LiveData<Exception?>
        get() = _exceptionHolder

    var currentQuestionIndex = 0

    val rightWrongAnswerIndexes = intArrayOf(-1, -1)

    fun resetData() {
        _practiceProgress.value = 1
        _questions.value = null
        currentQuestionIndex = 0
        rightWrongAnswerIndexes[0] = -1
        rightWrongAnswerIndexes[1] = -1
    }

    fun getMeaningQuestions() {
        viewModelScope.launch {
            if (isEnoughWords()) {
                _questions.value = repository.getMeaningQuestions(10)
            }
        }
    }

    suspend fun isEnoughWords(): Boolean {
        if (repository.getWordsCountMax5() < 5) {
            _exceptionHolder.value = NotEnoughWords("Not enough words in database to start practice")
            return false
        }
        return true
    }

    fun getQuestions(): List<Question> {
        return questions.value ?: listOf()
    }

    fun nextQuestion() {
        increaseProgressForProgressBar()
        currentQuestionIndex++
    }

    private fun increaseProgressForProgressBar() {
        (_practiceProgress.value ?: 1).apply {
            if (this < questions.value?.size ?: 0) {
                _practiceProgress.value = this + 1
            }
        }
        Timber.i(_practiceProgress.value.toString())
    }

    fun clearExceptionHolder() {
        _exceptionHolder.value = null
    }

}