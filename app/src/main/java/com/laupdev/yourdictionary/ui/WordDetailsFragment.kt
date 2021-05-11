package com.laupdev.yourdictionary.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
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
        val currWordObj = viewModel.getWordByName(currWord)
        binding.word.text = currWordObj.value?.word ?: "Error"

    }
}