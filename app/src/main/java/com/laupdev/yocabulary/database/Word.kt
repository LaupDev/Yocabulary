package com.laupdev.yocabulary.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @NotNull val word: String,
    @NotNull val translation: String,
    val transcription: String?,
    val meaning: String?,
    val example: String?
)
