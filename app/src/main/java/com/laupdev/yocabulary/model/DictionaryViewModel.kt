package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class DictionaryViewModel(private val repository: AppRepository) : ViewModel() {

    val allWords: LiveData<List<Word>> = repository.allWords.asLiveData()

    fun getWordWithPosAndMeaningsById(wordId: Long) = repository.getWordWithPosAndMeaningsById(wordId).asLiveData()

    fun removeWord(wordId: Long) = viewModelScope.launch {
        repository.removeWordById(wordId)
    }

}

class DictionaryViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DictionaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DictionaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}