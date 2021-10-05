package com.laupdev.yocabulary.ui.practice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.databinding.FragmentPracticeStartPageBinding
import com.laupdev.yocabulary.exceptions.NotEnoughWords
import com.laupdev.yocabulary.model.practice.PracticeViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val PRACTICE_TYPE_PARAM = "practice_type"

@AndroidEntryPoint
class PracticeStartPageFragment : Fragment() {

    private lateinit var practiceType: PracticeType

    private var _binding: FragmentPracticeStartPageBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<PracticeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            practiceType = it.get(PRACTICE_TYPE_PARAM) as PracticeType
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticeStartPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.resetData()

        when (practiceType) {
            PracticeType.MATCH_MEANINGS -> {
                binding.task.text = getString(R.string.meaning_task)
                viewModel.getMeaningQuestions()
            }
            PracticeType.LEARN_SPELLING -> {

            }
            PracticeType.MIXED -> {

            }
        }
        setListeners()
        setObservers()
    }

    private fun setListeners() {
        binding.startPractice.setOnClickListener {
            val action = PracticeStartPageFragmentDirections.startPractice()
            findNavController().navigate(action)
        }
    }

    private fun setObservers() {
        viewModel.exceptionHolder.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    is NotEnoughWords -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(resources.getString(R.string.not_enough_words_title))
                            .setMessage(resources.getString(R.string.not_enough_words_desc))
                            .setPositiveButton(resources.getString(R.string.got_it)) { _, _ ->
                            }
                            .show()
                    }
                }
                viewModel.clearExceptionHolder()
            }
        }

        viewModel.questions.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isEmpty()) {
                    //Display dialog
                    // TODO: 06.10.2021 Handle case when list is empty
                } else {
                    binding.startPractice.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}