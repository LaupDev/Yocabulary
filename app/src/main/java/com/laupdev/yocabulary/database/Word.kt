package com.laupdev.yocabulary.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.*

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val wordId: Long,
    @ColumnInfo(name = "word")
    val word: String,
    @ColumnInfo(name = "transcription")
    val transcription: String = "",
    @ColumnInfo(name = "date_added")
    val dateAdded: Calendar = Calendar.getInstance(),
    @ColumnInfo(name = "is_favorite")
    val isFavourite: Int = 0,
    @ColumnInfo(name = "audio_url")
    val audioUrl: String = ""
)
