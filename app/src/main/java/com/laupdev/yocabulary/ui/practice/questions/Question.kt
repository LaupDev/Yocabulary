package com.laupdev.yocabulary.ui.practice.questions

abstract class Question {
    abstract val questionType: QuestionType
}


enum class QuestionType() {
    MATCH_MEANING,
    LEARN_SPELLING
}
