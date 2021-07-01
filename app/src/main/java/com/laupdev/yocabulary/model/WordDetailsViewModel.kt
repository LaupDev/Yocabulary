package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.network.WordFromDictionary
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.IllegalArgumentException

class WordDetailsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _isAdded = MutableLiveData(true)
    val isAdded: LiveData<Boolean>
        get() = _isAdded

    private val _wordFromDictionary = MutableLiveData<WordFromDictionary>()
    val wordFromDictionary: LiveData<WordFromDictionary>
        get() = _wordFromDictionary

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

    val allWords: LiveData<List<Word>> = repository.allWords.asLiveData()

    fun getWordWithPosAndMeaningsById(wordId: Long) =
        repository.getWordWithPosAndMeaningsById(wordId).asLiveData()

    fun removeWord(wordId: Long) = viewModelScope.launch {
        repository.removeWordById(wordId)
    }

    fun getWordFromDictionary(word: String) {
        _isAdded.value = false
        viewModelScope.launch {
            try {
                _wordFromDictionary.value = repository.getWordFromDictionary(word)
            } catch (error: Exception) {
                _status.value = "Failure: ${error.message}"
            }
        }
    }

}

class WordDetailsViewModelFactory(private val repository: AppRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordDetailsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}