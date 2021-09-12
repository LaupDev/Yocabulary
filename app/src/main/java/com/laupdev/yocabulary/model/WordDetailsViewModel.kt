package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.database.WordIsFavorite
import com.laupdev.yocabulary.database.WordTranslation
import com.laupdev.yocabulary.database.WordWithPartsOfSpeechAndMeanings
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.UnknownHostException

enum class ErrorType {
    NO_SUCH_WORD,
    UNKNOWN_HOST,
    WORD_EXISTS,
    OTHER
}

class WordDetailsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _isAdded = MutableLiveData(true)
    val isAdded: LiveData<Boolean>
        get() = _isAdded

    private val _wordWithPosAndMeanings = MutableLiveData<WordWithPartsOfSpeechAndMeanings>()
    val wordWithPosAndMeanings: LiveData<WordWithPartsOfSpeechAndMeanings>
        get() = _wordWithPosAndMeanings

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

    private val _error = MutableLiveData<ErrorType>()
    val error: LiveData<ErrorType>
        get() = _error

    private val _isFavourite = MutableLiveData(false)
    val isFavourite: LiveData<Boolean>
        get() = _isFavourite

    fun getWordWithPosAndMeaningsByName(word: String) = Transformations.map(repository.getWordWithPosAndMeaningsByName(word).asLiveData()) {
        it?.let {
            _wordWithPosAndMeanings.value = it
            _isFavourite.value = it.word.isFavourite == 1
            return@map it
        }
    }

    fun removeWord(word: String) = viewModelScope.launch {
        repository.removeWordByName(word)
        _isAdded.value = false
    }

    fun getWordFromDictionary(word: String) {
        if (wordWithPosAndMeanings.value == null) {
            _isAdded.value = false
        }
        viewModelScope.launch {
            try {
                _wordWithPosAndMeanings.value = repository.getWordFromDictionary(word)
            } catch (error: Exception) {
                when(error) {
                    is UnknownHostException -> {
                        _error.value = ErrorType.UNKNOWN_HOST
                    }
                    is HttpException -> {
                        _error.value = ErrorType.NO_SUCH_WORD
                    }
                    else -> {
                        _error.value = ErrorType.OTHER
                    }
                }
                _status.value = "Failure: ${error.message}"
            }
        }

    }

    fun replaceWord(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        viewModelScope.launch {
            repository.removeWordByName(wordWithPartsOfSpeechAndMeanings.word.word)
            insertWordWithPartsOfSpeechAndMeanings(wordWithPartsOfSpeechAndMeanings)
        }
    }

    fun insertWordWithPartsOfSpeechAndMeanings(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) =
        viewModelScope.launch {
            try {
                if (repository.insertWord(wordWithPartsOfSpeechAndMeanings.word) == -1L) {
                    _error.value = ErrorType.WORD_EXISTS
                } else {
                    wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.forEach { partOfSpeechWithMeanings ->
                        partOfSpeechWithMeanings.partOfSpeech.word =
                            wordWithPartsOfSpeechAndMeanings.word.word
                        val newPosId =
                            repository.insertPartOfSpeech(partOfSpeechWithMeanings.partOfSpeech)
                        partOfSpeechWithMeanings.meanings.forEach { meaning ->
                            meaning.posId = newPosId
                            repository.insertMeaning(meaning)
                        }
                    }
                    _isAdded.value = true
                }
            } catch (error: Exception) {
                _error.value = ErrorType.OTHER
                _status.value = "Failure: ${error.message}"
            }
        }

    fun updateWordIsFavorite(word: String) =
        viewModelScope.launch {
            try {
                if (word.isEmpty()) {
                    throw Exception("Failure: System error")
                }
                repository.updateWordIsFavorite(
                    WordIsFavorite(
                        word,
                        if (isFavourite.value == true) 0 else 1
                    )
                )
                _isFavourite.value = isFavourite.value == false
            } catch (error: Exception) {
                _status.value = error.message
            }
        }

    fun updateWordTranslation(word: String, translation: String) =
        viewModelScope.launch {
            try {
                if (word.isEmpty()) {
                    throw Exception("Failure: System error")
                }
                repository.updateWordTranslation(
                    WordTranslation(
                        word,
                        translation
                    )
                )
            } catch (error: Exception) {
                _status.value = error.message
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