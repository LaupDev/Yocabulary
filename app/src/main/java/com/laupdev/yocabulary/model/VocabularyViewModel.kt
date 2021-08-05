package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.database.WordIsFavorite
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.async
import java.lang.Exception
import java.lang.IllegalArgumentException

class VocabularyViewModel(private val repository: AppRepository) : ViewModel() {

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

    val allWords: LiveData<List<Word>> = repository.allWords.asLiveData()

    suspend fun updateWordIsFavorite(word: String, isFavourite: Boolean) =
        viewModelScope.async {
            try {
                if (word.isEmpty()) {
                    throw Exception("Failure: System error")
                }
                repository.updateWordIsFavorite(
                    WordIsFavorite(
                        word,
                        if (isFavourite) 0 else 1
                    )
                )
                return@async true
            } catch (error: Exception) {
                _status.value = error.message
                return@async false
            }
        }.await()

}

class VocabularyViewModelFactory(private val repository: AppRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VocabularyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VocabularyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}