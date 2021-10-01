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
import java.util.*

@RunWith(AndroidJUnit4::class)
class PracticeProgressTest {

    private lateinit var wordDao: WordDao
    private lateinit var practiceProgressDao: PracticeProgressDao
    private lateinit var database: AppDatabase
    private lateinit var wordWithWritingPracticeProgress: WordWithWritingPracticeProgress

    @Before
    fun createDbAndInsertData() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        wordDao = database.wordDao()
        practiceProgressDao = database.practiceProgressDao()
        wordDao.insert(testWord)
        practiceProgressDao.insertWritingPracticeProgress(testPracticeProgress)
        wordWithWritingPracticeProgress = practiceProgressDao.getAllWordsWithWritingPracticeProgress()[0]
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
            assertTrue(wordWithWritingPracticeProgress.wordProgress.shouldBePracticed())
        }
    }

    @Test
    @Throws(Exception::class)
    fun setNewPracticeDateAndCheckDate() {
        runBlocking {
            
            wordWithWritingPracticeProgress.wordProgress.progress += 1
            wordWithWritingPracticeProgress.wordProgress.setNextPracticeDate()
            var actualDate = wordWithWritingPracticeProgress.wordProgress.nextPracticeDate
            var expectedDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            assertEquals(expectedDate, actualDate)

            wordWithWritingPracticeProgress.wordProgress.progress += 1
            wordWithWritingPracticeProgress.wordProgress.setNextPracticeDate()
            actualDate = wordWithWritingPracticeProgress.wordProgress.nextPracticeDate
            expectedDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }
            assertEquals(expectedDate, actualDate)

            wordWithWritingPracticeProgress.wordProgress.progress += 1
            wordWithWritingPracticeProgress.wordProgress.setNextPracticeDate()
            actualDate = wordWithWritingPracticeProgress.wordProgress.nextPracticeDate
            expectedDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 6) }
            assertEquals(expectedDate, actualDate)
        }
    }

}