package com.laupdev.yocabulary.database

import androidx.room.*

@Entity(
    tableName = "meanings",
    foreignKeys = [
        ForeignKey(
            entity = PartOfSpeech::class,
            parentColumns = ["id"],
            childColumns = ["pos_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Word::class,
            parentColumns = ["word"],
            childColumns = ["word"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("pos_id")]
)
data class Meaning(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val meaningId: Long,
    @ColumnInfo(name = "word")
    var word: String,
    @ColumnInfo(name = "pos_id")
    var posId: Long,
    @ColumnInfo(name = "meaning")
    val meaning: String,
    @ColumnInfo(name = "example")
    val example: String = "",
    @ColumnInfo(name = "synonyms")
    val synonyms: String = ""
)
