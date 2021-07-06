package com.laupdev.yocabulary.repository

import androidx.annotation.WorkerThread
import com.laupdev.yocabulary.database.*
import com.laupdev.yocabulary.network.DictionaryNetwork
import com.laupdev.yocabulary.network.WordFromDictionary
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val network: DictionaryNetwork,
    private val wordDao: WordDao,
    private val posDao: PartOfSpeechDao,
    private val meaningDao: MeaningDao
) {

    val allWords: Flow<List<Word>> = wordDao.getAllWords()

    fun getWord(word: String) = wordDao.getWordByName(word)

    fun getWordById(wordId: Int) = wordDao.getWordById(wordId)

    fun getWordWithPosAndMeaningsById(wordId: Long) = wordDao.getWordWithPosAndMeaningsById(wordId)

    suspend fun getWordFromDictionary(word: String): WordWithPartsOfSpeechAndMeanings {
        return dictionaryWordToVocabularyFormat(network.getWordFromDictionary(word)[0])
    }

    private fun dictionaryWordToVocabularyFormat(wordFromDictionary: WordFromDictionary): WordWithPartsOfSpeechAndMeanings {

        val partsOfSpeechWithMeanings = mutableListOf<PartOfSpeechWithMeanings>()

        wordFromDictionary.meanings.forEach {
            val newPartOfSpeechWithMeanings = PartOfSpeechWithMeanings(
                PartOfSpeech(posId = 0, wordId = 0, partOfSpeech = it.partOfSpeech),
                it.definitions.let { meanings ->
                    val newMeaningsList = mutableListOf<Meaning>()
                    meanings.forEach { meaning ->
                        newMeaningsList.add(
                            Meaning(
                                meaningId = 0,
                                posId = 0,
                                meaning = meaning.definition,
                                example = meaning.example,
                                synonyms = meaning.synonyms.joinToString(separator = ", ")
                            )
                        )
                    }
                    newMeaningsList
                }
            )
            partsOfSpeechWithMeanings.add(newPartOfSpeechWithMeanings)
        }

        return WordWithPartsOfSpeechAndMeanings(
            Word(
                wordId = 0,
                word = wordFromDictionary.word,
                transcription = if (wordFromDictionary.phonetics.isNotEmpty()) wordFromDictionary.phonetics[0].text else "",
                audioUrl = if (wordFromDictionary.phonetics.isNotEmpty()) wordFromDictionary.phonetics[0].audio else ""
            ),
            partsOfSpeechWithMeanings
        )
    }

    suspend fun insertWord(word: Word): Long {
        return wordDao.insert(word)
    }

    suspend fun insertPartOfSpeech(partOfSpeech: PartOfSpeech): Long {
        return posDao.insert(partOfSpeech)
    }

    suspend fun insertMeaning(meaning: Meaning) {
        meaningDao.insert(meaning)
    }

    suspend fun removeWordById(wordId: Long) {
        wordDao.removeWordById(wordId)
    }

    suspend fun update(word: Word) {
        wordDao.update(word)
    }

    suspend fun updateWordIsFavorite(word: WordIsFavorite) {
        wordDao.updateIsFavorite(word)
    }

}