package com.laupdev.yocabulary.ui

import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.laupdev.yocabulary.AdapterForDropdown
import com.laupdev.yocabulary.ProcessState
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.application.DictionaryApplication
import com.laupdev.yocabulary.database.*
import com.laupdev.yocabulary.databinding.FragmentAddNewWordBinding
import com.laupdev.yocabulary.model.AddWordViewModel
import com.laupdev.yocabulary.model.AddWordViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import kotlin.math.roundToInt

/*** ABBREVIATIONS:
 *
 * TIL = TEXT_INPUT_LAYOUT;
 *
 * TIET = TEXT_INPUT_EDIT_TEXT;
 *
 * ACTV = AUTO_COMPLETE_TEXT_VIEW;
 *
 * RB = REMOVE_BUTTON
 *
 * */
enum class UniqueIdAdditionAddWord(val idAddition: Int) {
    PART_OF_SPEECH_BLOCK(15000),
    PART_OF_SPEECH_TIL(16000),
    PART_OF_SPEECH_ACTV(17000),
    PART_OF_SPEECH_RB(18000),
    TRANSLATION_TIL(19000),
    TRANSLATION_TIET(20000),
    ADD_MEANING_BTN(21000),
    MEANING_BLOCK(22000),
    MEANING_TIL(23000),
    MEANING_TIET(24000),
    EXAMPLE_TIL(25000),
    EXAMPLE_TIET(26000),
    MEANING_RB(27000),
    SYNONYMS_TIL(28000),
    SYNONYMS_TIET(29000)
}

class AddNewWordFragment : Fragment() {

    companion object {
        const val WORD_ID = "word_id"
        fun convertDpToPixel(dp: Int): Int {
            return (dp * (Resources.getSystem().displayMetrics.densityDpi / 160f)).roundToInt()
        }
    }

    private var _binding: FragmentAddNewWordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddWordViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this,
            AddWordViewModelFactory((activity.application as DictionaryApplication).repository)
        )
            .get(AddWordViewModel::class.java)
    }

    private var wordId = 0L
    private var partsOfSpeechCount = 1
    private var currentPartOfSpeechCount = 0
    private var totalMeaningCount = 1
    private val meaningsCountMap = mutableMapOf(partsOfSpeechCount to 1)
    private val partsOfSpeechIdsWithMeaningsIdsMap = mutableMapOf<Int, MutableSet<Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            wordId = it.getLong(WORD_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewWordBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.status.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }

        binding.newWordEditText.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    binding.newWord.error = null
                    binding.newWord.isErrorEnabled = false
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }
            })

        binding.addPartOfSpeech.setOnClickListener {
            addPartOfSpeech()
        }

        binding.saveWord.setOnClickListener {
            addWordToDatabase()
        }

        if (wordId == 0L) {

            binding.searchInDictionary.setOnClickListener {
                if (binding.newWordEditText.text.toString().isNotEmpty()) {
                    binding.newWord.error = null

                    val action =
                        AddNewWordFragmentDirections.actionAddNewWordFragmentToWordDetailsFragment(
                            wordName = binding.newWordEditText.text.toString()
                        )
                    view.findNavController().navigate(action)
                } else {
                    binding.newWord.isErrorEnabled = true
                    binding.newWord.error = getString(R.string.word_error_message)
                }
            }
        } else {
            viewModel.getWordWithPosAndMeaningsById(wordId).observe(viewLifecycleOwner) {
                it?.let {
                    populateFieldsWithData(it)
                }
            }
        }


