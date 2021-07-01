package com.laupdev.yocabulary.network

data class Meaning(
    val definitions: List<Definition> = listOf(),
    val partOfSpeech: String = ""
)