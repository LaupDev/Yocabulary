package com.laupdev.yocabulary.ui.practice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.laupdev.yocabulary.databinding.FragmentPracticeHomePageBinding

class PracticeHomePageFragment : Fragment() {

    private lateinit var binding: FragmentPracticeHomePageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPracticeHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
    }

    private fun setListeners() {
        binding.matchMeaningsBlock.setOnClickListener {
            goToPractice(PracticeType.MATCH_MEANINGS)
        }
        binding.learnSpellingBlock.setOnClickListener {
            goToPractice(PracticeType.LEARN_SPELLING)
        }
        binding.mixedPracticeBlock.setOnClickListener {
            goToPractice(PracticeType.MIXED)
        }
    }

    private fun goToPractice(practiceType: PracticeType) {
        val action = PracticeHomePageFragmentDirections.goToPracticeStartPage(practiceType)
        findNavController().navigate(action)
    }
}