package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.database.WordIsFavorite
import com.laupdev.yocabulary.database.WordWithPartsOfSpeechAndMeanings
import com.laupdev.yocabulary.network.WordFromDictionary
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NullPointerException

class WordDetailsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _isAdded = MutableLiveData(true)
    val isAdded: LiveData<Boolean>
        get() = _isAdded

    private val _wordFromDictionary = MutableLiveData<WordFromDictionary>()
    val wordFromDictionary: LiveData<WordFromDictionary>
        get() = _wordFromDictionary

    private val _wordId = MutableLiveData<Long>()
    val wordId: LiveData<Long>
        get() = _wordId

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

//    private val _isFavourite = MutableLiveData(false)
//    val isFavourite: LiveData<Boolean>
//        get() = _isFavourite

    val allWords: LiveData<List<Word>> = repository.allWords.asLiveData()

    fun getWordWithPosAndMeaningsById(wordId: Long) = repository.getWordWithPosAndMeaningsById(wordId).asLiveData()

    fun removeWord(wordId: Long) = viewModelScope.launch {
        repository.removeWordById(wordId)
        _isAdded.value = false
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

    fun insertWordWithPartsOfSpeechAndMeanings(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        viewModelScope.launch {
            try {
                val newWordId = repository.insertWord(wordWithPartsOfSpeechAndMeanings.word)
                _wordId.value = newWordId
                wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.forEach { partOfSpeechWithMeanings ->
                    partOfSpeechWithMeanings.partOfSpeech.wordId = newWordId
                    val newPosId = repository.insertPartOfSpeech(partOfSpeechWithMeanings.partOfSpeech)
                    partOfSpeechWithMeanings.meanings.forEach { meaning ->
                        meaning.posId = newPosId
                        repository.insertMeaning(meaning)
                    }
                }
                _isAdded.value = true
            } catch (error: Exception) {
                _status.value = "Failure: ${error.message}"
            }
        }
    }

    suspend fun updateWordIsFavorite(wordId: Long, isFavourite: Boolean) =
        viewModelScope.async {
            try {
                if (wordId == 0L) {
                    throw Exception("Failure: System error")
                }
                repository.updateWordIsFavorite(WordIsFavorite(
                    wordId,
                    if (isFavourite) 1 else 0
                ))
                return@async true
            } catch (error: Exception) {
                _status.value = error.message
                return@async false
            }
        }.await()

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