package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.database.WordIsFavorite
import com.laupdev.yocabulary.database.WordTranslation
import com.laupdev.yocabulary.database.WordWithPartsOfSpeechAndMeanings
import com.laupdev.yocabulary.repository.VocabularyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.UnknownHostException
import javax.inject.Inject

enum class ErrorType {
    NO_SUCH_WORD,
    UNKNOWN_HOST,
    OTHER
}

@HiltViewModel
class WordDetailsViewModel @Inject constructor(private val repository: VocabularyRepository) : ViewModel() {

    private val _isAdded = MutableLiveData(true)
    val isAdded: LiveData<Boolean>
        get() = _isAdded

    private val _wordWithPosAndMeanings = MutableLiveData<WordWithPartsOfSpeechAndMeanings>()
    val wordWithPosAndMeanings: LiveData<WordWithPartsOfSpeechAndMeanings>
        get() = _wordWithPosAndMeanings

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

    private val _exceptionHolder = MutableLiveData<Exception?>(null)
    val exceptionHolder: LiveData<Exception?>
        get() = _exceptionHolder

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
                _exceptionHolder.value = error
            }
        }

    }

    fun replaceWord(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        viewModelScope.launch {
            repository.removeWordByName(wordWithPartsOfSpeechAndMeanings.word.word)
            insertWordWithPartsOfSpeechAndMeanings(wordWithPartsOfSpeechAndMeanings)
        }
    }

    fun insertWordWithPartsOfSpeechAndMeanings(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        viewModelScope.launch {
            try {
                _isAdded.value = repository.insertWordWithPartsOfSpeechAndMeanings(wordWithPartsOfSpeechAndMeanings)
            } catch (e: Exception) {
                _exceptionHolder.value = e
            }
        }
    }

    fun clearExceptionHolder() {
        _exceptionHolder.value = null
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