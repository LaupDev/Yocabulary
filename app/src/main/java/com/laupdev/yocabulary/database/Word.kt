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
    @PrimaryKey
    @ColumnInfo(name = "word")
    val word: String,
    @ColumnInfo(name = "transcription")
    val transcription: String = "",
    @ColumnInfo(name = "translations")
    val translations: String = "",
    @ColumnInfo(name = "is_trans_general")
    val isTranslationGeneral: Int = 1,
    @ColumnInfo(name = "date_added")
    val dateAdded: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().time),
    @ColumnInfo(name = "is_favorite")
    val isFavourite: Int = 0,
    @ColumnInfo(name = "audio_url")
    val audioUrl: String = ""
)

@Entity
data class WordIsFavorite(
    @ColumnInfo(name = "word")
    val word: String,
    @ColumnInfo(name = "is_favorite")
    val isFavourite: Int
)

@Entity
data class WordTranslation(
    @ColumnInfo(name = "word")
    val word: String,
    @ColumnInfo(name = "translations")
    val translations: String = ""
)
