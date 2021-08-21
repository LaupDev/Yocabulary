package com.laupdev.yocabulary.repository

import com.laupdev.yocabulary.database.*
import com.laupdev.yocabulary.network.DictionaryNetwork
import com.laupdev.yocabulary.network.WordFromDictionary
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val network: DictionaryNetwork,
    private val database: AppDatabase
) {

    val allWords: Flow<List<Word>> = database.wordDao().getAllWords()

//    fun getWordById(word: String) = database.wordDao().getWordByName(word)

    fun getWordWithPosAndMeaningsByName(word: String) = database.wordDao().getWordWithPosAndMeaningsByName(word)

    suspend fun updateWord(word: Word) = database.wordDao().update(word)

    suspend fun updatePartOfSpeech(partOfSpeech: PartOfSpeech) = database.partOfSpeechDao().update(partOfSpeech)

    suspend fun updateMeaning(meaning: Meaning) = database.meaningDao().update(meaning)

    suspend fun getWordFromDictionary(word: String): WordWithPartsOfSpeechAndMeanings {
        return dictionaryWordToVocabularyFormat(network.getWordFromDictionary(word)[0])
    }

    private fun dictionaryWordToVocabularyFormat(wordFromDictionary: WordFromDictionary): WordWithPartsOfSpeechAndMeanings {

        val partsOfSpeechWithMeanings = mutableListOf<PartOfSpeechWithMeanings>()

        wordFromDictionary.meanings.forEach {
            val newPartOfSpeechWithMeanings = PartOfSpeechWithMeanings(
                PartOfSpeech(posId = 0, word = wordFromDictionary.word, partOfSpeech = it.partOfSpeech),
                it.definitions.let { meanings ->
                    val newMeaningsList = mutableListOf<Meaning>()
                    meanings.forEach { meaning ->
                        newMeaningsList.add(
                            Meaning(
                                meaningId = 0,
                                posId = 0,
                                meaning = meaning.definition.replaceFirstChar { firstChar -> firstChar.uppercase() },
                                example = meaning.example.replaceFirstChar { firstChar -> firstChar.uppercase() },
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
                word = wordFromDictionary.word,
                transcription = if (wordFromDictionary.phonetics.isNotEmpty()) wordFromDictionary.phonetics[0].text.replace("/", "") else "",
                audioUrl = if (wordFromDictionary.phonetics.isNotEmpty()) {
                    wordFromDictionary.phonetics[0].audio.let {
                        if (it.startsWith("//")) {
                            "https:$it"
                        } else {
                            it
                        }
                    }
                } else ""
            ),
            partsOfSpeechWithMeanings
        )
    }

    suspend fun insertWord(word: Word): Long {
        return database.wordDao().insert(word)
    }

    suspend fun insertPartOfSpeech(partOfSpeech: PartOfSpeech): Long {
        return database.partOfSpeechDao().insert(partOfSpeech)
    }

    suspend fun insertMeaning(meaning: Meaning): Long {
        return database.meaningDao().insert(meaning)
    }

    suspend fun deletePartOfSpeech(partOfSpeech: PartOfSpeech) {
        database.partOfSpeechDao().delete(partOfSpeech)
    }

    suspend fun deleteMeaning(meaning: Meaning) {
        database.meaningDao().delete(meaning)
    }

    suspend fun removeWordByName(word: String) {
        database.wordDao().removeWordByName(word)
    }

    suspend fun updateWordIsFavorite(word: WordIsFavorite) {
        database.wordDao().updateIsFavorite(word)
    }

    suspend fun updateWordTranslation(word: WordTranslation) {
        database.wordDao().updateWordTranslation(word)
    }

}