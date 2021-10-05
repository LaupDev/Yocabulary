package com.laupdev.yocabulary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.laupdev.yocabulary.model.practice.PracticeViewModel
import com.laupdev.yocabulary.ui.practice.PracticeFragment
import com.laupdev.yocabulary.ui.practice.questions.MeaningQuestionView
import com.laupdev.yocabulary.ui.practice.questions.Question
import com.laupdev.yocabulary.ui.practice.questions.QuestionType
import com.laupdev.yocabulary.ui.practice.questions.QuestionView

class PracticeQuestionAdapter(private val viewModel: PracticeViewModel, private val practiceFragment: PracticeFragment) : RecyclerView.Adapter<PracticeQuestionHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PracticeQuestionHolder {
        return when (viewModel.getQuestions()[viewModel.currentQuestionIndex].questionType) {
            QuestionType.MATCH_MEANING -> {
                PracticeQuestionHolder(MeaningQuestionView(LayoutInflater.from(parent.context), parent, practiceFragment))
            }
            else -> {
                PracticeQuestionHolder(MeaningQuestionView(LayoutInflater.from(parent.context), parent, practiceFragment))
            }
        }
    }

    override fun onBindViewHolder(holder: PracticeQuestionHolder, position: Int) {
        holder.bind(viewModel.getQuestions()[position])
    }

    override fun getItemCount(): Int {
        return viewModel.getQuestions().size
    }
}

class PracticeQuestionHolder internal constructor(private val questionView: QuestionView) :
    RecyclerView.ViewHolder(questionView.view) {
        internal fun bind(question: Question) {
            questionView.bind(question)
        }
}
