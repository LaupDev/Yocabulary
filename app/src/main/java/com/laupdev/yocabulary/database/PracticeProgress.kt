package com.laupdev.yocabulary.database

import androidx.room.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_YEAR

@Entity(
    tableName = "practice_progress",
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
data class PracticeProgress(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val progressId: Long,
    @ColumnInfo(name = "word")
    var word: String,
    @ColumnInfo(name = "meaning_progress")
    var meaningProgress: Int = 0,
    @ColumnInfo(name = "next_meaning_practice_date")
    var nextMeaningPracticeDate: String = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(
        Calendar.getInstance().time
    ),
    @ColumnInfo(name = "writing_progress")
    var writingProgress: Int = 0,
    @ColumnInfo(name = "next_writing_practice_date")
    var nextWritingPracticeDate: String = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(
        Calendar.getInstance().time
    ),
) {
    fun shouldBePracticed(nextPracticeDate: Date) =
        Calendar.getInstance().time.after(nextPracticeDate)

    fun setNextMeaningPracticeDate() {
        nextMeaningPracticeDate = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(
            Calendar.getInstance().apply { add(DAY_OF_YEAR, (meaningProgress * 3) + 1) }.time
        )
    }

    fun setNextWritingPracticeDate() {
        nextWritingPracticeDate = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(
            Calendar.getInstance().apply { add(DAY_OF_YEAR, (writingProgress * 3) + 1) }.time
        )
    }
}
