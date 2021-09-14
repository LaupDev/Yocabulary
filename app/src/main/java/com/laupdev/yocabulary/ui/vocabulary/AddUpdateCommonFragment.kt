package com.laupdev.yocabulary.ui.vocabulary

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.laupdev.yocabulary.ProcessState
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.adapters.AdapterForDropdown
import com.laupdev.yocabulary.application.DictionaryApplication
import com.laupdev.yocabulary.database.*
import com.laupdev.yocabulary.databinding.FragmentAddNewWordBinding
import com.laupdev.yocabulary.model.AddUpdateWordViewModel
import com.laupdev.yocabulary.model.AddUpdateWordViewModelFactory
import timber.log.Timber
import java.text.FieldPosition

abstract class AddUpdateCommonFragment : Fragment() {

    private var _binding: FragmentAddNewWordBinding? = null
    val binding get() = _binding!!

    val viewModel: AddUpdateWordViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this,
            AddUpdateWordViewModelFactory((activity.application as DictionaryApplication).repository)
        )
            .get(AddUpdateWordViewModel::class.java)
    }


    var leaveWithoutWarning = true
    private var loadingAnim: AnimatedVectorDrawable? = null

    private val partsOfSpeechViewGroupWithMeaningsViewGroupMap =
        mutableMapOf<ViewGroup, MutableSet<ViewGroup>>()
    var partOfSpeechViewCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            leavePage()
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

        setListeners()

        setObservers()
    }

    open fun setListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            leavePage()
        }

        binding.newWordEditText.addTextChangedListener(onChangeListener(binding.newWord))

        binding.addPartOfSpeech.setOnClickListener {
            addPartOfSpeechView()
        }

        binding.saveWord.setOnClickListener {
            processWord()
        }

        binding.generalTranslationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setIsTranslationGeneral(isChecked)
            binding.generalTranslation.isEnabled = isChecked
        }
        binding.generalTranslationInfoBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.general_translation_info_title))
                .setMessage(resources.getString(R.string.general_translation_info_description))
                .setPositiveButton(resources.getString(R.string.got_it)) { _, _ ->
                }
                .show()
        }
    }

    private fun setObservers() {

        viewModel.processState.observe(viewLifecycleOwner) {
            binding.loadingScreen.visibility = View.GONE
            loadingAnim?.stop()
            when (it) {
                ProcessState.PROCESSING -> {
                    startLoadingAnimation()
                }
                ProcessState.COMPLETED_ADDING -> {
                    findNavController().popBackStack()
                }
                ProcessState.COMPLETED_UPDATE -> {
                    val action =
                        UpdateWordFragmentDirections.goToWordDetailsAfterUpdate(
                            trimInputField(binding.newWordEditText.text.toString()),
                            true
                        )
                    findNavController().navigate(action)
                }
                ProcessState.FAILED -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(resources.getString(R.string.error))
                        .setMessage(R.string.error_add_or_update)
                        .setPositiveButton(resources.getString(R.string.got_it)) { _, _ ->
                        }
                        .show()
                }
                ProcessState.FAILED_WORD_EXISTS -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(resources.getString(R.string.error))
                        .setMessage(resources.getString(R.string.word_already_exists))
                        .setCancelable(false)
                        .setPositiveButton(resources.getString(R.string.replace)) { _, _ ->
                            replaceWord()
                        }
                        .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                            viewModel.inactivateProcess()
                        }
                        .show()
                }
                else -> {
                }
            }
        }
    }

    abstract fun replaceWord()

    private fun leavePage() {
        if (leaveWithoutWarning) {
            findNavController().popBackStack()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.changes_unsaved))
                .setMessage(resources.getString(R.string.changes_unsaved_desc))
                .setPositiveButton(resources.getString(R.string.stay)) { _, _ -> }
                .setNegativeButton(resources.getString(R.string.leave)) { _, _ ->
                    findNavController().popBackStack()
                }
                .show()
        }
    }

    private fun onChangeListener(textInputLayout: TextInputLayout): TextWatcher =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputLayout.error = null
                textInputLayout.isErrorEnabled = false
                leaveWithoutWarning = false
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
        }

    private fun startLoadingAnimation() {
        binding.loadingScreen.visibility = View.VISIBLE
        loadingAnim = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.anim_loading
        ) as AnimatedVectorDrawable
        binding.loadingImg.setImageDrawable(loadingAnim)
        loadingAnim?.start()
    }

    private fun processWord() {
        if (isFieldsRight()) {
            val partsOfSpeechWithMeanings = mutableListOf<PartOfSpeechWithMeanings>()
            var translations = ""

            partsOfSpeechViewGroupWithMeaningsViewGroupMap.forEach { (partOfSpeechView, meaningsViewSet) ->
                if (partOfSpeechView.findViewById<AutoCompleteTextView>(R.id.part_of_speech_dropdown).text.isNotEmpty()) {
                    val currentTranslation = if (viewModel.isTranslationGeneral.value == true) {
                        ""
                    } else {
                        trimInputField(partOfSpeechView.findViewById<TextInputEditText>(R.id.translation_edit_text).text.toString())
                    }
                    if (currentTranslation.isNotEmpty()) {
                        if (translations.isNotEmpty()) {
                            translations += "| ${currentTranslation.replaceFirstChar { it.lowercase() }}"
                        } else {
                            translations = currentTranslation.replaceFirstChar { it.uppercase() }
                        }
                    }
                    val word = trimInputField(binding.newWordEditText.text.toString())
                    val partOfSpeech =
                        partOfSpeechView.findViewById<AutoCompleteTextView>(R.id.part_of_speech_dropdown).text.toString()
                            .lowercase()

                    partsOfSpeechWithMeanings.add(PartOfSpeechWithMeanings(
                        PartOfSpeech(
                            posId = getPartOfSpeechId(partOfSpeechView),
                            word = word,
                            partOfSpeech = partOfSpeech,
                            translation = currentTranslation
                        ),
                        meaningsViewSet.let {
                            val newMeaningsList = mutableListOf<Meaning>()
                            it.forEach { meaningView ->
                                val meaningText =
                                    trimInputField(
                                        meaningView.findViewById<TextInputEditText>(R.id.meaning_edit_text).text.toString()
                                    )
                                val exampleText =
                                    trimInputField(
                                        meaningView.findViewById<TextInputEditText>(R.id.example_edit_text).text.toString()
                                    )
                                val synonymsText =
                                    trimInputField(
                                        meaningView.findViewById<TextInputEditText>(R.id.synonyms_edit_text).text.toString()
                                    )
                                if (meaningText.isNotEmpty() || exampleText.isNotEmpty() || synonymsText.isNotEmpty()) {
                                    newMeaningsList.add(
                                        Meaning(
                                            meaningId = getMeaningId(meaningView),
                                            posId = getPartOfSpeechId(partOfSpeechView),
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

            val newWord = Word(
                word = trimInputField(binding.newWordEditText.text.toString()),
                transcription = trimInputField(binding.transcriptionEditText.text.toString()),
                translations = if (viewModel.isTranslationGeneral.value == true) {
                    trimInputField(binding.generalTranslationEditText.text.toString())
                } else translations,
                isTranslationGeneral = if (viewModel.isTranslationGeneral.value == true || translations.isEmpty()) 1 else 0,
                audioUrl = binding.audioUrl.text.toString()
            )

            val wordWithPartsOfSpeechAndMeanings = WordWithPartsOfSpeechAndMeanings(
                newWord,
                partsOfSpeechWithMeanings
            )

            doActionOnWord(wordWithPartsOfSpeechAndMeanings)
        }
    }

    abstract fun doActionOnWord(wordWithPartsOfSpeechAndMeanings: WordWithPartsOfSpeechAndMeanings)

    abstract fun getPartOfSpeechId(partOfSpeechView: View): Long

    abstract fun getMeaningId(meaningView: View): Long

    private fun isFieldsRight(): Boolean {
        var isFieldsRight = true

        if (binding.newWordEditText.text.toString().isEmpty()) {
            isFieldsRight = false
            binding.newWord.isErrorEnabled = true
            binding.newWord.error = getString(R.string.required_error)
        }

        partsOfSpeechViewGroupWithMeaningsViewGroupMap.forEach { (partOfSpeechView, _) ->
            if (partOfSpeechView.findViewById<AutoCompleteTextView>(R.id.part_of_speech_dropdown).text.isEmpty()) {
                partOfSpeechView.findViewById<TextInputLayout>(R.id.part_of_speech).apply {
                    this.isErrorEnabled = true
                    this.error = getString(R.string.required_error)
                }
                isFieldsRight = false
            }
        }
        return isFieldsRight
    }

    private fun trimInputField(text: String): String {
        return text.trim().replace("\\s+".toRegex(), " ")
    }

    fun addPartOfSpeechView(partOfSpeech: PartOfSpeech? = null): ViewGroup {
        val newPartOfSpeechView = layoutInflater.inflate(
            R.layout.view_part_of_speech,
            binding.addWordFields,
            false
        ) as LinearLayout

        partOfSpeechViewCount += 1

        partsOfSpeechViewGroupWithMeaningsViewGroupMap[newPartOfSpeechView] = mutableSetOf()
        newPartOfSpeechView.findViewById<TextInputLayout>(R.id.part_of_speech).hint =
            resources.getString(
                R.string.part_of_speech,
                "$partOfSpeechViewCount."
            )

        val partOfSpeechAutoCompleteTextView =
            newPartOfSpeechView.findViewById<AutoCompleteTextView>(R.id.part_of_speech_dropdown)

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
        partOfSpeechAutoCompleteTextView.addTextChangedListener(
            onChangeListener(
                newPartOfSpeechView.findViewById(R.id.part_of_speech)
            )
        )

        newPartOfSpeechView.findViewById<ImageButton>(R.id.remove_part_of_speech)
            .setOnClickListener {
                removePartOfSpeech(
                    newPartOfSpeechView,
                    partsOfSpeechViewGroupWithMeaningsViewGroupMap.keys.indexOf(newPartOfSpeechView)
                )
            }

        newPartOfSpeechView.findViewById<TextInputLayout>(R.id.translation).apply {
            this.hint = resources.getString(R.string.translation, "$partOfSpeechViewCount.")
            viewModel.isTranslationGeneral.observe(viewLifecycleOwner) {
                this.isEnabled = !it
            }
        }

        if (partOfSpeech == null) {
            addMeaningView(newPartOfSpeechView, partOfSpeechViewCount)
        }

        val storedValueForListeners = partOfSpeechViewCount

        newPartOfSpeechView.findViewById<Button>(R.id.add_meaning).setOnClickListener {
            addMeaningView(newPartOfSpeechView, storedValueForListeners)
        }

        partOfSpeech?.let {
            newPartOfSpeechView.tag = it.posId
            partOfSpeechAutoCompleteTextView.setText(it.partOfSpeech)
            newPartOfSpeechView.findViewById<TextInputEditText>(R.id.translation_edit_text)
                .setText(it.translation)
        }

        binding.addWordFields.addView(newPartOfSpeechView)
        return newPartOfSpeechView
    }

    private fun removePartOfSpeech(viewToRemove: View, viewToRemoveIndex: Int) {
        Timber.i(viewToRemove.toString())
        Timber.i("partOfSpeechViewCount before remove: %d", partOfSpeechViewCount)
        binding.addWordFields.removeView(viewToRemove)
        partsOfSpeechViewGroupWithMeaningsViewGroupMap.remove(viewToRemove)
        partOfSpeechViewCount -= 1
        updateNumbers(viewToRemoveIndex)
    }

    private fun updateNumbers(removedViewIndex: Int) {
        partsOfSpeechViewGroupWithMeaningsViewGroupMap.onEachIndexed { index, entry ->
            if (index >= removedViewIndex) {
                val partOfSpeechViewGroupNewPosition = index + 1
                Timber.i("Changed index: %d", index)
                entry.key.findViewById<TextInputLayout>(R.id.part_of_speech).hint =
                    resources.getString(
                        R.string.part_of_speech,
                        "$partOfSpeechViewGroupNewPosition."
                    )
                entry.key.findViewById<TextInputLayout>(R.id.translation).hint =
                    resources.getString(R.string.translation, "$partOfSpeechViewGroupNewPosition.")

                entry.value.forEachIndexed { meaningIndex, meaningViewGroup ->
                    val meaningViewGroupNewPosition = meaningIndex + 1
                    setMeaningViewGroupHints(
                        meaningViewGroup,
                        partOfSpeechViewGroupNewPosition,
                        meaningViewGroupNewPosition
                    )
                }
            }
        }
    }

    fun addMeaningView(
        parentView: ViewGroup,
        parentPartOfSpeechIndex: Int,
        meaning: Meaning? = null
    ) {
        var meaningsCount = partsOfSpeechViewGroupWithMeaningsViewGroupMap[parentView]?.size ?: 0
        meaningsCount += 1

        val newMeaningView =
            layoutInflater.inflate(R.layout.view_meaning, parentView, false) as LinearLayout

        setMeaningViewGroupHints(newMeaningView, parentPartOfSpeechIndex, meaningsCount)

        newMeaningView.findViewById<ImageButton>(R.id.remove_meaning).setOnClickListener {
            removeMeaningView(
                parentView,
                newMeaningView,
                partsOfSpeechViewGroupWithMeaningsViewGroupMap[parentView]?.indexOf(newMeaningView)
                    ?: 0
            )
        }

        meaning?.let {
            newMeaningView.tag = it.meaningId
            newMeaningView.findViewById<TextInputEditText>(R.id.meaning_edit_text)
                .setText(it.meaning)
            newMeaningView.findViewById<TextInputEditText>(R.id.example_edit_text)
                .setText(it.example)
            newMeaningView.findViewById<TextInputEditText>(R.id.synonyms_edit_text)
                .setText(it.synonyms)
        }

        partsOfSpeechViewGroupWithMeaningsViewGroupMap[parentView]?.add(newMeaningView)

        parentView.findViewById<LinearLayout>(R.id.meanings).addView(newMeaningView)
    }

    private fun removeMeaningView(
        parentView: ViewGroup,
        viewToRemove: View,
        viewToRemoveIndex: Int
    ) {
        parentView.findViewById<LinearLayout>(R.id.meanings).removeView(viewToRemove)
        partsOfSpeechViewGroupWithMeaningsViewGroupMap[parentView]?.remove(viewToRemove)
        updateMeaningViewsPosition(
            partsOfSpeechViewGroupWithMeaningsViewGroupMap.keys.indexOf(parentView) + 1,
            partsOfSpeechViewGroupWithMeaningsViewGroupMap[parentView] ?: setOf(),
            viewToRemoveIndex
        )
    }

    private fun updateMeaningViewsPosition(
        partOfSpeechViewPosition: Int,
        meaningViews: Set<ViewGroup>,
        removedViewIndex: Int
    ) {
        meaningViews.forEachIndexed { index, meaningViewGroup ->
            val meaningViewGroupNewPosition = index + 1
            if (index >= removedViewIndex) {
                setMeaningViewGroupHints(
                    meaningViewGroup,
                    partOfSpeechViewPosition,
                    meaningViewGroupNewPosition
                )
            }
        }
    }

    private fun setMeaningViewGroupHints(
        meaningViewGroup: ViewGroup,
        partOfSpeechViewPosition: Int,
        meaningViewGroupPosition: Int
    ) {
        meaningViewGroup.findViewById<TextInputLayout>(R.id.meaning).hint =
            resources.getString(
                R.string.meaning,
                "$partOfSpeechViewPosition.$meaningViewGroupPosition."
            )

        meaningViewGroup.findViewById<TextInputLayout>(R.id.example).hint =
            resources.getString(
                R.string.example,
                "$partOfSpeechViewPosition.$meaningViewGroupPosition."
            )

        meaningViewGroup.findViewById<TextInputLayout>(R.id.synonyms).hint =
            resources.getString(
                R.string.synonyms,
                "$partOfSpeechViewPosition.$meaningViewGroupPosition."
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}