package com.laupdev.yocabulary.network

data class Definition(
    val definition: String = "",
    val example: String = "",
    val synonyms: List<String> = listOf()
)