package com.laupdev.yocabulary.ui.practice.questions

import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.ui.practice.PracticeFragment
import timber.log.Timber

class MeaningQuestionView(layoutInflater: LayoutInflater, container: ViewGroup?, private val practiceFragment: PracticeFragment) : QuestionView() {
    override var view: View = layoutInflater.inflate(R.layout.item_question_layout, container, false)

    private lateinit var rightAnswer: String

    override fun bind(question: Question) {
        resetView()

        val meaningQuestion = question as MeaningQuestion
        rightAnswer = meaningQuestion.rightAnswer

        val answerButtonOnClickListener = View.OnClickListener {
            val chosenAnswer = (it as Button).text.toString()
            if (isAnswerRight(chosenAnswer)) {
                // TODO: 09.09.2021 Change score
                setRightAnswerButtonUI(it)
            } else {
                setWrongAnswerButtonUI(it)
                setRightAnswerButtonUI(getRightAnswerButtonIndex())
            }
            disableAnswerButtonsAndShowNextButton()
        }

        view.findViewById<TextView>(R.id.task).text = meaningQuestion.meaning

        view.findViewById<LinearLayout>(R.id.all_answers).forEachIndexed { index, answerBtn ->

            (answerBtn as Button).text = meaningQuestion.answersList[index]
            answerBtn.setOnClickListener(answerButtonOnClickListener)
        }

        view.findViewById<Button>(R.id.next_page).setOnClickListener {
            practiceFragment.viewModel.rightWrongAnswerIndexes[0] = -1
            practiceFragment.viewModel.rightWrongAnswerIndexes[1] = -1
            practiceFragment.nextPage()
        }

        practiceFragment.viewModel.rightWrongAnswerIndexes.let { (rightAnswerIndex, wrongAnswerIndex) ->
            if (rightAnswerIndex >= 0) {
                if (wrongAnswerIndex >= 0) {
                    setWrongAnswerButtonUI(view.findViewById<LinearLayout>(R.id.all_answers)[wrongAnswerIndex] as Button)
                }
                setRightAnswerButtonUI(view.findViewById<LinearLayout>(R.id.all_answers)[rightAnswerIndex] as Button)
                disableAnswerButtonsAndShowNextButton()
            }
        }
    }

    private fun isAnswerRight(chosenAnswer: String) = chosenAnswer == rightAnswer

    private fun getRightAnswerButtonIndex(): Button {
        view.findViewById<LinearLayout>(R.id.all_answers).forEach { answerBtn ->
            if ((answerBtn as Button).text.toString() == rightAnswer) {
                return answerBtn
            }
        }
        // It should never happen. Added to avoid compilation error
        return Button(practiceFragment.requireContext())
    }

    private fun disableAnswerButtonsAndShowNextButton() {
        view.findViewById<LinearLayout>(R.id.all_answers).forEach {
            it.isEnabled = false
        }
        view.findViewById<Button>(R.id.next_page).visibility = VISIBLE
    }

    private fun resetView() {
        view.findViewById<LinearLayout>(R.id.all_answers).forEach {
            it.isEnabled = true
            it.backgroundTintList = ContextCompat.getColorStateList(practiceFragment.requireContext(), R.color.color_grey_lighter_2)
        }
        view.findViewById<Button>(R.id.next_page).visibility = INVISIBLE

    }

    private fun setRightAnswerButtonUI(rightAnswerButton: Button) {
        rightAnswerButton.backgroundTintList =
            ContextCompat.getColorStateList(practiceFragment.requireContext(), R.color.color_state_right_answer_button)
        practiceFragment.viewModel.rightWrongAnswerIndexes[0] = view.findViewById<LinearLayout>(R.id.all_answers).indexOfChild(rightAnswerButton)
    }

    private fun setWrongAnswerButtonUI(wrongAnswerButton: Button) {
        wrongAnswerButton.backgroundTintList =
            ContextCompat.getColorStateList(practiceFragment.requireContext(), R.color.color_state_wrong_answer_button)
        practiceFragment.viewModel.rightWrongAnswerIndexes[1] = view.findViewById<LinearLayout>(R.id.all_answers).indexOfChild(wrongAnswerButton)
    }
}