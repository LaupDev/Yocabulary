package com.laupdev.yocabulary.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update

@Dao
interface PartOfSpeechDao {
    @Insert
    suspend fun insert(partOfSpeech: PartOfSpeech): Long

    @Update
    suspend fun update(partOfSpeech: PartOfSpeech)
}