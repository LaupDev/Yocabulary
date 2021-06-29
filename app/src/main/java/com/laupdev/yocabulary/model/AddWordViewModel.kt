package com.laupdev.yocabulary.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.laupdev.yocabulary.database.Meaning
import com.laupdev.yocabulary.database.PartOfSpeech
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class AddWordViewModel(private val repository: AppRepository) : ViewModel() {

    suspend fun insertWord(word: Word) = viewModelScope.launch {
        repository.insertWord(word)
    }

    suspend fun insertPartOfSpeech(partOfSpeech: PartOfSpeech) = viewModelScope.launch {
        repository.insertPartOfSpeech(partOfSpeech)
    }

    suspend fun insertMeaning(meaning: Meaning) = viewModelScope.launch {
        repository.insertMeaning(meaning)
    }

    fun update(word: Word) = viewModelScope.launch {
        repository.update(word)
    }

    fun getWordById(wordId: Int) = repository.getWordById(wordId).asLiveData()

    fun insertWordWithPartsOfSpeechWithMeanings(
        newWord: Word,
        posWithMeanings: MutableMap<PartOfSpeech, MutableList<Meaning>>
    ): Job {
       return viewModelScope.launch {
            val newWordId = repository.insertWord(newWord)
            for ((pos, meanings) in posWithMeanings) {
                pos.wordId = newWordId
                val newPosId = repository.insertPartOfSpeech(pos)
                for (meaning in meanings) {
                    meaning.posId = newPosId
                    repository.insertMeaning(meaning)
                }
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