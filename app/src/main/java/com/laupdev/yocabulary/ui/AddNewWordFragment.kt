package com.laupdev.yocabulary.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.laupdev.yocabulary.AdapterForDropdown
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.application.DictionaryApplication
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.databinding.FragmentAddNewWordBinding
import com.laupdev.yocabulary.model.AddWordViewModel
import com.laupdev.yocabulary.model.AddWordViewModelFactory

class AddNewWordFragment : Fragment() {

    companion object {
        const val WORD_ID = "word_id"
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
        // TODO: 07.06.2021 Fix bug with view recreation
        if (savedInstanceState == null) {
            binding.translation.isEnabled = false
            binding.meaning.isEnabled = false
            binding.example.isEnabled = false
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

        binding.saveWord.setOnClickListener {
            addWordToDatabase()
        }

        val adapter = AdapterForDropdown(requireContext(), resources.getStringArray(R.array.parts_of_speech).toList())
//        val adapter = ArrayAdapter(requireContext(), R.layout.view_pos_list_item, resources.getStringArray(R.array.parts_of_speech))
        binding.partOfSpeechDropdown.setAdapter(adapter)
        binding.partOfSpeechDropdown.setDropDownBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.custom_popupmenu_background,
                null
            )
        )

        binding.partOfSpeechDropdown.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                binding.translation.isEnabled = true
                binding.meaning.isEnabled = true
                binding.example.isEnabled = true
            }

        if (wordId != 0) {
            viewModel.getWordById(wordId).observe(viewLifecycleOwner, {
                it?.let {
                    binding.newWordEditText.setText(it.word)
                    binding.transcriptionEditText.setText(it.transcription)
                    binding.translationEditText.setText(it.translation)
                    binding.meaningEditText.setText(it.meaning)
                    binding.exampleEditText.setText(it.example)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}