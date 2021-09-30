package com.laupdev.yocabulary.database

import androidx.room.Embedded
import androidx.room.Relation

data class WordWithPracticeProgress(
    @Embedded
    val word: Word,
    @Relation(
        entity = PracticeProgress::class,
        parentColumn = "word",
        entityColumn = "word"
    )
    val wordProgress: PracticeProgress
)