//        if (wordId != 0L) {
//            viewModel.getWordById(wordId).observe(viewLifecycleOwner, {
//                it?.let {
//                    binding.newWordEditText.setText(it.word)
//                    binding.transcriptionEditText.setText(it.transcription)
//                    binding.translationEditText.setText(it.translation)
//                    binding.meaningEditText.setText(it.meaning)
//                    binding.exampleEditText.setText(it.example)
//                }
//            })
//        }
    }

    private fun populateFieldsWithData(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings) {
        wordWithPartsOfSpeechAndMeanings.word.let {
            binding.newWordEditText.setText(it.word)
            binding.transcriptionEditText.setText(it.transcription)
        }

        wordWithPartsOfSpeechAndMeanings.partsOfSpeechWithMeanings.forEach {
            val tempPartOfSpeechCount = partsOfSpeechCount
            val posBlockLinearLayout = addPartOfSpeech(it.partOfSpeech)
            it.meanings.forEach { meaning ->
                addMeaning(posBlockLinearLayout, true, tempPartOfSpeechCount, meaning)
            }
        }
    }

    /***
     * This function has two use cases:
     * 1. If a user click on the btn for adding new words
     * then this function will add new word to the database
     * 2. If a user click on the edit btn in fragment_word_details
     * then this word will be updated in the database
     ***/
    private fun addWordToDatabase() {
        if (isFieldsRight()) {
            binding.newWord.error = null

            val partsOfSpeechWithMeanings = mutableListOf<PartOfSpeechWithMeanings>()
            var translations = ""
            partsOfSpeechIdsWithMeaningsIdsMap.forEach { (posId, meaningIds) ->
                if (requireView().findViewById<AutoCompleteTextView>(posId + 2000).text.isNotEmpty()) {
                    val currTrans =
                        trimInputField(requireView().findViewById<TextInputEditText>(posId + 5000).text.toString())
                    if (currTrans.isNotEmpty()) {
                        if (translations.isNotEmpty()) {
                            translations += "| ${currTrans.replaceFirstChar { it.lowercase() }}"
                        } else {
                            translations = currTrans.replaceFirstChar { it.uppercase() }
                        }
                    }
                    partsOfSpeechWithMeanings.add(PartOfSpeechWithMeanings(
                        PartOfSpeech(
                            posId = if (wordId == 0L) 0 else requireView().findViewById<LinearLayout>(posId).tag?.toString()?.toLong() ?: 0,
                            wordId = wordId,
                            partOfSpeech = requireView().findViewById<AutoCompleteTextView>(
                                posId + 2000
                            ).text.toString().lowercase(),
                            translation = currTrans
                        ),
                        meaningIds.let {
                            val newMeaningsList = mutableListOf<Meaning>()
                            it.forEach { meaningId ->
                                val meaningText =
                                    trimInputField(
                                        requireView().findViewById<TextInputEditText>(
                                            meaningId + 2000
                                        ).text.toString()
                                    )
                                val exampleText =
                                    trimInputField(
                                        requireView().findViewById<TextInputEditText>(
                                            meaningId + 4000
                                        ).text.toString()
                                    )
                                val synonymsText =
                                    trimInputField(
                                        requireView().findViewById<TextInputEditText>(
                                            meaningId + 7000
                                        ).text.toString()
                                    )
                                if (meaningText.isNotEmpty() || exampleText.isNotEmpty() || synonymsText.isNotEmpty()) {
                                    newMeaningsList.add(
                                        Meaning(
                                            meaningId = if (wordId == 0L) 0 else requireView().findViewById<LinearLayout>(meaningId).tag?.toString()?.toLong() ?: 0,
                                            posId = if (wordId == 0L) 0 else requireView().findViewById<LinearLayout>(posId).tag?.toString()?.toLong() ?: 0, //Change it later for update functionality
                                            meaning = meaningText,
                                            example = exampleText,
                                            synonyms = synonymsText
                                        )
                                    )
                                }
                            }
                            newMeaningsList
                        }
                    ))
                }
            }

            viewModel.addingProcess.observe(viewLifecycleOwner) {
                when (it) {
                    ProcessState.PROCESSING -> {

                    }
                    ProcessState.COMPLETED -> {
                        findNavController().popBackStack()
                    }
                    ProcessState.FAILED -> {

                    }
                    else -> {

                    }
                }
            }

            // TODO: 28.06.2021 Add loading screen with spinner
            viewModel.insertWordWithPartsOfSpeechWithMeanings(
                WordWithPartsOfSpeechAndMeanings(
                    Word(
                        wordId = wordId,
                        word = trimInputField(binding.newWordEditText.text.toString()),
                        transcription = trimInputField(binding.transcriptionEditText.text.toString()),
                        translations = translations
                    ),
                    partsOfSpeechWithMeanings
                )
            )

//            if (wordId == 0) {
//                viewModel.insert(newWord)
//                Snackbar.make(requireView(), R.string.new_word_added, Snackbar.LENGTH_SHORT).show()
//            } else {
//                viewModel.update(newWord)
//                Snackbar.make(requireView(), R.string.word_updated_success, Snackbar.LENGTH_SHORT)
//                    .show()
//            }

        }
    }

    private fun trimInputField(text: String): String {
        return text.trim().replace("\\s+".toRegex(), " ")
    }

    private fun isFieldsRight(): Boolean {
        return if (binding.newWordEditText.text.toString().isNotEmpty()) {
            binding.newWord.error = null
            true
        } else {
            binding.newWord.isErrorEnabled = true
            binding.newWord.error = getString(R.string.word_error_message)
            false
        }
    }

    private fun addPartOfSpeech(partOfSpeech: PartOfSpeech? = null): ViewGroup {
        val newPartOfSpeechView = layoutInflater.inflate(
            R.layout.view_part_of_speech,
            binding.addWordFields,
            false
        ) as LinearLayout
        newPartOfSpeechView.id =
            R.id.part_of_speech_block + UniqueIdAdditionAddWord.PART_OF_SPEECH_BLOCK.idAddition + partsOfSpeechCount

        partsOfSpeechIdsWithMeaningsIdsMap[newPartOfSpeechView.id] = mutableSetOf()

        val partOfSpeechTextInputLayout =
            newPartOfSpeechView.findViewById<TextInputLayout>(R.id.part_of_speech)
        partOfSpeechTextInputLayout.id =
            R.id.part_of_speech_block + UniqueIdAdditionAddWord.PART_OF_SPEECH_TIL.idAddition + partsOfSpeechCount
        partOfSpeechTextInputLayout.hint = resources.getString(
            R.string.part_of_speech,
            "$partsOfSpeechCount."
        )

        val partOfSpeechAutoCompleteTextView =
            newPartOfSpeechView.findViewById<AutoCompleteTextView>(R.id.part_of_speech_dropdown)
        partOfSpeechAutoCompleteTextView.id =
            R.id.part_of_speech_block + UniqueIdAdditionAddWord.PART_OF_SPEECH_ACTV.idAddition + partsOfSpeechCount
        val adapter = AdapterForDropdown(
            requireContext(),
            resources.getStringArray(R.array.parts_of_speech).toList()
        )
        partOfSpeechAutoCompleteTextView.setAdapter(adapter)
        partOfSpeechAutoCompleteTextView.setDropDownBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.custom_popupmenu_background,
                null
            )
        )

        // TODO: 01.07.2021 Add "Other" option for "Part of speech" field and additional input field

        val partOfSpeechRemoveButton =
            newPartOfSpeechView.findViewById<ImageButton>(R.id.remove_part_of_speech)
        partOfSpeechRemoveButton.id =
            R.id.part_of_speech_block + UniqueIdAdditionAddWord.PART_OF_SPEECH_RB.idAddition + partsOfSpeechCount
        val constraintSet = ConstraintSet()
        constraintSet.clone(newPartOfSpeechView.findViewById<ConstraintLayout>(R.id.pos_constraint_layout))
        constraintSet.connect(
            partOfSpeechRemoveButton.id,
            ConstraintSet.START,
            partOfSpeechTextInputLayout.id,
            ConstraintSet.END
        )
        constraintSet.applyTo(newPartOfSpeechView.findViewById(R.id.pos_constraint_layout))

        partOfSpeechRemoveButton.setOnClickListener {
            binding.addWordFields.removeView(newPartOfSpeechView)
            currentPartOfSpeechCount--
            partsOfSpeechIdsWithMeaningsIdsMap.remove(newPartOfSpeechView.id)
            if (currentPartOfSpeechCount == 0) {
                meaningsCountMap.clear()
                partsOfSpeechCount = 1
                meaningsCountMap[partsOfSpeechCount] = 1
            }
        }

        val translationTextInputLayout =
            newPartOfSpeechView.findViewById<TextInputLayout>(R.id.translation)
        translationTextInputLayout.id =
            R.id.part_of_speech_block + UniqueIdAdditionAddWord.TRANSLATION_TIL.idAddition + partsOfSpeechCount
        if (partOfSpeech == null) {
            translationTextInputLayout.isEnabled = false
        }
        translationTextInputLayout.hint =
            resources.getString(R.string.translation, "$partsOfSpeechCount.")
        val translationTextInputEditText = newPartOfSpeechView.findViewById<TextInputEditText>(R.id.translation_edit_text)
        translationTextInputEditText.id =
            R.id.part_of_speech_block + UniqueIdAdditionAddWord.TRANSLATION_TIET.idAddition + partsOfSpeechCount

        val addMeaningButton = newPartOfSpeechView.findViewById<Button>(R.id.add_meaning)
        addMeaningButton.id =
            R.id.part_of_speech_block + UniqueIdAdditionAddWord.ADD_MEANING_BTN.idAddition + partsOfSpeechCount
        if (partOfSpeech == null) {
            addMeaningButton.isEnabled = false
        }

        if (partOfSpeech == null) {
            addMeaning(newPartOfSpeechView, false, partsOfSpeechCount)
        }
        val tempValue = partsOfSpeechCount
        addMeaningButton.setOnClickListener {
            addMeaning(newPartOfSpeechView, true, tempValue)
        }

        partOfSpeech?.let {
            newPartOfSpeechView.tag = it.posId
            partOfSpeechAutoCompleteTextView.setText(it.partOfSpeech)
            translationTextInputEditText.setText(it.translation)
        }

        partsOfSpeechCount++
        currentPartOfSpeechCount++
        meaningsCountMap[partsOfSpeechCount] = 1
        binding.addWordFields.addView(newPartOfSpeechView)
        // TODO: 09.06.2021 Fix bug with disappearing of programmatically added views when turning phone
        return newPartOfSpeechView
    }

