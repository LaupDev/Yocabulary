package com.laupdev.yocabulary.database

import androidx.room.*

@Dao
interface PracticeProgressDao {

//    @Query("SELECT * FROM practice_progress WHERE word = :word")

    @Transaction
    @Query("SELECT * FROM words")
    suspend fun getAllWordsWithPracticeProgress(): List<WordWithPracticeProgress>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(practiceProgress: PracticeProgress)

}