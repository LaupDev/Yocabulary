package com.laupdev.yocabulary.database

import androidx.room.*
import java.util.*

@Entity(
    tableName = "meaning_practice_progress",
    foreignKeys = [
        ForeignKey(
            entity = Meaning::class,
            parentColumns = ["id"],
            childColumns = ["meaning_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("meaning_id")]
)
data class MeaningPracticeProgress(
    @PrimaryKey
    @ColumnInfo(name = "meaning_id")
    var meaningId: Long,
    @ColumnInfo(name = "progress")
    var progress: Int = 0,
    @ColumnInfo(name = "next_practice_date")
    var nextPracticeDate: Calendar = Calendar.getInstance()
) {
    fun shouldBePracticed() = Calendar.getInstance() > nextPracticeDate

    fun setNextPracticeDate() {
        val daysToAdd =
            when (progress) {
                1 -> 1
                2 -> 3
                3 -> 6
                else -> progress * 2
            }
        nextPracticeDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, daysToAdd) }
    }

}