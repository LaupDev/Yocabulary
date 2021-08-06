package com.laupdev.yocabulary.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface MeaningDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(meaning: Meaning): Long

    @Update
    suspend fun update(meaning: Meaning)
}