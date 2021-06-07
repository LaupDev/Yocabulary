package com.laupdev.yocabulary.ui

import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.application.DictionaryApplication
import com.laupdev.yocabulary.databinding.FragmentWordDetailsBinding
import com.laupdev.yocabulary.model.DictionaryViewModel
import com.laupdev.yocabulary.model.DictionaryViewModelFactory

class WordDetailsFragment : Fragment() {

    companion object {
        const val WORD_ID = "word_id"
        const val SEARCH_PREFIX = "https://www.google.com/search?q="
    }

    private var _binding: FragmentWordDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DictionaryViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this,
            DictionaryViewModelFactory((activity.application as DictionaryApplication).repository)
        )
            .get(DictionaryViewModel::class.java)
    }

    private var currWordId: Int = 0

    private var partOfSpeechCount = 1
    private var definitionCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            currWordId = it.getInt(WORD_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordDetailsBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var wordId = 0
        viewModel.getWordById(currWordId).observe(viewLifecycleOwner, {
            it?.let {
                wordId = it.id
                binding.word.text = it.word
                binding.transcription.text = it.transcription
                binding.translation.text = it.translation
            }
        })

        // TODO: 02.06.2021 Change database and populate word details using data from this database add icon for "delete word" menu item
        populateWordDetails()

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_wordDetailsFragment_to_wordListFragment)
        }

        binding.editWordBtn.setOnClickListener {
            if (wordId != 0) {
                val action =
                    WordDetailsFragmentDirections.actionWordDetailsFragmentToAddNewWordFragment(
                        wordId
                    )
                view.findNavController().navigate(action)
            } else {
                Snackbar.make(requireView(), R.string.word_update_error, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        // TODO: 01.06.2021 Complete button selector

//        binding.searchTranslationBtn.setOnClickListener {
//            searchWordTranslationInWeb()
//        }

    }

//    private fun searchWordTranslationInWeb() {
//        val queryUrl: Uri = Uri.parse("${SEARCH_PREFIX}${binding.word.text} translation")
//        val intent = Intent(Intent.ACTION_VIEW, queryUrl)
//        requireContext().startActivity(intent)
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.word_details_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_word -> {
                removeWord()

                return true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun removeWord() {
        viewModel.removeWord(currWordId)
        requireView().findNavController().popBackStack()
    }


    private fun populateWordDetails() {

        val wordDetailsLinearLayout = LinearLayout(requireContext())
        wordDetailsLinearLayout.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        wordDetailsLinearLayout.orientation = LinearLayout.VERTICAL

        wordDetailsLinearLayout.addView(addPartOfSpeech())

        binding.wordBody.addView(wordDetailsLinearLayout)

        binding.wordBody.addView(addDefinition(meaning = true, example = true, synonyms = true))

    }

    private fun addPartOfSpeech(): View {
        val partOfSpeechTextView = TextView(requireContext())

        partOfSpeechTextView.id = R.id.part_of_speech + 10000 + partOfSpeechCount

        partOfSpeechTextView.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        partOfSpeechTextView.text = "- transitive verb (отримати доступ)"

        TextViewCompat.setTextAppearance(
            partOfSpeechTextView,
            R.style.TextAppearance_Yocabulary_PartOfSpeech
        )

        partOfSpeechCount++
        return partOfSpeechTextView

    }

    private fun addDefinition(meaning: Boolean, example: Boolean, synonyms: Boolean): View {
        val definitionLinearLayout = LinearLayout(requireContext())
        definitionLinearLayout.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        definitionLinearLayout.orientation = LinearLayout.VERTICAL

        if (meaning) {
            val meaningTextView = TextView(requireContext())
            meaningTextView.id = R.id.meaning + 10000 + definitionCount

            val paramsMargin = ViewGroup.MarginLayoutParams(
                LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            )
            paramsMargin.topMargin = 4
            meaningTextView.layoutParams = paramsMargin

            meaningTextView.text = definitionCount.toString() + "." + "Approach or enter (a place)"
            TextViewCompat.setTextAppearance(
                meaningTextView,
                R.style.TextAppearance_Yocabulary_Meaning
            )

            definitionLinearLayout.addView(meaningTextView)
        }

        if (example) {
            val exampleTextView = TextView(requireContext())
            exampleTextView.id = R.id.example + 10000 + definitionCount

            val paramsWithMargin = ViewGroup.MarginLayoutParams(
                LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            )
            paramsWithMargin.topMargin = 8
            exampleTextView.layoutParams = paramsWithMargin

            exampleTextView.text = "Single rooms have private baths accessed via the balcony"
            TextViewCompat.setTextAppearance(
                exampleTextView,
                R.style.TextAppearance_Yocabulary_Example
            )

            definitionLinearLayout.addView(exampleTextView)
        }

        if (synonyms) {
            val synonymsHeaderTextView = TextView(requireContext())

            val paramsWithMargin = ViewGroup.MarginLayoutParams(
                LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            )
            paramsWithMargin.topMargin = 8
            synonymsHeaderTextView.layoutParams = paramsWithMargin

            synonymsHeaderTextView.text = getString(R.string.synonyms_header)
            TextViewCompat.setTextAppearance(
                synonymsHeaderTextView,
                R.style.TextAppearance_Yocabulary_PartOfSpeech
            )
            synonymsHeaderTextView.textSize = 16f

            definitionLinearLayout.addView(synonymsHeaderTextView)

            val synonymsWordsTextView = TextView(requireContext())
            synonymsWordsTextView.id = R.id.synonym_words + 10000 + definitionCount

            paramsWithMargin.topMargin = 4
            synonymsWordsTextView.layoutParams = paramsWithMargin

            synonymsWordsTextView.text = "retrieve, gain, gain access to, acquire, obtain"
            TextViewCompat.setTextAppearance(
                synonymsWordsTextView,
                R.style.TextAppearance_Yocabulary_SynonymWords
            )

            definitionLinearLayout.addView(synonymsWordsTextView)
        }

        definitionCount++
        return definitionLinearLayout
    }

}