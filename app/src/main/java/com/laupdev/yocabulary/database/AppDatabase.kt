package com.laupdev.yocabulary.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Word::class, PartOfSpeech::class, Meaning::class, MeaningPracticeProgress::class, WritingPracticeProgress::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun partOfSpeechDao(): PartOfSpeechDao
    abstract fun meaningDao(): MeaningDao
    abstract fun practiceProgressDao(): PracticeProgressDao

    class WordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            // Delete all content here.
            database.wordDao().deleteAll()

            var word = Word(word = "Hello", transcription = "həˈləʊ", translations = "Привіт", audioUrl = "https://lex-audio.useremarkable.com/mp3/hello_us_1_rr.mp3")
            database.wordDao().insert(word)
            var partOfSpeech = PartOfSpeech(0, "Hello", "Interjection", "Привіт")
            var newPosId = database.partOfSpeechDao().insert(partOfSpeech)
            var meaning = Meaning(0, word.word, newPosId, "Used when meeting or greeting someone", "Hello, John! How are you?")
            var newMeaningId = database.meaningDao().insert(meaning)
            var meaningPracticeProgress = MeaningPracticeProgress(meaningId = newMeaningId)
            database.practiceProgressDao().insertMeaningPracticeProgress(meaningPracticeProgress)
            var writingPracticeProgress = WritingPracticeProgress(word = word.word)
            database.practiceProgressDao().insertWritingPracticeProgress(writingPracticeProgress)

            word = Word(word = "Hi", transcription = "haɪ", translations = "Привіт")
            database.wordDao().insert(word)
            partOfSpeech = PartOfSpeech(0, "Hi", "Interjection", "Привіт")
            newPosId = database.partOfSpeechDao().insert(partOfSpeech)
            meaning = Meaning(0, word.word, newPosId, "Used as an informal greeting, usually to people who you know", "Hi, there!", "Hello")
            newMeaningId = database.meaningDao().insert(meaning)
            meaningPracticeProgress = MeaningPracticeProgress(meaningId = newMeaningId)
            database.practiceProgressDao().insertMeaningPracticeProgress(meaningPracticeProgress)
            writingPracticeProgress = WritingPracticeProgress(word = word.word)
            database.practiceProgressDao().insertWritingPracticeProgress(writingPracticeProgress)

            word = Word(word = "Apple", translations = "Яблуко", audioUrl = "https://lex-audio.useremarkable.com/mp3/apple_us_1.mp3")
            database. wordDao().insert(word)
            partOfSpeech = PartOfSpeech(0, "Apple", "Noun", "Яблуко")
            newPosId = database.partOfSpeechDao().insert(partOfSpeech)
            meaning = Meaning(0, word.word, newPosId, "", "He took a bite out of the apple")
            newMeaningId = database.meaningDao().insert(meaning)
            meaningPracticeProgress = MeaningPracticeProgress(meaningId = newMeaningId)
            database.practiceProgressDao().insertMeaningPracticeProgress(meaningPracticeProgress)
            writingPracticeProgress = WritingPracticeProgress(word = word.word)
            database.practiceProgressDao().insertWritingPracticeProgress(writingPracticeProgress)
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context
        ): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yocabulary_database"
                )
                    .addCallback(WordDatabaseCallback(CoroutineScope(Dispatchers.IO)))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

}