package com.laupdev.yourdictionary.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.laupdev.yourdictionary.R
import com.laupdev.yourdictionary.application.DictionaryApplication
import com.laupdev.yourdictionary.databinding.FragmentWordDetailsBinding
import com.laupdev.yourdictionary.model.DictionaryViewModel
import com.laupdev.yourdictionary.model.DictionaryViewModelFactory

class WordDetailsFragment : Fragment() {

    companion object {
        const val WORD_ID = "word_id"
    }

    private var _binding: FragmentWordDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DictionaryViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, DictionaryViewModelFactory((activity.application as DictionaryApplication).repository))
            .get(DictionaryViewModel::class.java)
    }

    private var currWordId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        println("_____________________________________WDF___________")
        arguments?.let {
            currWordId = it.getInt(WORD_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWordDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var wordId: Int = 0
        viewModel.getWordById(currWordId).observe(viewLifecycleOwner, {
            it?.let {
                wordId = it.id
                binding.word.text = it.word
                binding.transcription.text = it.transcription
                binding.translation.text = it.translation
                binding.meaning.text = it.meaning
                binding.example.text = it.example
            }
        })
        binding.editWordBtn.setOnClickListener {
            if (wordId != 0) {
                val action =
                    WordDetailsFragmentDirections.actionWordDetailsFragmentToAddNewWordFragment(wordId)
                view.findNavController().navigate(action)
            } else {
                Snackbar.make(requireView(), R.string.word_update_error, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.remove_word_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.remove_word -> {
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

}