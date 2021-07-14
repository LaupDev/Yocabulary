package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.database.Meaning
import com.laupdev.yocabulary.database.PartOfSpeech
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.database.WordWithPartsOfSpeechAndMeanings
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.IllegalArgumentException

class AddWordViewModel(private val repository: AppRepository) : ViewModel() {

    // TODO: 14.07.2021 Add live data to observe the insert status

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

    fun insertWord(word: Word) = viewModelScope.launch {
        repository.insertWord(word)
    }

    fun insertPartOfSpeech(partOfSpeech: PartOfSpeech) = viewModelScope.launch {
        repository.insertPartOfSpeech(partOfSpeech)
    }

    fun insertMeaning(meaning: Meaning) = viewModelScope.launch {
        repository.insertMeaning(meaning)
    }

    fun update(word: Word) = viewModelScope.launch {
        repository.update(word)
    }

    fun getWordById(wordId: Int) = repository.getWordById(wordId).asLiveData()

    suspend fun insertWordWithPartsOfSpeechWithMeanings(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings): Boolean {
        return try {
            val newWordId = repository.insertWord(wordWithPartsOfSpeechAndMeanings.word)
            wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.forEach { (pos, meanings) ->
                pos.wordId = newWordId
                val newPosId = repository.insertPartOfSpeech(pos)
                meanings.forEach {
                    it.posId = newPosId
                    repository.insertMeaning(it)
                }
            }
            true
        } catch (error: Exception) {
            _status.value = error.message
            false
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