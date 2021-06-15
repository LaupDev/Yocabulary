package com.laupdev.yocabulary.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface PartOfSpeechDao {

    @Insert
    suspend fun insert(pos: PartOfSpeech): Long

}