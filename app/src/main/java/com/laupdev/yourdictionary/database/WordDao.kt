package com.laupdev.yourdictionary.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM word")
    fun getAllWords(): Flow<List<Word>>

    @Query("SELECT * FROM word WHERE word LIKE :letter || '%'")
    fun getByFirstLetter(letter: Char): Flow<List<Word>>

    @Query("SELECT * FROM word WHERE word = :word")
    fun getWordByName(word: String): Flow<Word>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word)

    @Query("DELETE FROM word")
    suspend fun deleteAll()
}