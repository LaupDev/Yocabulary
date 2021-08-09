package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.ProcessState
import com.laupdev.yocabulary.database.WordWithPartsOfSpeechAndMeanings
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.IllegalArgumentException

class AddWordViewModel(private val repository: AppRepository) : ViewModel() {

    private val _addingProcess = MutableLiveData(ProcessState.INACTIVE)
    val addingProcess: LiveData<ProcessState>
        get() = _addingProcess

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

    private val _isTranslationGeneral = MutableLiveData(true)
    val isTranslationGeneral: LiveData<Boolean>
        get() = _isTranslationGeneral

    private lateinit var wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings

    fun inactivateProcess() {
        _addingProcess.value = ProcessState.INACTIVE
    }

    fun setIsTranslationGeneral(isTranslationGeneral: Boolean) {
        _isTranslationGeneral.value = isTranslationGeneral
    }

    fun getWordWithPosAndMeaningsByName(word: String) =
        repository.getWordWithPosAndMeaningsByName(word).asLiveData()

    fun replaceWord(oldWord: String) {
        if (oldWord.isNotEmpty()) {
            removeWordByName(oldWord)
        }
        removeWordByName(wordWithPartsOfSpeechAndMeanings.word.word)
        insertWordWithPartsOfSpeechWithMeanings(null, wordWithPartsOfSpeechAndMeanings, true)
    }

    fun insertWordWithPartsOfSpeechWithMeanings(
        oldWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings?,
        newWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings,
        isAdding: Boolean
    ) {
        viewModelScope.launch {
            try {
                _addingProcess.value = ProcessState.PROCESSING
                var stop = false

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

                if (repository.insertWord(newWordWithPartsOfSpeechAndMeanings.word) == -1L) {
                    if (isAdding) {
                        stop = true
                        this@AddWordViewModel.wordWithPartsOfSpeechAndMeanings =
                            newWordWithPartsOfSpeechAndMeanings
                        _addingProcess.value = ProcessState.FAILED_WORD_EXISTS
                    } else {
                        if (oldWordWithPartsOfSpeechAndMeanings?.word?.word != newWordWithPartsOfSpeechAndMeanings.word.word) {
                            stop = true
                            this@AddWordViewModel.wordWithPartsOfSpeechAndMeanings =
                                newWordWithPartsOfSpeechAndMeanings
                            _addingProcess.value = ProcessState.FAILED_WORD_EXISTS
                        }
                        repository.updateWord(newWordWithPartsOfSpeechAndMeanings.word)
                    }
                }
                if (!stop) {
                    newWordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.forEach { (pos, meanings) ->
                        pos.word = newWordWithPartsOfSpeechAndMeanings.word.word
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
                    _addingProcess.value = ProcessState.COMPLETED
                }
            } catch (error: Exception) {
                _addingProcess.value = ProcessState.FAILED
                _status.value = error.message
            }
        }
    }

    fun removeWordByName(word: String) {
        viewModelScope.launch {
            try {
                repository.removeWordByName(word)
            } catch (error: Exception) {
                _status.value = error.message
            }
        }
    }

}

class AddWordViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddWordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddWordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}