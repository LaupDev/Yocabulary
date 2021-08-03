package com.laupdev.yocabulary.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE word LIKE :letter || '%'")
    fun getByFirstLetter(letter: Char): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE word = :word")
    fun getWordByName(word: String): Flow<Word>

    @Query("SELECT * FROM words WHERE id = :wordId")
    fun getWordById(wordId: Int): Flow<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: Word): Long

    @Update
    suspend fun update(word: Word)

    @Update(entity = Word::class)
    suspend fun updateIsFavorite(word: WordIsFavorite)

    @Update(entity = Word::class)
    suspend fun updateWordTranslation(word: WordTranslation)

    @Query("DELETE FROM words WHERE id = :wordId")
    suspend fun removeWordById(wordId: Long)

    @Query("DELETE FROM words")
    suspend fun deleteAll()

//    @Transaction
//    @Query("SELECT * FROM words WHERE word = :word")
//    fun getWordWithPosAndMeaningsByWord(word: String): Flow<WordWithPartsOfSpeechAndMeanings>

    @Transaction
    @Query("SELECT * FROM words WHERE id = :wordId")
    fun getWordWithPosAndMeaningsById(wordId: Long): Flow<WordWithPartsOfSpeechAndMeanings>

}