package com.laupdev.yocabulary.ui.practice.questions

import com.laupdev.yocabulary.database.MeaningPracticeProgress

class MeaningQuestion(
    val meaningPracticeProgress: MeaningPracticeProgress,
    val meaning: String,
    val answersList: List<String>,
    val rightAnswer: String
) : Question() {
    override val questionType: QuestionType = QuestionType.MATCH_MEANING
}