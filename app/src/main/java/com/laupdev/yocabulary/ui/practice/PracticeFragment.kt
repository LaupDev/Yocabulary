package com.laupdev.yocabulary.ui.practice

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.adapters.PracticeQuestionAdapter
import com.laupdev.yocabulary.databinding.FragmentPracticeBinding
import com.laupdev.yocabulary.model.practice.PracticeViewModel
import com.laupdev.yocabulary.ui.MainActivity

open class PracticeFragment : Fragment() {

    private val viewModel: PracticeViewModel by viewModels()
    private lateinit var viewPager2: ViewPager2

    private var _binding: FragmentPracticeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            (requireActivity() as MainActivity).showBottomNav()
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager2 = binding.pager
        viewPager2.adapter = PracticeQuestionAdapter(viewModel)
        viewPager2.isUserInputEnabled = false

        (requireActivity() as MainActivity).hideBottomNav()

        binding.topAppBar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).showBottomNav()
            findNavController().popBackStack()
        }

        binding.practiceProgressBar.max = viewModel.questions.size

        viewModel.practiceProgress.observe(viewLifecycleOwner) {
            binding.practiceProgressBar.setProgressCompat(it, true)
        }
    }

    fun nextPage() {
        // TODO: 04.09.2021 Go to next page
        viewModel.nextQuestion()
        viewPager2.apply {
            this.currentItem = this.currentItem + 1
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}