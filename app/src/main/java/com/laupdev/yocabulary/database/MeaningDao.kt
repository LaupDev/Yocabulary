package com.laupdev.yocabulary.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface MeaningDao {
    @Insert
    suspend fun insert(meaning: Meaning)
}