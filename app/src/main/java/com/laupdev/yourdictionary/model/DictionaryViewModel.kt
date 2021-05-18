package com.laupdev.yourdictionary.model

import androidx.lifecycle.*
import com.laupdev.yourdictionary.database.Word
import com.laupdev.yourdictionary.repository.AppRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class DictionaryViewModel(private val repository: AppRepository) : ViewModel() {

    val allWords: LiveData<List<Word>> = repository.allWords.asLiveData()

    fun getWordByName(word: String) = repository.getWord(word).asLiveData()

    fun removeWord(word: String) = viewModelScope.launch {
        repository.removeWordByName(word)
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