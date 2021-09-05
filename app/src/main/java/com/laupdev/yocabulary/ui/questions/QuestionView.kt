package com.laupdev.yocabulary.ui.questions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.laupdev.yocabulary.R

abstract class QuestionView {
    abstract var view: View
    abstract fun bind(question: Question)
}