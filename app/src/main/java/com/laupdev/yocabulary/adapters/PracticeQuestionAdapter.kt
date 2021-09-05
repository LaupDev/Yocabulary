package com.laupdev.yocabulary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.laupdev.yocabulary.model.practice.PracticeViewModel
import com.laupdev.yocabulary.ui.questions.MeaningsQuestionView
import com.laupdev.yocabulary.ui.questions.Question
import com.laupdev.yocabulary.ui.questions.QuestionType
import com.laupdev.yocabulary.ui.questions.QuestionView

class PracticeQuestionAdapter(private val viewModel: PracticeViewModel) : RecyclerView.Adapter<PracticeQuestionHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PracticeQuestionHolder {
        return when (viewModel.questions[viewModel.currentQuestionIndex].questionType) {
            QuestionType.MATCH_MEANING -> {
                PracticeQuestionHolder(MeaningsQuestionView(LayoutInflater.from(parent.context), parent))
            }
            else -> {
                PracticeQuestionHolder(MeaningsQuestionView(LayoutInflater.from(parent.context), parent))
            }
        }
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
