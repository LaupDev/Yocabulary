package com.laupdev.yocabulary.network

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/en_US/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface DictionaryNetwork {
    @GET("{word}")
    suspend fun getWordFromDictionary(
        @Path("word") word: String
    ): List<WordFromDictionary>
}

object DictionaryNet {
    val retrofitService: DictionaryNetwork by lazy {
        retrofit.create(DictionaryNetwork::class.java)
    }
}