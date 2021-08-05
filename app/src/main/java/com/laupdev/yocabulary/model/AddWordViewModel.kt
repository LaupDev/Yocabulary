package com.laupdev.yocabulary.model

import androidx.lifecycle.*
import com.laupdev.yocabulary.ProcessState
import com.laupdev.yocabulary.database.Meaning
import com.laupdev.yocabulary.database.PartOfSpeech
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.database.WordWithPartsOfSpeechAndMeanings
import com.laupdev.yocabulary.repository.AppRepository
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.IllegalArgumentException

class AddWordViewModel(private val repository: AppRepository) : ViewModel() {

    private val _addingProcess = MutableLiveData(ProcessState.INACTIVE)
    val addingProcess: LiveData<ProcessState>
        get() = _addingProcess

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

    private val _isTranslationGeneral = MutableLiveData(true)
    val isTranslationGeneral: LiveData<Boolean>
        get() = _isTranslationGeneral

    fun insertWord(word: Word) = viewModelScope.launch {
        repository.insertWord(word)
    }

    fun insertPartOfSpeech(partOfSpeech: PartOfSpeech) = viewModelScope.launch {
        repository.insertPartOfSpeech(partOfSpeech)
    }

    fun insertMeaning(meaning: Meaning) = viewModelScope.launch {
        repository.insertMeaning(meaning)
    }

    fun setIsTranslationGeneral(isTranslationGeneral: Boolean) {
        _isTranslationGeneral.value = isTranslationGeneral
    }

    suspend fun updateWord(word: Word) = repository.updateWord(word)

    suspend fun updatePartOfSpeech(partOfSpeech: PartOfSpeech) = repository.updatePartOfSpeech(partOfSpeech)

    suspend fun updateMeaning(meaning: Meaning) = repository.updateMeaning(meaning)

    fun getWordByName(word: String) = repository.getWordById(word).asLiveData()

    fun getWordWithPosAndMeaningsByName(word: String) =
        repository.getWordWithPosAndMeaningsByName(word).asLiveData()

    fun insertWordWithPartsOfSpeechWithMeanings(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        viewModelScope.launch {
            try {
                _addingProcess.value = ProcessState.PROCESSING

                // TODO: 05.08.2021 Disable user to change word or check it

                if (repository.insertWord(wordWithPartsOfSpeechAndMeanings.word) == -1L) {
                    updateWord(wordWithPartsOfSpeechAndMeanings.word)
                }

                wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.forEach { (pos, meanings) ->
                    val newPosId = if (pos.posId == 0L) {
                        pos.word = wordWithPartsOfSpeechAndMeanings.word.word
                        repository.insertPartOfSpeech(pos)
                    } else {
                        updatePartOfSpeech(pos)
                        pos.posId
                    }
                    meanings.forEach {
                        if (it.meaningId == 0L) {
                            it.posId = newPosId
                            repository.insertMeaning(it)
                        } else {
                            updateMeaning(it)
                        }
                    }
                }
                _addingProcess.value = ProcessState.COMPLETED
            } catch (error: Exception) {
                _addingProcess.value = ProcessState.FAILED
                _status.value = error.message
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