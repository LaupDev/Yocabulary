package com.laupdev.yocabulary.database

import androidx.room.Embedded
import androidx.room.Relation

data class WordWithWritingPracticeProgress(
    @Embedded
    val word: Word,
    @Relation(
        entity = WritingPracticeProgress::class,
        parentColumn = "word",
        entityColumn = "word"
    )
    val wordProgress: WritingPracticeProgress
)
