package com.laupdev.yocabulary

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.laupdev.yocabulary.database.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class PracticeProgressTest {

    private lateinit var wordDao: WordDao
    private lateinit var practiceProgressDao: PracticeProgressDao
    private lateinit var database: AppDatabase
    private lateinit var wordWithPracticeProgress: WordWithPracticeProgress

    @Before
    fun createDbAndInsertData() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        wordDao = database.wordDao()
        practiceProgressDao = database.practiceProgressDao()
        wordDao.insert(testWord)
        practiceProgressDao.insert(testPracticeProgress)
        wordWithPracticeProgress = practiceProgressDao.getAllWordsWithPracticeProgress()[0]
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun shouldBePracticedAfterWordCreated() {
        runBlocking {
            assertTrue(wordWithPracticeProgress.wordProgress.shouldBePracticed(PracticeType.MEANINGS))
            assertTrue(wordWithPracticeProgress.wordProgress.shouldBePracticed(PracticeType.WRITING))
        }
    }

    @Test
    @Throws(Exception::class)
    fun setNewPracticeDateAndCheckDate() {
        runBlocking {
            
            wordWithPracticeProgress.wordProgress.meaningProgress += 1
            wordWithPracticeProgress.wordProgress.setNextMeaningPracticeDate()
            var actualDate = wordWithPracticeProgress.wordProgress.nextMeaningPracticeDate
            var expectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time)
            assertEquals(expectedDate, actualDate)

            wordWithPracticeProgress.wordProgress.meaningProgress += 1
            wordWithPracticeProgress.wordProgress.setNextMeaningPracticeDate()
            actualDate = wordWithPracticeProgress.wordProgress.nextMeaningPracticeDate
            expectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }.time)
            assertEquals(expectedDate, actualDate)

            wordWithPracticeProgress.wordProgress.meaningProgress += 1
            wordWithPracticeProgress.wordProgress.setNextMeaningPracticeDate()
            actualDate = wordWithPracticeProgress.wordProgress.nextMeaningPracticeDate
            expectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 6) }.time)
            assertEquals(expectedDate, actualDate)
        }
    }

}