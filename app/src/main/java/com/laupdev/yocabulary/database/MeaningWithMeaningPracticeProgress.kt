package com.laupdev.yocabulary.database

import androidx.room.Embedded
import androidx.room.Relation

data class MeaningWithMeaningPracticeProgress(
    @Embedded
    val meaning: Meaning,
    @Relation(
        entity = MeaningPracticeProgress::class,
        parentColumn = "id",
        entityColumn = "meaning_id"
    )
    val meaningPracticeProgress: MeaningPracticeProgress
)