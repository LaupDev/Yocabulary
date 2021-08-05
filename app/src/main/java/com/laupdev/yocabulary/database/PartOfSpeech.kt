package com.laupdev.yocabulary.database

import androidx.room.*
import org.jetbrains.annotations.NotNull

@Entity(
    tableName = "parts_of_speech",
    foreignKeys = [
        ForeignKey(
            entity = Word::class,
            parentColumns = ["word"],
            childColumns = ["word"],
            onDelete = ForeignKey.CASCADE
        )],
    indices = [Index("word")]
)
data class PartOfSpeech(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val posId: Long,
    @ColumnInfo(name = "word")
    var word: String,
    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String,
    @ColumnInfo(name = "translation")
    val translation: String = ""
)