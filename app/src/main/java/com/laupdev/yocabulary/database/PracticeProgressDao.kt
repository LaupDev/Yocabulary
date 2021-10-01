package com.laupdev.yocabulary.database

import androidx.room.*

@Dao
interface PracticeProgressDao {

//    @Query("SELECT * FROM practice_progress WHERE word = :word")

    @Transaction
    @Query("SELECT * FROM words")
    suspend fun getAllWordsWithWritingPracticeProgress(): List<WordWithWritingPracticeProgress>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeaningPracticeProgress(meaningPracticeProgress: MeaningPracticeProgress)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWritingPracticeProgress(writingPracticeProgress: WritingPracticeProgress)

    @Query("DELETE FROM meaning_practice_progress WHERE meaning_id = :meaningId")
    suspend fun deleteMeaningPracticeProgressByMeaningId(meaningId: Long)

}