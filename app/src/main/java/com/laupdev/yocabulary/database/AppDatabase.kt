package com.laupdev.yocabulary.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Meaning::class, PartOfSpeech::class, Word::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun partOfSpeechDao(): PartOfSpeechDao
    abstract fun meaningDao(): MeaningDao

    class WordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.wordDao(), database.partOfSpeechDao(), database.meaningDao())
                }
            }
        }

        suspend fun populateDatabase(wordDao: WordDao, partOfSpeechDao: PartOfSpeechDao, meaningDao: MeaningDao) {
            // Delete all content here.
            wordDao.deleteAll()

            var word = Word(word = "Hello", transcription = "həˈləʊ", translations = "Привіт", audioUrl = "https://lex-audio.useremarkable.com/mp3/hello_us_1_rr.mp3")
            wordDao.insert(word)
            var partOfSpeech = PartOfSpeech(0, "Hello", "Interjection", "Привіт")
            var newPosId = partOfSpeechDao.insert(partOfSpeech)
            var meaning = Meaning(0, newPosId, "Used when meeting or greeting someone", "Hello, John! How are you?")
            meaningDao.insert(meaning)

            word = Word(word = "Hi", transcription = "haɪ", translations = "Привіт")
            wordDao.insert(word)
            partOfSpeech = PartOfSpeech(0, "Hi", "Interjection", "Привіт")
            newPosId = partOfSpeechDao.insert(partOfSpeech)
            meaning = Meaning(0, newPosId, "Used as an informal greeting, usually to people who you know", "Hi, there!", "Hello")
            meaningDao.insert(meaning)

            word = Word(word = "Apple", translations = "Яблуко", audioUrl = "https://lex-audio.useremarkable.com/mp3/apple_us_1.mp3")
            wordDao.insert(word)
            partOfSpeech = PartOfSpeech(0, "Apple", "Noun", "Яблуко")
            newPosId = partOfSpeechDao.insert(partOfSpeech)
            meaning = Meaning(0, newPosId, "", "He took a bite out of the apple")
            meaningDao.insert(meaning)
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
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