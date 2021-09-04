package com.laupdev.yocabulary.model.practice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.laupdev.yocabulary.ui.questions.Question
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

class BasePracticeViewModel : ViewModel() {

    private val _practiceProgress = MutableLiveData(0)
    val practiceProgress: LiveData<Int>
        get() = _practiceProgress

    val questions = mutableListOf(
        Question("LOL_1"),
        Question("LOL_2"),
        Question("LOL_3"),
        Question("LOL_4")
    )

    fun increaseProgress() {
        (_practiceProgress.value ?: 0).apply {
            if (this < 10) {
                _practiceProgress.value = this + 1
            }
        }
        Timber.i(_practiceProgress.value.toString())
    }

}