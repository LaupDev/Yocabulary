package com.laupdev.yocabulary.ui.questions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.laupdev.yocabulary.R

class MeaningsQuestionView(layoutInflater: LayoutInflater, container: ViewGroup?) : QuestionView() {
    override var view: View = layoutInflater.inflate(R.layout.item_question_layout, container, false)

    override fun bind(question: Question) {
        view.findViewById<TextView>(R.id.test_text).text = question.text
    }
}