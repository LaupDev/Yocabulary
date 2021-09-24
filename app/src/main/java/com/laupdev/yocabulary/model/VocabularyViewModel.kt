package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.database.WordIsFavorite
import com.laupdev.yocabulary.repository.VocabularyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.inject.Inject

@HiltViewModel
class VocabularyViewModel @Inject constructor(private val repository: VocabularyRepository) : ViewModel() {

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

    val allWords: LiveData<List<Word>> = repository.getAllWords().asLiveData()

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