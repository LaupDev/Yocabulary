package com.laupdev.yocabulary.database

import androidx.room.*

@Dao
interface MeaningDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(meaning: Meaning): Long

    @Update
    suspend fun update(meaning: Meaning)

    @Delete
    suspend fun delete(meaning: Meaning)
}