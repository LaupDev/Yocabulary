package com.laupdev.yourdictionary.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.laupdev.yourdictionary.R
import com.laupdev.yourdictionary.application.DictionaryApplication
import com.laupdev.yourdictionary.database.Word
import com.laupdev.yourdictionary.databinding.FragmentAddNewWordBinding
import com.laupdev.yourdictionary.model.AddWordViewModel
import com.laupdev.yourdictionary.model.AddWordViewModelFactory

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
        binding.newWordEditText.setOnKeyListener { _, _, _ ->
            binding.newWord.error = null
            false
        }
        binding.saveButton.setOnClickListener {
            addWordToDatabase()
        }
        binding.cancelButton.setOnClickListener {
            view.findNavController().popBackStack()
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
            val newWord = Word(
                wordId,
                binding.newWordEditText.text.toString(),
                binding.translationEditText.text.toString(),
                binding.transcriptionEditText.text.toString(),
                binding.meaningEditText.text.toString(),
                binding.exampleEditText.text.toString()
            )
            if (wordId == 0) {
                viewModel.insert(newWord)
                Snackbar.make(requireView(), R.string.new_word_added, Snackbar.LENGTH_SHORT).show()
            } else {
                viewModel.update(newWord)
                Snackbar.make(requireView(), R.string.word_updated_success, Snackbar.LENGTH_SHORT)
                    .show()
            }
            requireView().findNavController().popBackStack()
        } else {
            binding.newWord.error = getString(R.string.word_error_message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}