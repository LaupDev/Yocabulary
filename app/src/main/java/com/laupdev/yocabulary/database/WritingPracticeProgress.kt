package com.laupdev.yocabulary.database

import androidx.room.*
import java.util.*

@Entity(
    tableName = "writing_practice_progress",
    foreignKeys = [
        ForeignKey(
            entity = Word::class,
            parentColumns = ["word"],
            childColumns = ["word"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("word")]
)
data class WritingPracticeProgress(
    @PrimaryKey
    @ColumnInfo(name = "word")
    var word: String,
    @ColumnInfo(name = "progress")
    var progress: Int = 0,
    @ColumnInfo(name = "next_practice_date")
    var nextPracticeDate: Calendar = Calendar.getInstance()
) {
    fun shouldBePracticed() = Calendar.getInstance() > nextPracticeDate

    fun setNextPracticeDate() {
        val daysToAdd =
            when(progress) {
                1 -> 1
                2 -> 3
                3 -> 6
                else -> progress * 2
            }
        nextPracticeDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, daysToAdd) }
    }
}
