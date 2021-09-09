package com.laupdev.yocabulary.ui.questions

class MeaningQuestion(
    val meaning: String,
    val answersList: List<String>,
    val rightAnswer: String
) : Question() {
    override val questionType: QuestionType = QuestionType.MATCH_MEANING


}