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
        val goToPracticeListener = View.OnClickListener {
            val action = PracticeHomePageFragmentDirections.goToPractice()
            findNavController().navigate(action)
        }
        binding.matchMeaningsBlock.setOnClickListener(goToPracticeListener)
        binding.learnSpellingBlock.setOnClickListener(goToPracticeListener)
        binding.mixedPracticeBlock.setOnClickListener(goToPracticeListener)
    }
}