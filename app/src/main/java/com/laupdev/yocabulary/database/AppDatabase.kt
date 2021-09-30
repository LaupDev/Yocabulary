package com.laupdev.yocabulary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [PracticeProgress::class, Meaning::class, PartOfSpeech::class, Word::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun partOfSpeechDao(): PartOfSpeechDao
    abstract fun meaningDao(): MeaningDao
    abstract fun practiceProgressDao(): PracticeProgressDao

}