package com.laupdev.yocabulary.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.application.DictionaryApplication
import com.laupdev.yocabulary.database.*
import com.laupdev.yocabulary.databinding.FragmentWordDetailsBinding
import com.laupdev.yocabulary.model.WordDetailsViewModel
import com.laupdev.yocabulary.model.WordDetailsViewModelFactory
import com.laupdev.yocabulary.network.WordFromDictionary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

enum class UniqueIdAddition(val idAddition: Int) {
    PART_OF_SPEECH(10000),
    MEANING(11000),
    EXAMPLE(12000),
    SYNONYM_WORDS(13000)
}

class WordDetailsFragment : Fragment() {

    companion object {
        const val WORD_ID = "word_id"
        const val WORD_NAME = "word_name"
//        const val SEARCH_PREFIX = "https://www.google.com/search?q="
    }

    private var _binding: FragmentWordDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WordDetailsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this,
            WordDetailsViewModelFactory((activity.application as DictionaryApplication).repository)
        )
            .get(WordDetailsViewModel::class.java)
    }

    private var currWordId: Long = 0
    private var wordToSearch: String = ""

    private var partOfSpeechCount = 1
    private var meaningsCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            currWordId = it.getLong(WORD_ID)
            wordToSearch = it.getString(WORD_NAME).toString()
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: 04.07.2021 Add some properties to savedInstanceState

        if (wordToSearch != "0") {
            addWordDetailsFromDictionary()
        } else {
            //Add word details and callbacks for word from vocabulary
            addWordDetailsFromVocabulary()
        }

        binding.editWordBtn.setOnClickListener {
            if (currWordId != 0L) {
                val action =
                    WordDetailsFragmentDirections.actionWordDetailsFragmentToAddNewWordFragment(
                        currWordId
                        // TODO: 16.06.2021 Update edit word functionality
                    )
                view.findNavController().navigate(action)
            } else {
                Snackbar.make(requireView(), R.string.word_update_error, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        binding.addToFavorite.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                if (viewModel.updateWordIsFavorite(currWordId, !binding.addToFavorite.isSelected)) {
                    binding.addToFavorite.isSelected = !binding.addToFavorite.isSelected
                }
            }
        }

        // TODO: 02.06.2021 Add icon for "delete word" menu item
        binding.topAppBar.setNavigationOnClickListener {
            if (viewModel.isAdded.value == true) {
                findNavController().navigate(R.id.action_wordDetailsFragment_to_wordListFragment)
            } else {
                findNavController().popBackStack()
            }
        }


        viewModel.status.observe(viewLifecycleOwner, {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })


        // TODO: 01.06.2021 Complete button selector for AddToFav btn and PronounceWord btn

//        binding.searchTranslationBtn.setOnClickListener {
//            searchWordTranslationInWeb()
//        }

    }

    @SuppressLint("SetTextI18n")
    private fun addWordDetailsFromVocabulary() {
        viewModel.getWordWithPosAndMeaningsById(currWordId).observe(viewLifecycleOwner, {
            it?.let {
                addWordMainDetails(it)
                binding.addToFavorite.visibility = VISIBLE
                binding.pronounceWord.visibility = VISIBLE
            }
        })
    }

    private fun addWordDetailsFromDictionary() {
        viewModel.isAdded.observe(viewLifecycleOwner, {
            binding.addToVocabulary.visibility = if (it) GONE else VISIBLE
            binding.addToFavorite.visibility = if (it) VISIBLE else GONE
            binding.editWordBtn.visibility = if (it) VISIBLE else GONE
        })
        binding.pronounceWord.visibility = VISIBLE

        lateinit var formattedWord: WordWithPartsOfSpeechAndMeanings
        viewModel.getWordFromDictionary(wordToSearch)
        viewModel.wordFromDictionary.observe(viewLifecycleOwner, {
            it?.let {
                formattedWord = dictionaryWordToVocabularyFormat(it)
                addWordMainDetails(formattedWord)
            }
        })

        viewModel.wordId.observe(viewLifecycleOwner, {
            currWordId = it
        })
        binding.addToVocabulary.setOnClickListener {
            viewModel.insertWordWithPartsOfSpeechAndMeanings(formattedWord)
        }
    }

    private fun dictionaryWordToVocabularyFormat(wordFromDictionary: WordFromDictionary): WordWithPartsOfSpeechAndMeanings {

        val partsOfSpeechWithMeanings = mutableListOf<PartOfSpeechWithMeanings>()

        wordFromDictionary.meanings.forEach {
            val newPartOfSpeechWithMeanings = PartOfSpeechWithMeanings(
                PartOfSpeech(posId = 0, wordId = 0, partOfSpeech = it.partOfSpeech),
                it.definitions.let { meanings ->
                    val newMeaningsList = mutableListOf<Meaning>()
                    meanings.forEach { meaning ->
                        newMeaningsList.add(
                            Meaning(
                                meaningId = 0,
                                posId = 0,
                                meaning = meaning.definition,
                                example = meaning.example,
                                synonyms = meaning.synonyms.joinToString(separator = ", ")
                            )
                        )
                    }
                    newMeaningsList
                }
            )
            partsOfSpeechWithMeanings.add(newPartOfSpeechWithMeanings)
        }

        return WordWithPartsOfSpeechAndMeanings(
            Word(
                wordId = 0,
                word = wordFromDictionary.word,
                transcription = if (wordFromDictionary.phonetics.isNotEmpty()) wordFromDictionary.phonetics[0].text else "",
                audioUrl = if (wordFromDictionary.phonetics.isNotEmpty()) wordFromDictionary.phonetics[0].audio else ""
            ),
            partsOfSpeechWithMeanings
        )
    }

    @SuppressLint("SetTextI18n")
    private fun addWordMainDetails(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        binding.word.text = wordWithPartsOfSpeechAndMeanings.word.word
        if (wordWithPartsOfSpeechAndMeanings.word.transcription.isNotEmpty()) {
            binding.transcription.text = "[${wordWithPartsOfSpeechAndMeanings.word.transcription}]"
        } else {
            binding.transcription.visibility = GONE
        }
        binding.addToFavorite.isSelected = wordWithPartsOfSpeechAndMeanings.word.isFavourite == 1

        binding.wordDetails.removeAllViews()
        partOfSpeechCount = 1
        binding.translation.text = ""
        binding.translation.visibility = GONE

        wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.forEach { partOfSpeechWithMeanings ->
            addPartOfSpeech(partOfSpeechWithMeanings)
        }
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
        viewModel.isAdded.observe(viewLifecycleOwner) {
            binding.topAppBar.menu.forEach { menuItem ->
                menuItem.isVisible = it
            }
        }
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
        if (wordToSearch == "0") {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addPartOfSpeech(partOfSpeechWithMeanings: PartOfSpeechWithMeanings) {
        val wordDetailsLinearLayout = LinearLayout(requireContext())
        wordDetailsLinearLayout.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        wordDetailsLinearLayout.orientation = LinearLayout.VERTICAL

        val partOfSpeech = partOfSpeechWithMeanings.partOfSpeech

        val partOfSpeechTextView = TextView(requireContext())

        partOfSpeechTextView.id =
            R.id.part_of_speech + UniqueIdAddition.PART_OF_SPEECH.idAddition + partOfSpeechCount

        val paramsMargin = MarginLayoutParams(
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        )
        if (partOfSpeechCount > 1) {
            paramsMargin.topMargin = AddNewWordFragment.convertDpToPixel(14)
        }

        partOfSpeechTextView.layoutParams = paramsMargin

        partOfSpeechTextView.text = if (partOfSpeech.translation.isNotEmpty()) {
            "- ${partOfSpeech.partOfSpeech} (${partOfSpeech.translation})"
        } else {
            "- ${partOfSpeech.partOfSpeech}"
        }

        TextViewCompat.setTextAppearance(
            partOfSpeechTextView,
            R.style.TextAppearance_Yocabulary_PartOfSpeech
        )

        binding.translation.text = binding.translation.text.let {
            if (it.isNotEmpty()) {
                toString() + ", ${partOfSpeech.translation}"
            } else {
                partOfSpeech.translation.also { trans ->
                    if (trans.isNotEmpty()) {
                        binding.translation.visibility = VISIBLE
                    }
                }
            }
        }

        partOfSpeechCount++

        wordDetailsLinearLayout.addView(partOfSpeechTextView)

        // TODO: 03.07.2021 FIX IDS
        meaningsCount = 1
        partOfSpeechWithMeanings.meanings.forEach {
            wordDetailsLinearLayout.addView(addDefinition(it))
        }

        binding.wordDetails.addView(wordDetailsLinearLayout)

    }

    @SuppressLint("SetTextI18n")
    private fun addDefinition(meaning: Meaning): View {
        val definitionLinearLayout = LinearLayout(requireContext())
        definitionLinearLayout.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        definitionLinearLayout.orientation = LinearLayout.VERTICAL

        if (meaning.meaning.isNotEmpty()) {
            val meaningTextView = TextView(requireContext())
            meaningTextView.id = R.id.meaning + UniqueIdAddition.MEANING.idAddition + meaningsCount

            val paramsMargin = MarginLayoutParams(
                LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            )
            if (meaningsCount > 1) {
                paramsMargin.topMargin = AddNewWordFragment.convertDpToPixel(8)
            } else {
                paramsMargin.topMargin = AddNewWordFragment.convertDpToPixel(6)
            }
            meaningTextView.layoutParams = paramsMargin

            meaningTextView.text = "${meaningsCount}. ${meaning.meaning}"
            TextViewCompat.setTextAppearance(
                meaningTextView,
                R.style.TextAppearance_Yocabulary_Meaning
            )

            definitionLinearLayout.addView(meaningTextView)
        }

        if (meaning.example.isNotEmpty()) {
            val exampleTextView = TextView(requireContext())
            exampleTextView.id = R.id.example + UniqueIdAddition.EXAMPLE.idAddition + meaningsCount

            val paramsWithMargin = MarginLayoutParams(
                LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            )
            paramsWithMargin.topMargin = AddNewWordFragment.convertDpToPixel(8)
            exampleTextView.layoutParams = paramsWithMargin

            exampleTextView.text = if (meaning.meaning.isNotEmpty()) {
                meaning.example
            } else {
                "${meaningsCount}. ${meaning.example}"
            }
            TextViewCompat.setTextAppearance(
                exampleTextView,
                R.style.TextAppearance_Yocabulary_Example
            )

            definitionLinearLayout.addView(exampleTextView)
        }

        if (meaning.synonyms.isNotEmpty()) {
            val synonymsHeaderTextView = TextView(requireContext())

            val paramsWithMargin = MarginLayoutParams(
                LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            )
            paramsWithMargin.topMargin = AddNewWordFragment.convertDpToPixel(8)
            synonymsHeaderTextView.layoutParams = paramsWithMargin

            synonymsHeaderTextView.text = getString(R.string.synonyms_header)
            TextViewCompat.setTextAppearance(
                synonymsHeaderTextView,
                R.style.TextAppearance_Yocabulary_PartOfSpeech
            )
            synonymsHeaderTextView.textSize = 16f

            definitionLinearLayout.addView(synonymsHeaderTextView)

            val synonymsWordsTextView = TextView(requireContext())
            synonymsWordsTextView.id =
                R.id.synonym_words + UniqueIdAddition.SYNONYM_WORDS.idAddition + meaningsCount

            paramsWithMargin.topMargin = AddNewWordFragment.convertDpToPixel(4)
            synonymsWordsTextView.layoutParams = paramsWithMargin

            synonymsWordsTextView.text = if (meaning.meaning.isNotEmpty()) {
                meaning.synonyms
            } else {
                "${meaningsCount}. ${meaning.synonyms}"
            }
            TextViewCompat.setTextAppearance(
                synonymsWordsTextView,
                R.style.TextAppearance_Yocabulary_SynonymWords
            )

            definitionLinearLayout.addView(synonymsWordsTextView)
        }

        meaningsCount++
        return definitionLinearLayout
    }

}