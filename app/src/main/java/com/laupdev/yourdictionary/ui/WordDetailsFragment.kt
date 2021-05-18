package com.laupdev.yourdictionary.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.laupdev.yourdictionary.R
import com.laupdev.yourdictionary.application.DictionaryApplication
import com.laupdev.yourdictionary.databinding.FragmentWordDetailsBinding
import com.laupdev.yourdictionary.model.DictionaryViewModel
import com.laupdev.yourdictionary.model.DictionaryViewModelFactory

class WordDetailsFragment : Fragment() {

    companion object {
        const val WORD = "word"
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

    private lateinit var currWord: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            currWord = it.getString(WORD).toString()
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
        viewModel.getWordByName(currWord).observe(viewLifecycleOwner, {
            it?.let {
                binding.word.text = it.word
                binding.transcription.text = it.transcription
                binding.translation.text = it.translation
                binding.meaning.text = it.meaning
                binding.example.text = it.example
            }
        })

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
        viewModel.removeWord(currWord)
        requireView().findNavController().popBackStack()
    }


}