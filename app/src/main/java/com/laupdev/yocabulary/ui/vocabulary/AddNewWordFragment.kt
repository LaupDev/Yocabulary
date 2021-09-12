package com.laupdev.yocabulary.ui.vocabulary

import android.content.res.Resources
import android.view.View
import android.view.View.VISIBLE
import androidx.navigation.findNavController
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.database.WordWithPartsOfSpeechAndMeanings
import kotlin.math.roundToInt

class AddNewWordFragment : AddUpdateCommonFragment() {

    companion object {
        fun convertDpToPixel(dp: Int): Int {
            return (dp * (Resources.getSystem().displayMetrics.densityDpi / 160f)).roundToInt()
        }
    }

    override fun setListeners() {
        super.setListeners()

        setupSearchInDictionaryBtn()

    }

    override fun replaceWord() {
        viewModel.replaceWord()
    }

    override fun getPartOfSpeechId(partOfSpeechView: View): Long {
        return 0
    }

    override fun getMeaningId(meaningView: View): Long {
        return 0
    }

    override fun doActionOnWord(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        viewModel.insertWordWithPartsOfSpeechWithMeanings(
            wordWithPartsOfSpeechAndMeanings,
            false
        )
    }

    private fun setupSearchInDictionaryBtn() {
        binding.searchInDictionary.apply {
            visibility = VISIBLE
            setOnClickListener {
                if (binding.newWordEditText.text.toString().isNotEmpty()) {
                    binding.newWord.error = null

                    val action =
                        AddNewWordFragmentDirections.actionAddNewWordFragmentToWordDetailsFragment(
                            word = binding.newWordEditText.text.toString(),
                            isInVocabulary = false
                        )
                    requireView().findNavController().navigate(action)
                } else {
                    binding.newWord.isErrorEnabled = true
                    binding.newWord.error = getString(R.string.required_error)
                }
            }
        }
    }
}