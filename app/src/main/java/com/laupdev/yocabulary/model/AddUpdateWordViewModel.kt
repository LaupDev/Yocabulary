package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.ui.vocabulary.ProcessStatus
import com.laupdev.yocabulary.database.PartOfSpeechWithMeanings
import com.laupdev.yocabulary.database.WordWithPartsOfSpeechAndMeanings
import com.laupdev.yocabulary.repository.VocabularyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.inject.Inject

@HiltViewModel
class AddUpdateWordViewModel @Inject constructor(val repository: VocabularyRepository) : ViewModel() {

    private val _processStatus = MutableLiveData(ProcessStatus.INACTIVE)
    val processStatus: LiveData<ProcessStatus>
        get() = _processStatus

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

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
        removeWordByName(wordWithPartsOfSpeechAndMeanings.word.word)
        insertWordWithPartsOfSpeechWithMeanings(wordWithPartsOfSpeechAndMeanings, false)
    }

    fun replaceWordOnUpdate(oldWord: String) {
        if (oldWord.isNotEmpty()) {
            removeWordByName(oldWord)
        }
        removeWordByName(wordWithPartsOfSpeechAndMeanings.word.word)
        insertWordWithPartsOfSpeechWithMeanings(wordWithPartsOfSpeechAndMeanings, true)
    }

    fun insertWordWithPartsOfSpeechWithMeanings(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings, isAfterUpdate: Boolean) {
        viewModelScope.launch {
            try {
                _processStatus.value = ProcessStatus.PROCESSING
                var stop = false

                if (repository.insertWord(wordWithPartsOfSpeechAndMeanings.word) == -1L) {
                    stop = true
                    this@AddUpdateWordViewModel.wordWithPartsOfSpeechAndMeanings =
                        wordWithPartsOfSpeechAndMeanings
                    _processStatus.value = ProcessStatus.FAILED_WORD_EXISTS
                }

                if (!stop) {
                    addAndUpdatePartsOfSpeechAndMeanings(wordWithPartsOfSpeechAndMeanings.word.word, wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings)
                    if (isAfterUpdate) {
                        _processStatus.value = ProcessStatus.COMPLETED_UPDATE
                    } else {
                        _processStatus.value = ProcessStatus.COMPLETED_ADDING
                    }
                }

            } catch (error: Exception) {
                _processStatus.value = ProcessStatus.FAILED
                _status.value = error.message
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
                var stop = false

                deletePartsOfSpeechAndMeaningsRemovedByUser(
                    oldWordWithPartsOfSpeechAndMeanings,
                    newWordWithPartsOfSpeechAndMeanings
                )

                if (repository.insertWord(newWordWithPartsOfSpeechAndMeanings.word) == -1L) {
                    if (oldWordWithPartsOfSpeechAndMeanings.word.word != newWordWithPartsOfSpeechAndMeanings.word.word) {
                        stop = true
                        this@AddUpdateWordViewModel.wordWithPartsOfSpeechAndMeanings =
                            newWordWithPartsOfSpeechAndMeanings
                        _processStatus.value = ProcessStatus.FAILED_WORD_EXISTS
                    } else {
                        repository.updateWord(newWordWithPartsOfSpeechAndMeanings.word)
                    }
                } else {
                    // If the word is successfully inserted then it means that user changed word "name". So we need to delete
                    oldWordWithPartsOfSpeechAndMeanings.word.word.let {
                            removeWordByName(it)
                    }
                }


                if (!stop) {
                    addAndUpdatePartsOfSpeechAndMeanings(newWordWithPartsOfSpeechAndMeanings.word.word, newWordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings)
                    _processStatus.value = ProcessStatus.COMPLETED_UPDATE
                }
            } catch (error: Exception) {
                _processStatus.value = ProcessStatus.FAILED
                _status.value = error.message
            }
        }
    }

    private suspend fun addAndUpdatePartsOfSpeechAndMeanings(word: String, partsOfSpeechWithMeanings: List<PartOfSpeechWithMeanings>) {
        partsOfSpeechWithMeanings.forEach { (pos, meanings) ->
            pos.word = word
            var newPosId = repository.insertPartOfSpeech(pos)
            if (newPosId == -1L) {
                repository.updatePartOfSpeech(pos)
                newPosId = pos.posId
            }

            meanings.forEach {
                it.posId = newPosId
                if (repository.insertMeaning(it) == -1L) {
                    repository.updateMeaning(it)
                }
            }
        }
    }

    private suspend fun deletePartsOfSpeechAndMeaningsRemovedByUser(
        oldWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings?,
        newWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings
    ) {
        oldWordWithPartsOfSpeechAndMeanings?.partsOfSpeechWithMeanings?.forEach { oldPartOfSpeechWithMeanings ->
            val tempPartOfSpeech =
                newWordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.firstOrNull {
                    it.partOfSpeech.posId == oldPartOfSpeechWithMeanings.partOfSpeech.posId
                }
            if (tempPartOfSpeech == null) {
                repository.deletePartOfSpeech(oldPartOfSpeechWithMeanings.partOfSpeech)
            } else {
                oldPartOfSpeechWithMeanings.meanings.forEach { oldMeaning ->
                    if (!tempPartOfSpeech.meanings.any { it.meaningId == oldMeaning.meaningId }) {
                        repository.deleteMeaning(oldMeaning)
                    }
                }
            }
        }
    }

    private fun removeWordByName(word: String) {
        viewModelScope.launch {
            try {
                repository.removeWordByName(word)
            } catch (error: Exception) {
                _status.value = error.message
            }
        }
    }

}