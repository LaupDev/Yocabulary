package com.laupdev.yourdictionary.database

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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word)

    @Query("DELETE FROM Word WHERE word = :word")
    suspend fun removeWordByName(word: String)

    @Query("DELETE FROM Word")
    suspend fun deleteAll()
}