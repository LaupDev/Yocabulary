package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.ui.vocabulary.ProcessStatus
import com.laupdev.yocabulary.database.WordWithPartsOfSpeechAndMeanings
import com.laupdev.yocabulary.database.WritingPracticeProgress
import com.laupdev.yocabulary.repository.VocabularyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class AddUpdateWordViewModel @Inject constructor(val repository: VocabularyRepository) :
    ViewModel() {

    private val _processStatus = MutableLiveData(ProcessStatus.INACTIVE)
    val processStatus: LiveData<ProcessStatus>
        get() = _processStatus

    private val _exceptionHolder = MutableLiveData<Exception?>(null)
    val exceptionHolder: LiveData<Exception?>
        get() = _exceptionHolder

    private val _isTranslationGeneral = MutableLiveData(true)
    val isTranslationGeneral: LiveData<Boolean>
        get() = _isTranslationGeneral

    private lateinit var wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings

    fun inactivateProcess() {
        _processStatus.value = ProcessStatus.INACTIVE
    }

    fun setIsTranslationGeneral(isTranslationGeneral: Boolean) {
        _isTranslationGeneral.value = isTranslationGeneral
    }

    fun getWordWithPosAndMeaningsByName(word: String) =
        repository.getWordWithPosAndMeaningsByName(word).asLiveData()
            .also { _processStatus.value = ProcessStatus.PROCESSING }

    fun replaceWord() {
        viewModelScope.launch {
            val writingPracticeProgress = repository.getWritingPracticeProgressByWord(wordWithPartsOfSpeechAndMeanings.word.word)
            removeWordByName(wordWithPartsOfSpeechAndMeanings.word.word)
            insertWordWithPartsOfSpeechWithMeanings(wordWithPartsOfSpeechAndMeanings, writingPracticeProgress)
        }
    }

    fun replaceWordOnUpdate(oldWord: String) {
        if (oldWord.isNotEmpty()) {
            removeWordByName(oldWord)
        }
        viewModelScope.launch {
            val writingPracticeProgress = repository.getWritingPracticeProgressByWord(wordWithPartsOfSpeechAndMeanings.word.word)
            removeWordByName(wordWithPartsOfSpeechAndMeanings.word.word)
            insertWordWithPartsOfSpeechWithMeanings(wordWithPartsOfSpeechAndMeanings, writingPracticeProgress)
        }
    }

    fun clearExceptionHolder() {
        _exceptionHolder.value = null
    }

    fun insertWordWithPartsOfSpeechWithMeanings(
        wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings,
        writingPracticeProgress: WritingPracticeProgress? = null
    ) {
        viewModelScope.launch {
            _processStatus.value = ProcessStatus.PROCESSING
            this@AddUpdateWordViewModel.wordWithPartsOfSpeechAndMeanings =
                wordWithPartsOfSpeechAndMeanings
            try {

                val isSuccessfullyAdded = repository.insertWordWithPartsOfSpeechAndMeanings(
                    wordWithPartsOfSpeechAndMeanings,
                    writingPracticeProgress
                )

                if (isSuccessfullyAdded) {
                    _processStatus.value = ProcessStatus.COMPLETED
                }

            } catch (error: Exception) {
                _processStatus.value = ProcessStatus.INACTIVE
                _exceptionHolder.value = error
            }
        }
    }

    fun updateWordWithPartsOfSpeechWithMeanings(
        oldWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings,
        newWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings
    ) {
        viewModelScope.launch {
            try {
                _processStatus.value = ProcessStatus.PROCESSING
                this@AddUpdateWordViewModel.wordWithPartsOfSpeechAndMeanings =
                    newWordWithPartsOfSpeechAndMeanings

                val isSuccessfullyUpdated = repository.updateWordWithPartsOfSpeechAndMeanings(
                    oldWordWithPartsOfSpeechAndMeanings,
                    newWordWithPartsOfSpeechAndMeanings
                )

                if (isSuccessfullyUpdated) {
                    _processStatus.value = ProcessStatus.COMPLETED
                }
            } catch (error: Exception) {
                _processStatus.value = ProcessStatus.INACTIVE
                _exceptionHolder.value = error
            }
        }
    }

    private fun removeWordByName(word: String) {
        viewModelScope.launch {
            repository.deleteWordByName(word)
        }
    }

}