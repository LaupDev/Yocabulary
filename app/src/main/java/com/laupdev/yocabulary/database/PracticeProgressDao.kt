package com.laupdev.yocabulary.database

import androidx.room.*

@Dao
interface PracticeProgressDao {

    @Transaction
    @Query("SELECT * FROM words")
    suspend fun getAllWordsWithWritingPracticeProgress(): List<WordWithWritingPracticeProgress>

    @Transaction
    @Query("SELECT * FROM meanings ORDER BY RANDOM()")
    suspend fun getAllMeaningsWithMeaningPracticeProgress(): List<MeaningWithMeaningPracticeProgress>

    @Query("SELECT * FROM writing_practice_progress WHERE word = :word")
    suspend fun getWritingPracticeProgressByWord(word: String): WritingPracticeProgress

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeaningPracticeProgress(meaningPracticeProgress: MeaningPracticeProgress)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWritingPracticeProgress(writingPracticeProgress: WritingPracticeProgress)

    @Query("DELETE FROM meaning_practice_progress WHERE meaning_id = :meaningId")
    suspend fun deleteMeaningPracticeProgressByMeaningId(meaningId: Long)

    @Update
    suspend fun updateMeaningPracticeProgress(meaningPracticeProgress: MeaningPracticeProgress)

}