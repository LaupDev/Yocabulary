package com.laupdev.yocabulary.repository

import com.laupdev.yocabulary.database.*
import com.laupdev.yocabulary.exceptions.WordAlreadyExistsException
import com.laupdev.yocabulary.network.DictionaryNetwork
import com.laupdev.yocabulary.network.WordFromDictionary
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRepository @Inject constructor(
    val network: DictionaryNetwork,
    val database: AppDatabase
) {

    fun getAllWords() = database.wordDao().getAllWords()

    fun getWordWithPosAndMeaningsByName(word: String) =
        database.wordDao().getWordWithPosAndMeaningsByName(word)

    suspend fun getWordFromDictionary(word: String): WordWithPartsOfSpeechAndMeanings {
        return wordFromDictionaryApiToVocabularyFormat(network.getWordFromDictionary(word)[0])
    }

    private fun wordFromDictionaryApiToVocabularyFormat(wordFromDictionary: WordFromDictionary): WordWithPartsOfSpeechAndMeanings {

        val word = Word(
            word = wordFromDictionary.word,
            transcription = if (wordFromDictionary.phonetics.isNotEmpty()) wordFromDictionary.phonetics[0].text.replace(
                "/",
                ""
            ) else "",
            audioUrl = if (wordFromDictionary.phonetics.isNotEmpty()) {
                wordFromDictionary.phonetics[0].audio.let {
                    if (it.startsWith("//")) {
                        "https:$it"
                    } else {
                        it
                    }
                }
            } else ""
        )
        val partsOfSpeechWithMeanings = mutableListOf<PartOfSpeechWithMeanings>()

        wordFromDictionary.meanings.forEach {
            val newPartOfSpeech = PartOfSpeech(
                posId = 0,
                word = wordFromDictionary.word,
                partOfSpeech = it.partOfSpeech
            )
            val newMeaningsList = mutableListOf<Meaning>()
            it.definitions.let { meanings ->
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

            val newPartOfSpeechWithMeanings =
                PartOfSpeechWithMeanings(newPartOfSpeech, newMeaningsList)
            partsOfSpeechWithMeanings.add(newPartOfSpeechWithMeanings)
        }

        return WordWithPartsOfSpeechAndMeanings(word, partsOfSpeechWithMeanings)
    }

    suspend fun insertWordWithPartsOfSpeechAndMeanings(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings): Boolean {
        if (insertWord(wordWithPartsOfSpeechAndMeanings.word) == -1L) {
            throw WordAlreadyExistsException("Word already exists in database")
        } else {
            addOrUpdatePartsOfSpeechAndMeaningsWithProgress(
                wordWithPartsOfSpeechAndMeanings.word.word,
                wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings
            )
            insertWritingPracticeProgress(WritingPracticeProgress(word = wordWithPartsOfSpeechAndMeanings.word.word))

            return true
        }
    }

    suspend fun updateWordWithPartsOfSpeechAndMeanings(
        oldWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings,
        newWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings
    ): Boolean {
        if (oldWordWithPartsOfSpeechAndMeanings.word.word != newWordWithPartsOfSpeechAndMeanings.word.word) {
            // TODO: 02.10.2021 Reset writing practice progress
            val isSuccessfullyAdded =
                insertWordWithPartsOfSpeechAndMeanings(newWordWithPartsOfSpeechAndMeanings)
            if (isSuccessfullyAdded) {
                removeWordByName(oldWordWithPartsOfSpeechAndMeanings.word.word)
            }
        } else {
            updateWord(newWordWithPartsOfSpeechAndMeanings.word)
            deletePartsOfSpeechAndMeaningsRemovedByUser(
                oldWordWithPartsOfSpeechAndMeanings,
                newWordWithPartsOfSpeechAndMeanings
            )
            resetMeaningsProgressIfMeaningChanged(
                oldWordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings,
                newWordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings
            )
            addOrUpdatePartsOfSpeechAndMeaningsWithProgress(
                newWordWithPartsOfSpeechAndMeanings.word.word,
                newWordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings
            )
        }
        return true
    }

    private suspend fun resetMeaningsProgressIfMeaningChanged(
        oldPartsOfSpeechWithMeanings: List<PartOfSpeechWithMeanings>,
        newPartsOfSpeechWithMeanings: List<PartOfSpeechWithMeanings>
    ) {
        oldPartsOfSpeechWithMeanings.forEach { (oldPartOfSpeech, oldMeanings) ->
            newPartsOfSpeechWithMeanings.firstOrNull {
                it.partOfSpeech.posId == oldPartOfSpeech.posId
            }?.let { (_, newMeanings) ->
                oldMeanings.forEach { oldMeaning ->
                    newMeanings.find { newMeaning -> oldMeaning.meaningId == newMeaning.meaningId }
                        ?.let {
                            if (oldMeaning.meaning != it.meaning) {
                                Timber.w("${it.meaning}Meaning practice progress reset")
                                deleteMeaningPracticeProgressByMeaningId(it.meaningId)
                                insertMeaningPracticeProgress(MeaningPracticeProgress(meaningId = it.meaningId))
                            }
                        }
                }
            }

        }

    }

    private suspend fun deletePartsOfSpeechAndMeaningsRemovedByUser(
        oldWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings?,
        newWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings
    ) {
        oldWordWithPartsOfSpeechAndMeanings?.partsOfSpeechWithMeanings?.forEach { oldPartOfSpeechWithMeanings ->
            val tempPartOfSpeechWithMeanings =
                newWordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.firstOrNull {
                    it.partOfSpeech.posId == oldPartOfSpeechWithMeanings.partOfSpeech.posId
                }
            if (tempPartOfSpeechWithMeanings == null) {
                deletePartOfSpeech(oldPartOfSpeechWithMeanings.partOfSpeech)
            } else {
                oldPartOfSpeechWithMeanings.meanings.forEach { oldMeaning ->
                    if (!tempPartOfSpeechWithMeanings.meanings.any { it.meaningId == oldMeaning.meaningId }) {
                        deleteMeaning(oldMeaning)
                    }
                }
            }
        }
    }

    private suspend fun addOrUpdatePartsOfSpeechAndMeaningsWithProgress(
        word: String,
        partsOfSpeechWithMeanings: List<PartOfSpeechWithMeanings>
    ) {
        partsOfSpeechWithMeanings.forEach { (pos, meanings) ->
            pos.word = word
            var newPosId = insertPartOfSpeech(pos)
            if (newPosId == -1L) {
                updatePartOfSpeech(pos)
                newPosId = pos.posId
            }

            meanings.forEach {
                it.posId = newPosId
                val newMeaningId = insertMeaning(it)
                if (newMeaningId == -1L) {
                    updateMeaning(it)
                } else {
                    insertMeaningPracticeProgress(MeaningPracticeProgress(meaningId = newMeaningId))
                }
            }
        }
    }

    private suspend fun insertWord(word: Word): Long {
        return database.wordDao().insert(word)
    }

    private suspend fun insertPartOfSpeech(partOfSpeech: PartOfSpeech): Long {
        return database.partOfSpeechDao().insert(partOfSpeech)
    }

    private suspend fun insertMeaning(meaning: Meaning): Long {
        return database.meaningDao().insert(meaning)
    }

    private suspend fun insertWritingPracticeProgress(writingPracticeProgress: WritingPracticeProgress) {
        database.practiceProgressDao().insertWritingPracticeProgress(writingPracticeProgress)
    }

    private suspend fun insertMeaningPracticeProgress(meaningPracticeProgress: MeaningPracticeProgress) {
        database.practiceProgressDao().insertMeaningPracticeProgress(meaningPracticeProgress)
    }

    private suspend fun deletePartOfSpeech(partOfSpeech: PartOfSpeech) {
        database.partOfSpeechDao().delete(partOfSpeech)
    }

    private suspend fun deleteMeaning(meaning: Meaning) {
        database.meaningDao().delete(meaning)
    }

    private suspend fun deleteMeaningPracticeProgressByMeaningId(meaningId: Long) {
        database.practiceProgressDao().deleteMeaningPracticeProgressByMeaningId(meaningId)
    }

    suspend fun removeWordByName(word: String) {
        database.wordDao().removeWordByName(word)
    }

    private suspend fun updateWord(word: Word) = database.wordDao().update(word)

    private suspend fun updatePartOfSpeech(partOfSpeech: PartOfSpeech) =
        database.partOfSpeechDao().update(partOfSpeech)

    private suspend fun updateMeaning(meaning: Meaning) = database.meaningDao().update(meaning)

    suspend fun updateWordIsFavorite(word: WordIsFavorite) {
        database.wordDao().updateIsFavorite(word)
    }

    suspend fun updateWordTranslation(word: WordTranslation) {
        database.wordDao().updateWordTranslation(word)
    }

}