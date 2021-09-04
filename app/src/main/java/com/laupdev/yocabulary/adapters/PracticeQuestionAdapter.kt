package com.laupdev.yocabulary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.laupdev.yocabulary.model.practice.BasePracticeViewModel
import com.laupdev.yocabulary.ui.questions.Question
import com.laupdev.yocabulary.ui.questions.QuestionView

class PracticeQuestionAdapter(private val viewModel: BasePracticeViewModel) : RecyclerView.Adapter<PracticeQuestionHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PracticeQuestionHolder {
        return PracticeQuestionHolder(QuestionView(LayoutInflater.from(parent.context), parent))
    }

    override fun onBindViewHolder(holder: PracticeQuestionHolder, position: Int) {
        holder.bind(viewModel.questions[position])
    }

    override fun getItemCount(): Int {
        return viewModel.questions.size
    }
}

class PracticeQuestionHolder internal constructor(private val questionView: QuestionView) :
    RecyclerView.ViewHolder(questionView.view) {
        internal fun bind(question: Question) {
            questionView.bind(question)
        }
}
