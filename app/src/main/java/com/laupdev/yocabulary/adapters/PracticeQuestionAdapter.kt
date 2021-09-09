package com.laupdev.yocabulary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.laupdev.yocabulary.model.practice.PracticeViewModel
import com.laupdev.yocabulary.ui.practice.PracticeFragment
import com.laupdev.yocabulary.ui.questions.MeaningQuestionView
import com.laupdev.yocabulary.ui.questions.Question
import com.laupdev.yocabulary.ui.questions.QuestionType
import com.laupdev.yocabulary.ui.questions.QuestionView
import timber.log.Timber

class PracticeQuestionAdapter(private val viewModel: PracticeViewModel, private val practiceFragment: PracticeFragment) : RecyclerView.Adapter<PracticeQuestionHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PracticeQuestionHolder {
        return when (viewModel.questions[viewModel.currentQuestionIndex].questionType) {
            QuestionType.MATCH_MEANING -> {
                PracticeQuestionHolder(MeaningQuestionView(LayoutInflater.from(parent.context), parent, practiceFragment))
            }
            else -> {
                PracticeQuestionHolder(MeaningQuestionView(LayoutInflater.from(parent.context), parent, practiceFragment))
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
