package com.laupdev.yocabulary.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM Word")
    fun getAllWords(): Flow<List<Word>>

    @Query("SELECT * FROM Word WHERE word LIKE :letter || '%'")
    fun getByFirstLetter(letter: Char): Flow<List<Word>>

    @Query("SELECT * FROM Word WHERE word = :word")
    fun getWordByName(word: String): Flow<Word>

    @Query("SELECT * FROM Word WHERE id = :wordId")
    fun getWordById(wordId: Int): Flow<Word>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word)

    @Update
    suspend fun update(word: Word)

    @Query("DELETE FROM Word WHERE id = :wordId")
    suspend fun removeWordById(wordId: Int)

    @Query("DELETE FROM Word")
    suspend fun deleteAll()
}