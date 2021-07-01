package com.laupdev.yocabulary.network

data class WordFromDictionary(
    val meanings: List<Meaning> = listOf(),
    val phonetics: List<Phonetic> = listOf(),
    val word: String
)