//    private fun removePartOfSpeech(partOfSpeechPosition: Int) {
//        for (pair in meaningsCountMap) {
//            if (pair.key >= partOfSpeechPosition) {
//                meaningsCountMap[pair.key - 1] = pair.value
//            }
//        }
//        meaningsCountMap.remove(partsOfSpeechCount)
//        partsOfSpeechCount--
//    }

    private fun addMeaning(
        parentView: ViewGroup,
        isFieldsEnabled: Boolean,
        partsOfSpeechCount: Int,
        meaning: Meaning? = null
    ) {

//        val meaningsCount = partsOfSpeechIdsWithMeaningsIdsMap[parentView.id]?.size?.plus(1) ?: 1

        val meaningsCount = meaningsCountMap[partsOfSpeechCount] ?: 1

        val newMeaningView = layoutInflater.inflate(R.layout.view_meaning, parentView, false)
        newMeaningView.id =
            R.id.meaning_block + UniqueIdAdditionAddWord.MEANING_BLOCK.idAddition + totalMeaningCount

        val meaningTextInputLayout = newMeaningView.findViewById<TextInputLayout>(R.id.meaning)
        meaningTextInputLayout.id =
            R.id.meaning_block + UniqueIdAdditionAddWord.MEANING_TIL.idAddition + totalMeaningCount
        meaningTextInputLayout.isEnabled = isFieldsEnabled
        meaningTextInputLayout.hint =
            resources.getString(R.string.meaning, "$partsOfSpeechCount.$meaningsCount.")
        val meaningTextInputEditText = newMeaningView.findViewById<TextInputEditText>(R.id.meaning_edit_text)
        meaningTextInputEditText.id =
            R.id.meaning_block + UniqueIdAdditionAddWord.MEANING_TIET.idAddition + totalMeaningCount

        val exampleTextInputLayout = newMeaningView.findViewById<TextInputLayout>(R.id.example)
        exampleTextInputLayout.id =
            R.id.meaning_block + UniqueIdAdditionAddWord.EXAMPLE_TIL.idAddition + totalMeaningCount
        exampleTextInputLayout.isEnabled = isFieldsEnabled
        exampleTextInputLayout.hint =
            resources.getString(R.string.example, "$partsOfSpeechCount.$meaningsCount.")
        val exampleTextInputEditText = newMeaningView.findViewById<TextInputEditText>(R.id.example_edit_text)
        exampleTextInputEditText.id =
            R.id.meaning_block + UniqueIdAdditionAddWord.EXAMPLE_TIET.idAddition + totalMeaningCount

        val synonymsTextInputLayout = newMeaningView.findViewById<TextInputLayout>(R.id.synonyms)
        synonymsTextInputLayout.id =
            R.id.meaning_block + UniqueIdAdditionAddWord.SYNONYMS_TIL.idAddition + totalMeaningCount
        synonymsTextInputLayout.isEnabled = isFieldsEnabled
        synonymsTextInputLayout.hint =
            resources.getString(R.string.synonyms, "$partsOfSpeechCount.$meaningsCount.")
        val synonymsTextInputEditText = newMeaningView.findViewById<TextInputEditText>(R.id.synonyms_edit_text)
        synonymsTextInputEditText.id =
            R.id.meaning_block + UniqueIdAdditionAddWord.SYNONYMS_TIET.idAddition + totalMeaningCount

        val meaningRemoveButton = newMeaningView.findViewById<ImageButton>(R.id.remove_meaning)
        meaningRemoveButton.id =
            R.id.meaning_block + UniqueIdAdditionAddWord.MEANING_RB.idAddition + totalMeaningCount
        meaningRemoveButton.isEnabled = isFieldsEnabled

        meaningRemoveButton.setOnClickListener {
            parentView.findViewById<LinearLayout>(R.id.meanings).removeView(newMeaningView)
            partsOfSpeechIdsWithMeaningsIdsMap[parentView.id]?.remove(newMeaningView.id)
            if (partsOfSpeechIdsWithMeaningsIdsMap[parentView.id]?.size?.plus(1) ?: 1 == 1) {
                meaningsCountMap[partsOfSpeechCount] = 1
            }
        }

        if (!isFieldsEnabled) {
            parentView.findViewById<AutoCompleteTextView>(R.id.part_of_speech_block + UniqueIdAdditionAddWord.PART_OF_SPEECH_ACTV.idAddition + partsOfSpeechCount)
                .onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    when (parent.getItemAtPosition(position).toString()) {
                        "None" -> {
                            parentView.findViewById<AutoCompleteTextView>(
                                R.id.part_of_speech_block +
                                        UniqueIdAdditionAddWord.PART_OF_SPEECH_ACTV.idAddition +
                                        partsOfSpeechCount
                            )
                                .setText("")
                            parentView.findViewById<TextInputLayout>(R.id.part_of_speech_block + UniqueIdAdditionAddWord.TRANSLATION_TIL.idAddition + partsOfSpeechCount)
                                .isEnabled =
                                false
                            parentView.findViewById<Button>(R.id.part_of_speech_block + UniqueIdAdditionAddWord.ADD_MEANING_BTN.idAddition + partsOfSpeechCount)
                                .isEnabled = false
                            meaningTextInputLayout.isEnabled = false
                            exampleTextInputLayout.isEnabled = false
                            synonymsTextInputLayout.isEnabled = false
                            meaningRemoveButton.isEnabled = false
// TODO: 16.07.2021 FIX BUG. Recreation: 1. Add "Part of speech" block. 2. Remove "Meaning" block. 3. Add "Meaning" block. 4. Set "Part of speech" field to "None"
                        }
                        else -> {
                            parentView.findViewById<TextInputLayout>(R.id.part_of_speech_block + UniqueIdAdditionAddWord.TRANSLATION_TIL.idAddition + partsOfSpeechCount)
                                .isEnabled =
                                true
                            parentView.findViewById<Button>(R.id.part_of_speech_block + UniqueIdAdditionAddWord.ADD_MEANING_BTN.idAddition + partsOfSpeechCount).isEnabled =
                                true
                            meaningTextInputLayout.isEnabled = true
                            exampleTextInputLayout.isEnabled = true
                            synonymsTextInputLayout.isEnabled = true
                            meaningRemoveButton.isEnabled = true
                        }
                    }
                }
        }

        meaning?.let {
            newMeaningView.tag = it.meaningId
            meaningTextInputEditText.setText(it.meaning)
            exampleTextInputEditText.setText(it.example)
            synonymsTextInputEditText.setText(it.synonyms)
        }

        totalMeaningCount++
        meaningsCountMap[partsOfSpeechCount] = meaningsCount + 1
        partsOfSpeechIdsWithMeaningsIdsMap[R.id.part_of_speech_block + UniqueIdAdditionAddWord.PART_OF_SPEECH_BLOCK.idAddition + partsOfSpeechCount]?.add(
            newMeaningView.id
        )

        parentView.findViewById<LinearLayout>(R.id.meanings).addView(newMeaningView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}