package com.laupdev.yocabulary.ui.practice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.databinding.FragmentMeaningsPracticeBinding

class MeaningsPracticeFragment : BasePracticeFragment() {

    private lateinit var binding: FragmentMeaningsPracticeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMeaningsPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.test.setOnClickListener {
//            nextPage()
//        }
    }
}