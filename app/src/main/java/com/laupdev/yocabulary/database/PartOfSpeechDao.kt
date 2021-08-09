package com.laupdev.yocabulary.database

import androidx.room.*

@Dao
interface PartOfSpeechDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(partOfSpeech: PartOfSpeech): Long

    @Update
    suspend fun update(partOfSpeech: PartOfSpeech)

    @Delete
    suspend fun delete(partOfSpeech: PartOfSpeech)
}