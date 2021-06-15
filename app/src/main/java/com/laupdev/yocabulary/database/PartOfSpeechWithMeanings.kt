package com.laupdev.yocabulary.database

import androidx.room.Embedded
import androidx.room.Relation

data class PartOfSpeechWithMeanings(
    @Embedded
    val partOfSpeech: PartOfSpeech,
    @Relation(
        entity = Meaning::class,
        parentColumn = "id",
        entityColumn = "pos_id"
    )
    val meanings: List<Meaning>
)