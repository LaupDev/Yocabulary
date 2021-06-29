package com.laupdev.yocabulary.database

import androidx.room.Embedded
import androidx.room.Relation

data class WordWithPartsOfSpeechAndMeanings(
    @Embedded
    val word: Word,
    @Relation(
        entity = PartOfSpeech::class,
        parentColumn = "id",
        entityColumn = "word_id"
    )
    val partsOfSpeechWithMeanings: List<PartOfSpeechWithMeanings>
)
