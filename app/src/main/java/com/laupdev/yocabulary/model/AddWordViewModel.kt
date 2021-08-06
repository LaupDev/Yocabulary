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

    fun setIsTranslationGeneral(isTranslationGeneral: Boolean) {
        _isTranslationGeneral.value = isTranslationGeneral
    }

    fun getWordWithPosAndMeaningsByName(word: String) =
        repository.getWordWithPosAndMeaningsByName(word).asLiveData()

    fun insertWordWithPartsOfSpeechWithMeanings(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        viewModelScope.launch {
            try {
                _addingProcess.value = ProcessState.PROCESSING

                if (repository.insertWord(wordWithPartsOfSpeechAndMeanings.word) == -1L) {
                    repository.updateWord(wordWithPartsOfSpeechAndMeanings.word)
                }

                wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.forEach { (pos, meanings) ->
                    pos.word = wordWithPartsOfSpeechAndMeanings.word.word
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
            } catch (error: Exception) {
                _addingProcess.value = ProcessState.FAILED
                _status.value = error.message
            }
            // TODO: 07.08.2021 BUG FIX. Remove PoS and meanings from database if they were removed while editing word
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