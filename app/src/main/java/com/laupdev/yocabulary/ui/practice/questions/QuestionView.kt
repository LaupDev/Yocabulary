package com.laupdev.yocabulary.ui.practice.questions

import android.view.View

abstract class QuestionView {
    abstract var view: View
    abstract fun bind(question: Question)
}