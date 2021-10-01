package com.laupdev.yocabulary.ui.vocabulary

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.laupdev.yocabulary.database.WordWithPartsOfSpeechAndMeanings
import timber.log.Timber

private const val WORD_PARAM = "word"

class UpdateWordFragment : AddUpdateCommonFragment() {

    private var word: String = ""

    private lateinit var oldWordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            word = it.getString(WORD_PARAM, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leaveWithoutWarning = false
        viewModel.getWordWithPosAndMeaningsByName(word).let { liveData ->
            liveData.observe(viewLifecycleOwner) { data ->
                data?.let {
                    oldWordWithPartsOfSpeechAndMeanings = data
                    populateFieldsWithData(data)
                    viewModel.inactivateProcess()
                    liveData.removeObservers(viewLifecycleOwner)
                }
            }
        }
    }

    override fun replaceWord() {
        viewModel.replaceWordOnUpdate(word)
    }

    override fun changePageAfterActionCompleted() {
        val action =
            UpdateWordFragmentDirections.goToWordDetailsAfterUpdate(
                trimInputField(binding.newWordEditText.text.toString()),
                true
            )
        findNavController().navigate(action)
    }

    override fun getPartOfSpeechId(partOfSpeechView: View): Long {
        return partOfSpeechView.tag?.toString()?.toLongOrNull() ?: 0
    }

    override fun getMeaningId(meaningView: View): Long {
        return meaningView.tag?.toString()?.toLongOrNull() ?: 0
    }

    override fun doActionOnWord(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        viewModel.updateWordWithPartsOfSpeechWithMeanings(oldWordWithPartsOfSpeechAndMeanings, wordWithPartsOfSpeechAndMeanings)
    }

    private fun populateFieldsWithData(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        Timber.i("--------------POPULATE------------------")
        wordWithPartsOfSpeechAndMeanings.word.let {
            binding.newWordEditText.setText(it.word)
            binding.transcriptionEditText.setText(it.transcription)
            binding.audioUrl.text = it.audioUrl
            binding.generalTranslationSwitch.isChecked =
                if (wordWithPartsOfSpeechAndMeanings.word.isTranslationGeneral == 0) {
                    binding.generalTranslation.isEnabled = false
                    false
                } else {
                    binding.generalTranslationEditText.setText(wordWithPartsOfSpeechAndMeanings.word.translations)
                    true
                }
        }

        wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.forEach {
            val posBlockLinearLayout = addPartOfSpeechView(it.partOfSpeech)
            it.meanings.forEach { meaning ->
                addMeaningView(posBlockLinearLayout, partOfSpeechViewCount, meaning)
            }
        }
    }

}