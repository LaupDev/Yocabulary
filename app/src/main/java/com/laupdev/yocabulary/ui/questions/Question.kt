package com.laupdev.yocabulary.ui.questions

data class Question (
    val questionType: QuestionType,
    var text: String = ""
)

enum class QuestionType() {
    MATCH_MEANING,
    LEARN_SPELLING
}
