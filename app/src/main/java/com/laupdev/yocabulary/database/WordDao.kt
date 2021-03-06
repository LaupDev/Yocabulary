package com.laupdev.yocabulary.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<Word>>

    @Query("SELECT COUNT(*) FROM words LIMIT 5")
    suspend fun getWordsCountMax5(): Int

    @Query("SELECT * FROM words LIMIT 10")
    suspend fun getTenWords(): List<Word>

    @Query("SELECT * FROM words WHERE word LIKE :letter || '%'")
    fun getByFirstLetter(letter: Char): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE word = :word")
    fun getWordByName(word: String): Flow<Word>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word): Long

    @Update
    suspend fun update(word: Word)

    @Update(entity = Word::class)
    suspend fun updateIsFavorite(word: WordIsFavorite)

    @Update(entity = Word::class)
    suspend fun updateWordTranslation(word: WordTranslation)

    @Query("DELETE FROM words WHERE word = :word")
    suspend fun deleteWordByName(word: String)

    @Query("DELETE FROM words")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM words WHERE word = :word")
    fun getWordWithPosAndMeaningsByName(word: String): Flow<WordWithPartsOfSpeechAndMeanings>

}