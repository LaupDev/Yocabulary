package com.laupdev.yocabulary.ui

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
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

    private var pronounceWordMediaPlayer: MediaPlayer? = null

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
//                if (viewModel.updateWordIsFavorite(currWordId, !binding.addToFavorite.isSelected)) {
//                    binding.addToFavorite.isSelected = !binding.addToFavorite.isSelected
//                }
                viewModel.updateWordIsFavorite(currWordId)
            }
        }

        viewModel.isFavourite.observe(viewLifecycleOwner) {
            binding.addToFavorite.isSelected = it
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
        viewModel.isAdded.observe(viewLifecycleOwner) {
            binding.addToVocabulary.visibility = if (it) GONE else VISIBLE
            binding.addToFavorite.visibility = if (it) VISIBLE else GONE
            binding.editWordBtn.visibility = if (it) VISIBLE else GONE
        }
        binding.pronounceWord.visibility = VISIBLE

        println()
        viewModel.getWordFromDictionary(wordToSearch)
        viewModel.wordWithPosAndMeanings.observe(viewLifecycleOwner) {
            it?.let {
                addWordMainDetails(it)
            }
        }

        viewModel.wordId.observe(viewLifecycleOwner, {
            currWordId = it
        })
        binding.addToVocabulary.setOnClickListener {
            viewModel.insertWordWithPartsOfSpeechAndMeanings(viewModel.wordWithPosAndMeanings.value!!)
        }
        // TODO: 12.07.2021 Fix long loading when getting first word from dictionary
    }

    @SuppressLint("SetTextI18n")
    private fun addWordMainDetails(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        println("------------UPDATE------------")
        binding.word.text = wordWithPartsOfSpeechAndMeanings.word.word
        if (wordWithPartsOfSpeechAndMeanings.word.transcription.isNotEmpty()) {
            binding.transcription.text = "[${wordWithPartsOfSpeechAndMeanings.word.transcription}]"
        } else {
            binding.transcription.visibility = GONE
        }
//        binding.addToFavorite.isSelected = wordWithPartsOfSpeechAndMeanings.word.isFavourite == 1

        if (wordWithPartsOfSpeechAndMeanings.word.audioUrl.isNotEmpty()) {
            binding.pronounceWord.setOnClickListener {
                playWordPronunciation(wordWithPartsOfSpeechAndMeanings.word.audioUrl)
            }
        }
        wordWithPartsOfSpeechAndMeanings.word.translations.let {
            if (it.isEmpty()) {
                binding.translation.visibility = GONE
            } else {
                binding.translation.text = it.replace("|", ",")
            }
        }

        clearWordDetails()

        wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.forEach { partOfSpeechWithMeanings ->
            addPartOfSpeech(partOfSpeechWithMeanings)
        }
    }

    private fun playWordPronunciation(audioUrl: String) {
        pronounceWordMediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(audioUrl)
            isLooping = false
            setOnPreparedListener {
                it.start()
            }
            setOnCompletionListener {
                if (_binding != null) {
                    binding.pronounceWord.isSelected = false
                }
                it.release()
            }
            prepareAsync()
            binding.pronounceWord.isSelected = true
        }
    }

    private fun clearWordDetails() {
        binding.wordDetails.removeAllViews()
        partOfSpeechCount = 1
    }

//    private fun searchWordTranslationInWeb() {
//        val queryUrl: Uri = Uri.parse("${SEARCH_PREFIX}${binding.word.text} translation")
//        val intent = Intent(Intent.ACTION_VIEW, queryUrl)
//        requireContext().startActivity(intent)
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        pronounceWordMediaPlayer?.release()
        pronounceWordMediaPlayer = null
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
        binding.addToFavorite.isSelected = false
        if (wordToSearch == "0") {
            findNavController().popBackStack()
//            findNavController().navigate(R.id.action_wordDetailsFragment_to_wordListFragment)
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

//        binding.translation.text = binding.translation.text.let {
//            if (it.isNotEmpty()) {
//                it.toString() + ", ${partOfSpeech.translation}"
//            } else {
//                partOfSpeech.translation.also { trans ->
//                    if (trans.isNotEmpty()) {
//                        binding.translation.visibility = VISIBLE
//                    }
//                }
//            }
//        }

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