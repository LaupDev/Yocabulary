package com.laupdev.yocabulary.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update

@Dao
interface MeaningDao {
    @Insert
    suspend fun insert(meaning: Meaning)

    @Update
    suspend fun update(meaning: Meaning)
}