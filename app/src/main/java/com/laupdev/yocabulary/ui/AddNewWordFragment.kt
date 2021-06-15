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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.laupdev.yocabulary.AdapterForDropdown
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.application.DictionaryApplication
import com.laupdev.yocabulary.databinding.FragmentAddNewWordBinding
import com.laupdev.yocabulary.model.AddWordViewModel
import com.laupdev.yocabulary.model.AddWordViewModelFactory
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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

    private var wordId = 0
    private var partsOfSpeechCount = 1
    private var currentPartOfSpeechCount = 0
    private val meaningsCountMap = mutableMapOf(partsOfSpeechCount to 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            wordId = it.getInt(WORD_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewWordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        if (savedInstanceState == null) {
//            binding.translation.isEnabled = false
//            binding.meaning.isEnabled = false
//            binding.example.isEnabled = false
//        }

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

        binding.saveWord.setOnClickListener {
            addWordToDatabase()
        }

//        val adapter = AdapterForDropdown(
//            requireContext(),
//            resources.getStringArray(R.array.parts_of_speech).toList()
//        )
//        val adapter = ArrayAdapter(requireContext(), R.layout.view_pos_list_item, resources.getStringArray(R.array.parts_of_speech))
//        binding.partOfSpeechDropdown.setAdapter(adapter)
//        binding.partOfSpeechDropdown.setDropDownBackgroundDrawable(
//            ResourcesCompat.getDrawable(
//                resources,
//                R.drawable.custom_popupmenu_background,
//                null
//            )
//        )

//        binding.partOfSpeechDropdown.onItemClickListener =
//            AdapterView.OnItemClickListener { _, _, _, _ ->
//                binding.translation.isEnabled = true
//                binding.meaning.isEnabled = true
//                binding.example.isEnabled = true
//            }
        binding.addPartOfSpeech.setOnClickListener {
            addPartOfSpeech()
        }

        if (wordId != 0) {
            viewModel.getWordById(wordId).observe(viewLifecycleOwner, {
                it?.let {
                    binding.newWordEditText.setText(it.word)
                    binding.transcriptionEditText.setText(it.transcription)
//                    binding.translationEditText.setText(it.translation)
//                    binding.meaningEditText.setText(it.meaning)
//                    binding.exampleEditText.setText(it.example)
                }
            })
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
        if (binding.newWordEditText.text != null &&
            binding.newWordEditText.text.toString().isNotEmpty()
        ) {
            binding.newWord.error = null
//            val newWord = Word(
//                wordId,
//                binding.newWordEditText.text.toString(),
//                binding.translationEditText.text.toString(),
//                binding.transcriptionEditText.text.toString(),
//                binding.meaningEditText.text.toString(),
//                binding.exampleEditText.text.toString()
//            )
//            if (wordId == 0) {
//                viewModel.insert(newWord)
//                Snackbar.make(requireView(), R.string.new_word_added, Snackbar.LENGTH_SHORT).show()
//            } else {
//                viewModel.update(newWord)
//                Snackbar.make(requireView(), R.string.word_updated_success, Snackbar.LENGTH_SHORT)
//                    .show()
//            }
            requireView().findNavController().popBackStack()
        } else {
            binding.newWord.isErrorEnabled = true
            binding.newWord.error = getString(R.string.word_error_message)
        }
    }

    private fun addPartOfSpeech() {
        val newPartOfSpeechView = layoutInflater.inflate(R.layout.view_part_of_speech, binding.addWordFields, false) as LinearLayout
        newPartOfSpeechView.id = R.id.part_of_speech_block + 15000 + partsOfSpeechCount

        val partOfSpeechTextInputLayout = newPartOfSpeechView.findViewById<TextInputLayout>(R.id.part_of_speech)
        partOfSpeechTextInputLayout.id = R.id.part_of_speech + 16000 + partsOfSpeechCount
        partOfSpeechTextInputLayout.hint = resources.getString(R.string.part_of_speech,
            "$partsOfSpeechCount."
        )

        val partOfSpeechAutoCompleteTextView = newPartOfSpeechView.findViewById<AutoCompleteTextView>(R.id.part_of_speech_dropdown)
        partOfSpeechAutoCompleteTextView.id = R.id.part_of_speech_dropdown + 17000 + partsOfSpeechCount
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

        val partOfSpeechRemoveButton = newPartOfSpeechView.findViewById<ImageButton>(R.id.remove_part_of_speech)
        partOfSpeechRemoveButton.id = R.id.remove_part_of_speech + 17000 + partsOfSpeechCount
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
            if (currentPartOfSpeechCount == 0) {
                meaningsCountMap.clear()
                partsOfSpeechCount = 1
                meaningsCountMap[partsOfSpeechCount] = 1
            }
        }

        val translationTextInputLayout = newPartOfSpeechView.findViewById<TextInputLayout>(R.id.translation)
        translationTextInputLayout.id = R.id.translation + 18000 + partsOfSpeechCount
        translationTextInputLayout.isEnabled = false
        translationTextInputLayout.hint = resources.getString(R.string.translation, "$partsOfSpeechCount.")
        newPartOfSpeechView.findViewById<TextInputEditText>(R.id.translation_edit_text).id = R.id.translation_edit_text + 18000 + partsOfSpeechCount

        val meaningButton = newPartOfSpeechView.findViewById<Button>(R.id.add_meaning)
        meaningButton.id = R.id.add_meaning + 19000 + partsOfSpeechCount
        meaningButton.isEnabled = false

        addMeaning(newPartOfSpeechView, false, partsOfSpeechCount)

        val tempValue = partsOfSpeechCount
        meaningButton.setOnClickListener {
            addMeaning(newPartOfSpeechView, true, tempValue)
        }

        partsOfSpeechCount++
        currentPartOfSpeechCount++
        meaningsCountMap[partsOfSpeechCount] = 1
        binding.addWordFields.addView(newPartOfSpeechView)
        // TODO: 09.06.2021 Fix bug with disappearing of programmatically added views when turning phone
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

    private fun addMeaning(parentView: ViewGroup, isFieldsEnabled: Boolean, partsOfSpeechCount: Int) {

        val meaningsCount = meaningsCountMap[partsOfSpeechCount] ?: 0

        val newMeaningView = layoutInflater.inflate(R.layout.view_meaning, parentView, false)
        newMeaningView.id = R.id.meaning_block + 20000 + meaningsCount

        val meaningTextInputLayout = newMeaningView.findViewById<TextInputLayout>(R.id.meaning)
        meaningTextInputLayout.id = R.id.meaning + 21000 + meaningsCount
        meaningTextInputLayout.isEnabled = isFieldsEnabled
        meaningTextInputLayout.hint = resources.getString(R.string.meaning, "$partsOfSpeechCount.$meaningsCount.")
        newMeaningView.findViewById<TextInputEditText>(R.id.meaning_edit_text).id = R.id.meaning_edit_text + 21000 + meaningsCount

        val exampleTextInputLayout = newMeaningView.findViewById<TextInputLayout>(R.id.example)
        exampleTextInputLayout.id = R.id.example + 22000 + meaningsCount
        exampleTextInputLayout.isEnabled = isFieldsEnabled
        exampleTextInputLayout.hint = resources.getString(R.string.example, "$partsOfSpeechCount.$meaningsCount.")
        newMeaningView.findViewById<TextInputEditText>(R.id.example_edit_text).id = R.id.example_edit_text + 22000 + meaningsCount

        if (!isFieldsEnabled) {
            parentView.findViewById<AutoCompleteTextView>(R.id.part_of_speech_dropdown + 17000 + partsOfSpeechCount).onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, postion, _ ->
                    when (parent.getItemAtPosition(postion).toString()) {
                        "None" -> {
                            parentView.findViewById<TextInputLayout>(R.id.translation + 18000 + partsOfSpeechCount).isEnabled = false
                            parentView.findViewById<Button>(R.id.add_meaning + 19000 + partsOfSpeechCount)
                            meaningTextInputLayout.isEnabled = false
                            exampleTextInputLayout.isEnabled = false
                        }
                        else -> {
                            parentView.findViewById<TextInputLayout>(R.id.translation + 18000 + partsOfSpeechCount).isEnabled = true
                            parentView.findViewById<Button>(R.id.add_meaning + 19000 + partsOfSpeechCount).isEnabled = true
                            meaningTextInputLayout.isEnabled = true
                            exampleTextInputLayout.isEnabled = true
                        }
                    }
                }
        }

        meaningsCountMap[partsOfSpeechCount] = meaningsCount + 1

        parentView.findViewById<LinearLayout>(R.id.meanings).addView(newMeaningView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}