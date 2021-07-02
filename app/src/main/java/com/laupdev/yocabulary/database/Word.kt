package com.laupdev.yocabulary.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.text.DateFormat
import java.text.SimpleDateFormat
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
    val dateAdded: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().time),
    @ColumnInfo(name = "is_favorite")
    val isFavourite: Int = 0,
    @ColumnInfo(name = "audio_url")
    val audioUrl: String = ""
)

@Entity
data class WordIsFavorite(
    @ColumnInfo(name = "id")
    val wordId: Long,
    @ColumnInfo(name = "is_favorite")
    val isFavourite: Int
)
